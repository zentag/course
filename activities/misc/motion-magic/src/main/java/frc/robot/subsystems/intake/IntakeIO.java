package frc.robot.subsystems.intake;

import org.littletonrobotics.junction.AutoLog;

/** The intake pivot and its two roller motors, which always spin together. */
public interface IntakeIO {
  @AutoLog
  public static class IntakeIOInputs {
    /** True once the pivot position is known, see {@link #home()}. */
    public boolean homed = false;

    public boolean pivotConnected = false;
    public double pivotPositionRad = 0.0;
    public double pivotVelocityRadPerSec = 0.0;
    public double pivotAppliedVolts = 0.0;
    public double pivotTorqueCurrentAmps = 0.0;
    public double pivotSupplyCurrentAmps = 0.0;
    public double pivotTempCelsius = 0.0;

    public boolean rightRollerConnected = false;
    public double rightRollerVelocityRadPerSec = 0.0;
    public double rightRollerAppliedVolts = 0.0;
    public double rightRollerSupplyCurrentAmps = 0.0;

    public boolean leftRollerConnected = false;
    public double leftRollerVelocityRadPerSec = 0.0;
    public double leftRollerAppliedVolts = 0.0;
    public double leftRollerSupplyCurrentAmps = 0.0;
  }

  public default void updateInputs(IntakeIOInputs inputs) {}

  /** Runs the homing routine. Call every loop until the inputs report homed. */
  public default void home() {}

  /** Moves the pivot to the angle, measured from the retracted position, with a profile. */
  public default void setPivotAngle(double angleRad) {}

  /** Spins both rollers together, from -1 to 1. */
  public default void setRollerDutyCycle(double dutyCycle) {}
}
