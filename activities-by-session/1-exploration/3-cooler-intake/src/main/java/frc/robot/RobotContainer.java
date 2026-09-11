// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.subsystems.Intake;

public class RobotContainer {
        private final Intake intake = new Intake();
        private final CommandXboxController controller = new CommandXboxController(1);
        public RobotContainer() {
                configureBindings();
        }

        public void configureBindings() {
                controller.x().whileTrue(Commands.startEnd(intake::intake,intake::stop,intake));
                controller.b().whileTrue(Commands.startEnd(intake::eject,intake::stop,intake));
        }
        public Intake getIntake() {
                return intake;
        }
}
