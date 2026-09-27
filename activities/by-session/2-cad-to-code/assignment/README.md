# Subsystems and States

make another file in this directory called "submission.md". look at your team's robot from last year (or pick another if you prefer) and divide it up into subsystems. brainstorm what states each subsystem might have, and what states the robot itself might have. an example is below, but it would benefit you to come up with more states than that. what states were not available on your robot but could be with the addition of some code or a sensor? submit a PR when you're done

# assignment 2

## Superstructure

- scoring
    - intake: stowed
    - shooter: shooting
    - swerve: braking
- idle
    - intake: stowed
    - shooter: idle
    - swerve: idle
- intaking
    - intake: intaking
    - shooter: idle
    - swerve: teleoperated
- ...
    - ...
    - ...
## Intake

- stowed
- deployed
- intaking
- outtaking
- ...

## Shooter

- spinning up
- shooting
- idle
- ...

## Swerve

- teleoperated
- path-following
- braking
- idle
- ...
