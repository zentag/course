package frc.robot.subsystems.fuel;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.base.RollerIO;
import frc.robot.subsystems.base.RollerIOInputsAutoLogged;
import org.littletonrobotics.junction.Logger;

public class Fuel extends SubsystemBase {
  private RollerIO intakeIO, feederIO, shooterIO, shooterFollowerIO;
  private RollerIOInputsAutoLogged intakeInputs, feederInputs, shooterInputs, shooterFollowerInputs;

  public boolean isSpunUp = false;
  public double shooterTarget = 60;

  public Fuel(
      RollerIO intakeIO, RollerIO feederIO, RollerIO shooterIO, RollerIO shooterFollowerIO) {

    this.intakeIO = intakeIO;
    this.feederIO = feederIO;
    this.shooterIO = shooterIO;
    this.shooterFollowerIO = shooterFollowerIO;

    this.intakeInputs = new RollerIOInputsAutoLogged();
    this.feederInputs = new RollerIOInputsAutoLogged();
    this.shooterInputs = new RollerIOInputsAutoLogged();
    this.shooterFollowerInputs = new RollerIOInputsAutoLogged();

    var configs = new TalonFXConfiguration();
    var slot0 = configs.Slot0;
    slot0.kS = 0.25; // Add 0.25 V output to overcome static friction
    slot0.kV = 0.12; // A velocity target of 1 rps results in 0.12 V output
    slot0.kA = 0.01; // An acceleration of 1 rps/s requires 0.01 V output
    slot0.kP = 0.11; // An error of 1 rps results in 0.11 V output
    slot0.kI = 0; // no output for integrated error
    slot0.kD = 0; // no output for error derivative

    // set Motion Magic Velocity settings
    var motionMagicConfigs = configs.MotionMagic;
    motionMagicConfigs.MotionMagicAcceleration =
        400; // Target acceleration of 400 rps/s (0.25 seconds to max)
    motionMagicConfigs.MotionMagicJerk = 4000; // Target jerk of 4000 rps/s/s (0.1 seconds)

    shooterIO.setConfigs(configs);

    // https://api.ctr-electronics.com/phoenix6/stable/java/com/ctre/phoenix6/controls/package-summary.html
    shooterFollowerIO.follow(shooterIO.getCanID(), MotorAlignmentValue.Aligned);

    setDefaultCommand(requestState(WantedState.IDLE));
  }

  public enum WantedState {
    SHOOT,
    INTAKE,
    IDLE,
    OUTTAKE
  }

  private enum SystemState {
    SHOOTING,
    SPINNING_UP,
    INTAKING,
    IDLING,
    OUTTAKING
  }

  private WantedState wantedState = WantedState.IDLE;
  private SystemState systemState = SystemState.IDLING;

  private void handleStateTransitions() {
    systemState =
        switch (wantedState) {
          case SHOOT -> {
            if (isSpunUp) yield SystemState.SHOOTING;
            else yield SystemState.SPINNING_UP;
          }
          case INTAKE -> SystemState.INTAKING;
          case IDLE -> SystemState.IDLING;
          case OUTTAKE -> SystemState.OUTTAKING;
        };
  }

  private void applyState() {
    switch (systemState) {
      case SHOOTING:
        intakeIO.setOpenLoopDutyCycle(-.3);
        feederIO.setOpenLoopDutyCycle(-.4);
        shooterIO.setMotionMagicVelocity(shooterTarget);
        break;
      case SPINNING_UP:
        intakeIO.setOpenLoopDutyCycle(0);
        feederIO.setOpenLoopDutyCycle(0);
        shooterIO.setMotionMagicVelocity(shooterTarget);
        break;
      case INTAKING:
        intakeIO.setOpenLoopDutyCycle(-.7);
        feederIO.setOpenLoopDutyCycle(.4);
        shooterIO.setOpenLoopDutyCycle(0);
        break;
      case IDLING:
        intakeIO.setOpenLoopDutyCycle(0);
        feederIO.setOpenLoopDutyCycle(0);
        shooterIO.setOpenLoopDutyCycle(0);
        break;
      case OUTTAKING:
        intakeIO.setOpenLoopDutyCycle(.4);
        feederIO.setOpenLoopDutyCycle(-.4);
        shooterIO.setOpenLoopDutyCycle(0);
        break;
    }
  }

  public Command requestState(WantedState requestedState) {
    return this.run(() -> wantedState = requestedState);
  }

  @Override
  public void periodic() {

    intakeIO.updateInputs(intakeInputs);
    feederIO.updateInputs(feederInputs);
    shooterIO.updateInputs(shooterInputs);
    shooterFollowerIO.updateInputs(shooterFollowerInputs);

    Logger.processInputs("Intake", intakeInputs);
    Logger.processInputs("Feeder", feederInputs);
    Logger.processInputs("Shooter", shooterInputs);
    Logger.processInputs("ShooterFollower", shooterFollowerInputs);

    Logger.recordOutput("Fuel/WantedState", wantedState);
    Logger.recordOutput("Fuel/SystemState", systemState);

    boolean shooterVelocityAcceptable =
        Math.abs(shooterInputs.mechanismVelocityPerSecondInMechanismUnits - shooterTarget) < 10;
    // check if we are spun up. don't want to change this while we are shooting because a ball could
    // get stuck or misfired
    if (shooterVelocityAcceptable || systemState == SystemState.SHOOTING) isSpunUp = true;
    else isSpunUp = false;
    handleStateTransitions();
    applyState();
  }
}
