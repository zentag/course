// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.subsystems.Intake;

public class RobotContainer {
        private final Intake intake;
        private final CommandXboxController controller = new CommandXboxController(port:1);
        public RobotContainer() {
                intake = newIntake();
                controller.x().whileTrue(Commands.startEnd(intake::intake, intake::stop, intake));
        }
        public Intake getIntake() {
                return intake;
        }
}
