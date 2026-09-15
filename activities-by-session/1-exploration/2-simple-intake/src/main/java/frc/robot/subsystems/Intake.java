package frc.robot.subsystems;

import com.ctre.phoenix6.hardware.TalonFX;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Intake extends SubsystemBase {

    public final TalonFX intakeMotor = new TalonFX(deviceid:1);
    public Intake() {
        intakeMotor.set(speed: 0.75);
    }

}
