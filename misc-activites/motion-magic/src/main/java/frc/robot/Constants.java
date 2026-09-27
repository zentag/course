// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.RobotBase;

/**
 * This class defines the runtime mode used by AdvantageKit. The mode is always "real" when running
 * on a roboRIO. Change the value of "simMode" to switch between "sim" (physics sim) and "replay"
 * (log replay from a file).
 */
public final class Constants {
  public static final Mode simMode = Mode.SIM;
  public static final Mode currentMode = RobotBase.isReal() ? Mode.REAL : simMode;

  public static enum Mode {
    /** Running on a real robot. */
    REAL,

    /** Running a physics simulator. */
    SIM,

    /** Replaying from a log file. */
    REPLAY
  }

  /**
   * Intake, using the numbers from 2910's 2026 robot. The pivot angle is measured from the
   * retracted hard stop and increases as the intake deploys out of the frame.
   */
  public static final class IntakeConstants {
    public static final String CAN_BUS = "CANivore";
    public static final int PIVOT_CAN_ID = 14;
    public static final int RIGHT_ROLLER_CAN_ID = 15;
    public static final int LEFT_ROLLER_CAN_ID = 16;

    public static final DCMotor GEARBOX = DCMotor.getKrakenX60Foc(1);

    // Rotor rotations per pivot rotation
    public static final double GEAR_RATIO =
        (36.0 / 12.0) * (26.0 / 26.0) * (64.0 / 16.0) * (30.0 / 9.0);

    /** What the encoder reads when the intake is against the retracted hard stop. */
    public static final double HOMED_ANGLE_RAD = Units.degreesToRadians(-2.0);

    // Software limits, and the two positions the intake moves between
    public static final double MIN_ANGLE_RAD = Units.degreesToRadians(0.0);
    public static final double MAX_ANGLE_RAD = Units.degreesToRadians(114.3);
    public static final double DEPLOYED_ANGLE_RAD = MAX_ANGLE_RAD;
    public static final double STOWED_ANGLE_RAD = Units.degreesToRadians(10.0);

    // Motion Magic limits. The cruise velocity is 5500 rpm at the motor
    public static final double MAX_VELOCITY_RAD_PER_SEC =
        Units.rotationsPerMinuteToRadiansPerSecond(5500.0) / GEAR_RATIO;
    public static final double MAX_ACCELERATION_RAD_PER_SEC_SQ = Units.degreesToRadians(4000.0);

    // Gains, in volts per radian and volts per rad/s. The feedforward is only kV, from motor free
    // speed, and there is no gravity compensation
    public static final double KP = 5.0 * GEAR_RATIO / (2.0 * Math.PI); // 5 V per rotor rotation
    public static final double KV = 12.0 / (GEARBOX.freeSpeedRadPerSec / GEAR_RATIO);

    // Current limits, which the sims apply too
    public static final double PIVOT_STATOR_CURRENT_LIMIT_AMPS = 75.0;
    public static final double PIVOT_SUPPLY_CURRENT_LIMIT_AMPS = 30.0;
    public static final double ROLLER_STATOR_CURRENT_LIMIT_AMPS = 120.0;
    public static final double ROLLER_SUPPLY_CURRENT_LIMIT_AMPS = 100.0;
    public static final double ROLLER_SUPPLY_CURRENT_LOWER_LIMIT_AMPS = 80.0;

    // 2910's default for mechanisms that haven't been measured (a 1 kg intake with a 4 cm radius)
    public static final double MOMENT_OF_INERTIA_KG_METERS_SQUARED = 0.016;
  }
}
