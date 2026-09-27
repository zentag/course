package frc.robot.subsystems.intake;

import static frc.robot.Constants.IntakeConstants.*;
import static frc.robot.util.PhoenixUtil.tryUntilOk;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.SoftwareLimitSwitchConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.DutyCycleOut;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Temperature;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;

public class IntakeIOTalonFX implements IntakeIO {
  private final CANBus canBus = new CANBus(CAN_BUS);
  private final TalonFX pivot = new TalonFX(PIVOT_CAN_ID, canBus);
  private final TalonFX rightRoller = new TalonFX(RIGHT_ROLLER_CAN_ID, canBus);
  private final TalonFX leftRoller = new TalonFX(LEFT_ROLLER_CAN_ID, canBus);

  private final MotionMagicVoltage motionMagicRequest = new MotionMagicVoltage(0);
  private final VoltageOut voltageRequest = new VoltageOut(0);
  private final DutyCycleOut dutyCycleRequest = new DutyCycleOut(0);

  private final StatusSignal<Angle> pivotPosition = pivot.getPosition();
  private final StatusSignal<AngularVelocity> pivotVelocity = pivot.getVelocity();
  private final StatusSignal<Voltage> pivotVolts = pivot.getMotorVoltage();
  private final StatusSignal<Current> pivotTorqueCurrent = pivot.getTorqueCurrent();
  private final StatusSignal<Current> pivotSupplyCurrent = pivot.getSupplyCurrent();
  private final StatusSignal<Temperature> pivotTemp = pivot.getDeviceTemp();

  private final StatusSignal<AngularVelocity> rightVelocity = rightRoller.getVelocity();
  private final StatusSignal<Voltage> rightVolts = rightRoller.getMotorVoltage();
  private final StatusSignal<Current> rightSupplyCurrent = rightRoller.getSupplyCurrent();

  private final StatusSignal<AngularVelocity> leftVelocity = leftRoller.getVelocity();
  private final StatusSignal<Voltage> leftVolts = leftRoller.getMotorVoltage();
  private final StatusSignal<Current> leftSupplyCurrent = leftRoller.getSupplyCurrent();

  // Homing drives into the retracted hard stop until the pivot stops moving, then zeros there
  private static final double homingVolts = -0.07 * 12.0; // 2910 homes at -7% duty
  private static final double homingVelocityThreshold = Units.degreesToRadians(0.5); // rad/s
  private static final double homingMinRunSeconds = 0.5; // Time to start moving before stalling
  private static final double homingSettleSeconds = 0.1;
  private boolean homed = false;
  private double homingStartSeconds = Double.NaN;
  private double stalledSinceSeconds = Double.NaN;

  public IntakeIOTalonFX() {
    var pivotConfig = new TalonFXConfiguration();
    pivotConfig.Feedback.SensorToMechanismRatio = GEAR_RATIO;
    pivotConfig.MotorOutput.NeutralMode = NeutralModeValue.Coast;
    pivotConfig.MotorOutput.Inverted = InvertedValue.Clockwise_Positive;
    pivotConfig.CurrentLimits.StatorCurrentLimit = PIVOT_STATOR_CURRENT_LIMIT_AMPS;
    pivotConfig.CurrentLimits.StatorCurrentLimitEnable = true;
    pivotConfig.CurrentLimits.SupplyCurrentLimit = PIVOT_SUPPLY_CURRENT_LIMIT_AMPS;
    pivotConfig.CurrentLimits.SupplyCurrentLimitEnable = true;

    // Talon gains are per rotation, ours are per radian
    pivotConfig.Slot0.kP = KP * 2.0 * Math.PI;
    pivotConfig.Slot0.kV = KV * 2.0 * Math.PI;
    pivotConfig.MotionMagic.MotionMagicCruiseVelocity =
        Units.radiansToRotations(MAX_VELOCITY_RAD_PER_SEC);
    pivotConfig.MotionMagic.MotionMagicAcceleration =
        Units.radiansToRotations(MAX_ACCELERATION_RAD_PER_SEC_SQ);
    tryUntilOk(5, () -> pivot.getConfigurator().apply(pivotConfig, 0.25));

    // The rollers are mounted mirrored, so they are inverted opposite to each other
    configureRoller(rightRoller, InvertedValue.CounterClockwise_Positive);
    configureRoller(leftRoller, InvertedValue.Clockwise_Positive);

    // Only the signals we actually log need to be on the bus
    BaseStatusSignal.setUpdateFrequencyForAll(
        50.0,
        pivotPosition,
        pivotVelocity,
        pivotVolts,
        pivotTorqueCurrent,
        pivotSupplyCurrent,
        pivotTemp,
        rightVelocity,
        rightVolts,
        rightSupplyCurrent,
        leftVelocity,
        leftVolts,
        leftSupplyCurrent);
    tryUntilOk(5, () -> pivot.optimizeBusUtilization(0, 0.25));
    tryUntilOk(5, () -> rightRoller.optimizeBusUtilization(0, 0.25));
    tryUntilOk(5, () -> leftRoller.optimizeBusUtilization(0, 0.25));
  }

