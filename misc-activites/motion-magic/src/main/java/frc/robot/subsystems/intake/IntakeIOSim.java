package frc.robot.subsystems.intake;

import static frc.robot.Constants.IntakeConstants.*;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;
import org.littletonrobotics.junction.LoggedRobot;

/**
 * Physics sim implementation of the intake IO.
 *
 * <p>This stands in for the Talons and the mechanism. It doesn't use CTRE's simulated Talon, so the
 * Motion Magic the real IO configures is emulated here with the same trapezoid profile, P gain, and
 * velocity feedforward. Homing is skipped, the pivot always starts out at a known position.
 */
public class IntakeIOSim implements IntakeIO {
  // No gravity: the pivot geometry isn't known, and the robot doesn't compensate for it either. The
  // arm length only matters for gravity
  private final SingleJointedArmSim pivotSim =
      new SingleJointedArmSim(
          GEARBOX,
          GEAR_RATIO,
          MOMENT_OF_INERTIA_KG_METERS_SQUARED,
          0.3,
          HOMED_ANGLE_RAD,
          MAX_ANGLE_RAD,
          false,
          HOMED_ANGLE_RAD);
  private final DCMotorSim rightRollerSim = createRollerSim();
  private final DCMotorSim leftRollerSim = createRollerSim();

  private final TrapezoidProfile profile =
      new TrapezoidProfile(
          new TrapezoidProfile.Constraints(
              MAX_VELOCITY_RAD_PER_SEC, MAX_ACCELERATION_RAD_PER_SEC_SQ));
  private TrapezoidProfile.State setpoint = new TrapezoidProfile.State(HOMED_ANGLE_RAD, 0.0);
  private double goalAngleRad = HOMED_ANGLE_RAD;
  private double rollerDutyCycle = 0.0;

  private static DCMotorSim createRollerSim() {
    return new DCMotorSim(
        LinearSystemId.createDCMotorSystem(GEARBOX, MOMENT_OF_INERTIA_KG_METERS_SQUARED, 1.0),
        GEARBOX);
  }

  @Override
  public void updateInputs(IntakeIOInputs inputs) {
    double batteryVolts = RobotController.getBatteryVoltage();

    // Motion Magic: follow the profile toward the goal, with a P loop and velocity feedforward.
    // It starts over from the pivot's state whenever the robot is disabled
    if (DriverStation.isDisabled()) {
      setpoint =
          new TrapezoidProfile.State(pivotSim.getAngleRads(), pivotSim.getVelocityRadPerSec());
    } else {
      setpoint =
          profile.calculate(
              LoggedRobot.defaultPeriodSecs,
              setpoint,
              new TrapezoidProfile.State(goalAngleRad, 0.0));
    }
    double pivotRequestVolts =
        KP * (setpoint.position - pivotSim.getAngleRads()) + KV * setpoint.velocity;
    double pivotVolts =
        limitedVolts(
            pivotRequestVolts,
            pivotSim.getVelocityRadPerSec() * GEAR_RATIO,
            PIVOT_STATOR_CURRENT_LIMIT_AMPS);
    double rollerRequestVolts = rollerDutyCycle * batteryVolts;
    double rightVolts =
        limitedVolts(
            rollerRequestVolts,
            rightRollerSim.getAngularVelocityRadPerSec(),
            ROLLER_STATOR_CURRENT_LIMIT_AMPS);
    double leftVolts =
        limitedVolts(
            rollerRequestVolts,
            leftRollerSim.getAngularVelocityRadPerSec(),
            ROLLER_STATOR_CURRENT_LIMIT_AMPS);

    pivotSim.setInputVoltage(pivotVolts);
    rightRollerSim.setInputVoltage(rightVolts);
    leftRollerSim.setInputVoltage(leftVolts);
    pivotSim.update(LoggedRobot.defaultPeriodSecs);
    rightRollerSim.update(LoggedRobot.defaultPeriodSecs);
    leftRollerSim.update(LoggedRobot.defaultPeriodSecs);

    // Battery current is electrical power over battery voltage
    double pivotTorqueCurrent =
        GEARBOX.getCurrent(pivotSim.getVelocityRadPerSec() * GEAR_RATIO, pivotVolts);

    inputs.homed = true;

    inputs.pivotConnected = true;
    inputs.pivotPositionRad = pivotSim.getAngleRads();
    inputs.pivotVelocityRadPerSec = pivotSim.getVelocityRadPerSec();
    inputs.pivotAppliedVolts = pivotVolts;
    inputs.pivotTorqueCurrentAmps = pivotTorqueCurrent;
    inputs.pivotSupplyCurrentAmps = pivotVolts * pivotTorqueCurrent / batteryVolts;
    inputs.pivotTempCelsius = 0.0;

    inputs.rightRollerConnected = true;
    inputs.rightRollerVelocityRadPerSec = rightRollerSim.getAngularVelocityRadPerSec();
    inputs.rightRollerAppliedVolts = rightVolts;
    inputs.rightRollerSupplyCurrentAmps =
        rightVolts
            * GEARBOX.getCurrent(rightRollerSim.getAngularVelocityRadPerSec(), rightVolts)
            / batteryVolts;

    inputs.leftRollerConnected = true;
    inputs.leftRollerVelocityRadPerSec = leftRollerSim.getAngularVelocityRadPerSec();
    inputs.leftRollerAppliedVolts = leftVolts;
    inputs.leftRollerSupplyCurrentAmps =
        leftVolts
            * GEARBOX.getCurrent(leftRollerSim.getAngularVelocityRadPerSec(), leftVolts)
            / batteryVolts;
  }

  /**
   * What a Talon actually applies for a requested voltage: nothing but the back-EMF while disabled,
   * so the mechanism coasts, and never enough to push more current than its stator limit.
   */
  private static double limitedVolts(
      double requestedVolts, double motorRadPerSec, double statorLimitAmps) {
    double backEmfVolts = motorRadPerSec / GEARBOX.KvRadPerSecPerVolt;
    if (DriverStation.isDisabled()) {
      return backEmfVolts;
    }
    double batteryVolts = RobotController.getBatteryVoltage();
    double limitVolts = statorLimitAmps * GEARBOX.rOhms;
    return MathUtil.clamp(
        MathUtil.clamp(requestedVolts, -batteryVolts, batteryVolts),
        backEmfVolts - limitVolts,
        backEmfVolts + limitVolts);
  }

  @Override
  public void setPivotAngle(double angleRad) {
    goalAngleRad = MathUtil.clamp(angleRad, MIN_ANGLE_RAD, MAX_ANGLE_RAD);
  }

  @Override
  public void setRollerDutyCycle(double dutyCycle) {
    rollerDutyCycle = dutyCycle;
  }
}
