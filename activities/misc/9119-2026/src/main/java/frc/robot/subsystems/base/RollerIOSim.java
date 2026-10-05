package frc.robot.subsystems.base;

import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;

public class RollerIOSim implements RollerIO {
  private final DCMotorSim simMotor;

  private double lastUpdateTimestamp = 0.0;
  private double rotorToMechanismRatio = 1;

  public record RollerSimConstants(
      double momentOfIntertia,
      double rotorToSensorRatio,
      double sensorToMechanismRatio,
      double positionStandardDev,
      double velocityStandardDev) {}

  public RollerIOSim(RollerSimConstants constants) {
    rotorToMechanismRatio = constants.rotorToSensorRatio * constants.sensorToMechanismRatio;
    this.simMotor =
        new DCMotorSim(
            LinearSystemId.createDCMotorSystem(
                DCMotor.getKrakenX60Foc(1), constants.momentOfIntertia, rotorToMechanismRatio),
            DCMotor.getKrakenX60Foc(1),
            constants.positionStandardDev,
            constants.velocityStandardDev);
  }

  @Override
  public void updateInputs(RollerIOInputs inputs) {
    double timestamp = Timer.getFPGATimestamp(); // Time since roboRio on or code deploy
    simMotor.update(timestamp - lastUpdateTimestamp);
    lastUpdateTimestamp = timestamp;

    inputs.motorRawPositionRotations = simMotor.getAngularPositionRotations();
    inputs.motorVelocityRPS = simMotor.getAngularVelocityRPM() / 60.0;
    inputs.motorVoltage = simMotor.getInputVoltage();
    inputs.motorSupplyCurrentAmps = simMotor.getCurrentDrawAmps();
    inputs.mechanismRawPositionInMechanismUnits =
        inputs.motorRawPositionRotations / rotorToMechanismRatio;
    inputs.mechanismVelocityPerSecondInMechanismUnits =
        inputs.motorVelocityRPS / rotorToMechanismRatio;
  }

  @Override
  public void setOpenLoopDutyCycle(double dutyCycle) {
    simMotor.setInputVoltage(dutyCycle * 12);
  }

  @Override
  public void setMotionMagicVelocity(double mechanismVelocity) {
    simMotor.setAngularVelocity((mechanismVelocity * rotorToMechanismRatio) * 2 * Math.PI);
  }
}