  private static void configureRoller(TalonFX roller, InvertedValue inverted) {
    var config = new TalonFXConfiguration();
    config.MotorOutput.NeutralMode = NeutralModeValue.Coast;
    config.MotorOutput.Inverted = inverted;
    config.CurrentLimits.StatorCurrentLimit = ROLLER_STATOR_CURRENT_LIMIT_AMPS;
    config.CurrentLimits.StatorCurrentLimitEnable = true;
    config.CurrentLimits.SupplyCurrentLimit = ROLLER_SUPPLY_CURRENT_LIMIT_AMPS;
    config.CurrentLimits.SupplyCurrentLowerLimit = ROLLER_SUPPLY_CURRENT_LOWER_LIMIT_AMPS;
    config.CurrentLimits.SupplyCurrentLimitEnable = true;
    tryUntilOk(5, () -> roller.getConfigurator().apply(config, 0.25));
  }

  @Override
  public void updateInputs(IntakeIOInputs inputs) {
    inputs.homed = homed;

    inputs.pivotConnected =
        BaseStatusSignal.refreshAll(
                pivotPosition,
                pivotVelocity,
                pivotVolts,
                pivotTorqueCurrent,
                pivotSupplyCurrent,
                pivotTemp)
            .isOK();
    inputs.rightRollerConnected =
        BaseStatusSignal.refreshAll(rightVelocity, rightVolts, rightSupplyCurrent).isOK();
    inputs.leftRollerConnected =
        BaseStatusSignal.refreshAll(leftVelocity, leftVolts, leftSupplyCurrent).isOK();

    // Signals report mechanism rotations, convert to radians at the IO boundary
    inputs.pivotPositionRad = Units.rotationsToRadians(pivotPosition.getValueAsDouble());
    inputs.pivotVelocityRadPerSec = Units.rotationsToRadians(pivotVelocity.getValueAsDouble());
    inputs.pivotAppliedVolts = pivotVolts.getValueAsDouble();
    inputs.pivotTorqueCurrentAmps = pivotTorqueCurrent.getValueAsDouble();
    inputs.pivotSupplyCurrentAmps = pivotSupplyCurrent.getValueAsDouble();
    inputs.pivotTempCelsius = pivotTemp.getValueAsDouble();

    inputs.rightRollerVelocityRadPerSec =
        Units.rotationsToRadians(rightVelocity.getValueAsDouble());
    inputs.rightRollerAppliedVolts = rightVolts.getValueAsDouble();
    inputs.rightRollerSupplyCurrentAmps = rightSupplyCurrent.getValueAsDouble();

    inputs.leftRollerVelocityRadPerSec = Units.rotationsToRadians(leftVelocity.getValueAsDouble());
    inputs.leftRollerAppliedVolts = leftVolts.getValueAsDouble();
    inputs.leftRollerSupplyCurrentAmps = leftSupplyCurrent.getValueAsDouble();
  }

  @Override
  public void home() {
    if (homed) return;

    // Do not want to home in disabled
    if (DriverStation.isDisabled()) {
      homingStartSeconds = Double.NaN;
      stalledSinceSeconds = Double.NaN;
      return;
    }

    double now = Timer.getFPGATimestamp();
    if (Double.isNaN(homingStartSeconds)) {
      homingStartSeconds = now;
      // The soft limits would stop the pivot before it reaches the hard stop
      setSoftLimits(false);
    }
    pivot.setControl(voltageRequest.withOutput(homingVolts));

    boolean stalled =
        now - homingStartSeconds >= homingMinRunSeconds
            && Math.abs(Units.rotationsToRadians(pivotVelocity.getValueAsDouble()))
                <= homingVelocityThreshold;
    if (!stalled) {
      stalledSinceSeconds = Double.NaN;
      return;
    }
    if (Double.isNaN(stalledSinceSeconds)) {
      stalledSinceSeconds = now;
    }
    if (now - stalledSinceSeconds < homingSettleSeconds) {
      return;
    }

    pivot.setControl(voltageRequest.withOutput(0.0));
    pivot.setPosition(Units.radiansToRotations(HOMED_ANGLE_RAD));
    setSoftLimits(true);
    homed = true;
  }

  private void setSoftLimits(boolean enabled) {
    pivot
        .getConfigurator()
        .apply(
            new SoftwareLimitSwitchConfigs()
                .withForwardSoftLimitThreshold(Units.radiansToRotations(MAX_ANGLE_RAD))
                .withReverseSoftLimitThreshold(Units.radiansToRotations(MIN_ANGLE_RAD))
                .withForwardSoftLimitEnable(enabled)
                .withReverseSoftLimitEnable(enabled));
  }

  @Override
  public void setPivotAngle(double angleRad) {
    pivot.setControl(motionMagicRequest.withPosition(Units.radiansToRotations(angleRad)));
  }

  @Override
  public void setRollerDutyCycle(double dutyCycle) {
    rightRoller.setControl(dutyCycleRequest.withOutput(dutyCycle));
    leftRoller.setControl(dutyCycleRequest.withOutput(dutyCycle));
  }
}
