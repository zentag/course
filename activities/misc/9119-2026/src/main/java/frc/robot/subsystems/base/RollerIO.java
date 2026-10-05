package frc.robot.subsystems.base;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import org.littletonrobotics.junction.AutoLog;

/** MotorIO for all motor implementations, used by all BaseSubsystems that require a motor */
public interface RollerIO {
  @AutoLog
  class RollerIOInputs {
    public boolean connected = false;

    // Uses talon.getRotor...() that returns values dependent on only the motor
    public double motorRawPositionRotations = 0.0;
    public double motorVelocityRPS = 0.0;
    public double motorAccelerationRotationsPerSecondPerSecond = 0.0;
    public double motorVoltage = 0.0;
    public double motorStatorCurrentAmps = 0.0;
    public double motorSupplyCurrentAmps = 0.0;
    public double motorTemperatureC = 0.0;

    // Uses talon.get...() that returns values dependent on rotorToSensorRatio and
    // sensorToMechanismRatio
    // The units that the mechanism uses will be defined in its config
    public double mechanismRawPositionInMechanismUnits = 0.0;
    public double mechanismVelocityPerSecondInMechanismUnits = 0.0;
    public double mechanismAccelerationPerSecondPerSecondInMechanismUnits = 0.0;
  }

  default void updateInputs(RollerIOInputs inputs) {}

  default int getCanID() {
    return 0;
  }

  default void setOpenLoopDutyCycle(double dutyCycle) {}

  default void follow(int leaderID, MotorAlignmentValue motorAlignment) {}

  default void setConfigs(TalonFXConfiguration configs) {}

  default void setMotionMagicVelocity(double mechanismVelocity) {}
}
