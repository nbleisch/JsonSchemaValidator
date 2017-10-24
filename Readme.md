### Usage Example

Given the following Json-Schema
```javascript
{
    "title": "Animal",
    "type": "object",
    "properties": {
        "name": {
            "type": "string",
            "minLength": 2
        },
        "type": {
            "enum": [
                "DOG",
                "CHICKEN"
            ]
        },
        "weightInKg": {
            "type": "integer",
            "minimum": 0
        },
        "ageInMonth": {
            "type": "integer",
            "minimum": 0
        },
        "isCarnivore": {
            "type": "boolean"
        }
    },
    "required": [
        "name",
        "type",
        "weightInKG",
        "ageInMonth",
        "isCarnivore"
    ]
}
```

This test data:

```Java
return Lists.newArrayList(
                new Animal("Stuart", CHICKEN, 2, 6, false),
                new Animal(null, CHICKEN, 2, 9, false),
                new Animal("Baxter", DOG, 15, 24, true),
                new Animal("Max", ELEPHANT, 600, 90, false));
```

And this test:

```Java
@RunWith(SchemaValidator.class)
@Schemas("schemas/animals")
public class ValidateAnimalContract {

    @ValidateModels
    public Collection<Animal> validateAnimals() {
        return ZooService.getAnimals();
    }

    @ValidateModel
    public Animal validateAnimal() {
        return ZooService.getAnimals().iterator().next();
    }
}
```

This is going to happen

```
java.lang.Exception: Did not pass validation with /schemas/animals/team_turtlez_animals.schema.json and
{"name":null,"type":"CHICKEN","weightInKG":2,"ageInMonth":9,"isCarnivore":false}.
Reason is: com.github.fge.jsonschema.core.report.ListProcessingReport: failure
--- BEGIN MESSAGES ---
error: instance type (null) does not match any allowed primitive type (allowed: ["string"])
    level: "error"
    schema: {"loadingURI":"#","pointer":"/properties/name"}
    instance: {"pointer":"/name"}
    domain: "validation"
    keyword: "type"
    found: "null"
    expected: ["string"]
---  END MESSAGES  ---

java.lang.Exception: Did not pass validation with /schemas/animals/team_turtlez_animals.schema.json and
{"name":"Max","type":"ELEPHANT","weightInKG":600,"ageInMonth":90,"isCarnivore":false}.
Reason is: com.github.fge.jsonschema.core.report.ListProcessingReport: failure
--- BEGIN MESSAGES ---
error: instance value ("ELEPHANT") not found in enum (possible values: ["DOG","CHICKEN"])
    level: "error"
    schema: {"loadingURI":"#","pointer":"/properties/type"}
    instance: {"pointer":"/type"}
    domain: "validation"
    keyword: "enum"
    value: "ELEPHANT"
    enum: ["DOG","CHICKEN"]
---  END MESSAGES  ---
```