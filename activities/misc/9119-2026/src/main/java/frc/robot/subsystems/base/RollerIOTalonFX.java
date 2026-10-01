package frc.robot.subsystems.base;

import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.StatusSignalCollection;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.DutyCycleOut;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.MotionMagicVelocityVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import edu.wpi.first.units.measure.*;

public class RollerIOTalonFX implements RollerIO {
  private final TalonFX talon;

  // https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/api-usage/control-requests.html
  private final DutyCycleOut dutyCycleRequest = new DutyCycleOut(0);
  private final MotionMagicVelocityVoltage motionMagicVeloRequest =
      new MotionMagicVelocityVoltage(0);

  // https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/api-usage/status-signals.html
  private final StatusSignalCollection signals = new StatusSignalCollection();
  private final StatusSignal<Angle> rawRotorPositionSignal;
  private final StatusSignal<AngularVelocity> rotorVelocitySignal;
  private final StatusSignal<Current> currentStatorSignal;
  private final StatusSignal<Current> currentSupplySignal;
  private final StatusSignal<Voltage> voltageSignal;
  private final StatusSignal<Temperature> temperatureSignal;
  private final StatusSignal<Angle> rawMechanismPositionSignal;
  private final StatusSignal<AngularVelocity> mechanismVelocitySignal;
  private final StatusSignal<AngularAcceleration> mechanismAccelerationSignal;

  private TalonFXConfiguration config = new TalonFXConfiguration();

  public RollerIOTalonFX(int deviceId) {
    talon = new TalonFX(deviceId);

    rawRotorPositionSignal = talon.getRotorPosition();
    rotorVelocitySignal = talon.getRotorVelocity();
    voltageSignal = talon.getMotorVoltage();
    currentStatorSignal = talon.getStatorCurrent();
    currentSupplySignal = talon.getSupplyCurrent();
    temperatureSignal = talon.getDeviceTemp();
    rawMechanismPositionSignal = talon.getPosition();
    mechanismVelocitySignal = talon.getVelocity();
    mechanismAccelerationSignal = talon.getAcceleration();
    // Add all signals to the collection for easy refreshing in refreshData()
    signals.addSignals(
        rawRotorPositionSignal,
        rotorVelocitySignal,
        voltageSignal,
        currentStatorSignal,
        currentSupplySignal,
        temperatureSignal,
        rawMechanismPositionSignal,
        mechanismVelocitySignal,
        mechanismAccelerationSignal);
    signals.setUpdateFrequencyForAll(100.0); // 100 hz
    talon.optimizeBusUtilization();
  }

  public void updateInputs(RollerIOInputs inputs) {
    signals.refreshAll();

    inputs.motorRawPositionRotations = rawRotorPositionSignal.getValueAsDouble();
    inputs.motorStatorCurrentAmps = currentStatorSignal.getValueAsDouble();
    inputs.motorSupplyCurrentAmps = currentSupplySignal.getValueAsDouble();
    inputs.motorVoltage = voltageSignal.getValueAsDouble();
    inputs.motorTemperatureC = temperatureSignal.getValueAsDouble();
    inputs.mechanismAccelerationPerSecondPerSecondInMechanismUnits =
        mechanismAccelerationSignal.getValueAsDouble();
    inputs.motorVelocityRPS = rotorVelocitySignal.getValueAsDouble();
    inputs.mechanismRawPositionInMechanismUnits = rawMechanismPositionSignal.getValueAsDouble();
    inputs.mechanismVelocityPerSecondInMechanismUnits = mechanismVelocitySignal.getValueAsDouble();
    // No talon.getRotor...() for rotorAcceleration
    inputs.motorAccelerationRotationsPerSecondPerSecond =
        inputs.mechanismAccelerationPerSecondPerSecondInMechanismUnits
            * (config.Feedback.RotorToSensorRatio * config.Feedback.SensorToMechanismRatio);
    inputs.connected = signals.isAllGood();
  }

  public int getCanID() {
    return talon.getDeviceID();
  }

  public void setConfigs(TalonFXConfiguration configs) {
    talon.getConfigurator().apply(configs);
  }

  public void setOpenLoopDutyCycle(double dutyCycle) {
    talon.setControl(dutyCycleRequest.withOutput(dutyCycle));
  }

  public void follow(int leaderID, MotorAlignmentValue motorAlignment) {
    talon.setControl(new Follower(leaderID, motorAlignment));
  }

  public void setMotionMagicVelocity(double mechanismVelocity) {
    talon.setControl(motionMagicVeloRequest.withVelocity(mechanismVelocity));
  }
}
