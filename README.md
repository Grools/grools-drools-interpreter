# GROOLS drools interpreter

## Query shell interpreter
Invoking the jar file with the option `-q` enable the query shell interpreter once reasoning and reporting is done.
It is a minimalist shell and command interpreter to display final state of values.

Supported command are:
```
> get [type] where [field] [symbol] [constraint]
> quit
```
Type:
- concepts
- prior-knowledges
- relations
- observations

Field:
- name
- source

Symbol:
- `==`
- `!=`

Constraint: any id/name

Example:

```
> get prior-knowledges where name == GenProp1071
PriorKnowledge(
    name                       = GenProp1071
    source                     = Genome Properties v3.2
    prediction                 = {{t},{f}}
    expectation                = {{f}}
    conclusion                 = CONFIRMED_ABSENCE
    isDispensable              = false
    isSpecific                 = false
)
```
