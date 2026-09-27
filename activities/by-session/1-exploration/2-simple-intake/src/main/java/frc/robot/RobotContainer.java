// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.subsystems.Intake;

public class RobotContainer {
        private final Intake intake;
        public RobotContainer() {
                intake = new Intake();
        }
        public Intake getIntake() {
                return intake;
        }
}
