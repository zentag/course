package frc.robot.subsystems.intake;

import static frc.robot.Constants.IntakeConstants.*;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;

public class Intake extends SubsystemBase {
  private final IntakeIO io;
  private final IntakeIOInputsAutoLogged inputs = new IntakeIOInputsAutoLogged();

  public Intake(IntakeIO io) {
    this.io = io;
  }

  public enum WantedState {
    HOME,
    DEPLOY,
    INTAKE,
    STOW
  }

  private enum SystemState {
    HOMING,
    DEPLOYING,
    INTAKING,
    STOWING,
  }

  public WantedState wantedState = WantedState.HOME;
  private SystemState systemState = SystemState.HOMING;

  public void setWantedState(WantedState wantedState) {
    this.wantedState = wantedState;
  }

  public void handleStateTransitions() {
    systemState =
        switch (wantedState) {
          case HOME -> SystemState.HOMING;
          case DEPLOY -> SystemState.DEPLOYING;
          case INTAKE -> SystemState.INTAKING;
          case STOW -> SystemState.STOWING;
        };

    // Positions mean nothing until the pivot is homed
    if (!inputs.homed) {
      systemState = SystemState.HOMING;
    }
  }

  public void applyState() {
    switch (systemState) {
      case HOMING -> {
        io.home();
        io.setRollerDutyCycle(0.0);

        if (inputs.homed) {
          systemState = SystemState.DEPLOYING;
          if (wantedState == WantedState.HOME) {
            wantedState = WantedState.DEPLOY;
          }
        }
      }
      case DEPLOYING -> {
        io.setPivotAngle(DEPLOYED_ANGLE_RAD);
        io.setRollerDutyCycle(0.0);
      }
      case INTAKING -> {
        io.setPivotAngle(DEPLOYED_ANGLE_RAD);
        io.setRollerDutyCycle(1.0);
      }
      case STOWING -> {
        io.setPivotAngle(STOWED_ANGLE_RAD);
        io.setRollerDutyCycle(0.15); // Keeps fuel pulled in while retracting
      }
    }
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Intake", inputs);

    handleStateTransitions();
    applyState();

    Logger.recordOutput("Intake/WantedState", wantedState);
    Logger.recordOutput("Intake/SystemState", systemState);
  }
}
