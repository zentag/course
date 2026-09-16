package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Intake extends SubsystemBase {

private final TalonFX intakeMotor = new TalonFX(1);

    public Intake(){}
    public void intake(){
        intakeMotor.set(1);
    }
    public void eject(){
        intakeMotor.set(-1);
    }
    public void stop() {
        intakeMotor.set(0);
    }

}
