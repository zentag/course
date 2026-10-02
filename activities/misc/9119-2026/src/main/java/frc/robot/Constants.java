// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;
import frc.robot.subsystems.base.RollerIOSim.RollerSimConstants;

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

  public static final class FuelConstants {
    // momentOfInertia (kg*m^2), rotorToSensorRatio, sensorToMechanismRatio, positionStdDev,
    // velocityStdDev
    public static final RollerSimConstants intakeSimConstants =
        new RollerSimConstants(0.001, 1.0, 3.0, 0.0, 0.0);
    public static final RollerSimConstants feederSimConstants =
        new RollerSimConstants(0.0005, 1.0, 4.0, 0.0, 0.0);
    public static final RollerSimConstants shooterSimConstants =
        new RollerSimConstants(0.004, 1.0, 1.0, 0.0, 0.0);
  }
}
