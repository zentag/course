# checklist for implementing a subystem with advantagekit

## required fields

### interface ([name]IO.java)
- class called [name]Inputs with @AutoLog annotation
### subsystem
- [name]IO called io (passed through constructor)
- [name]IOInputsAutoLogged called inputs (generated, imported)

## required method calls in periodic
- io.updateInputs(inputs);
- Logger.processInputs("[name]", inputs);


## required method to be implemented on each [name]IO[implementation].java
```
public void updateInputs([name]IOInputs inputs){
    inputs.exampleInput = exampleValue;
}
```
