package examples;

import com.rewedigital.schema.validator.annotation.Schemas;
import com.rewedigital.schema.validator.annotation.ValidateModel;
import com.rewedigital.schema.validator.annotation.ValidateModels;
import com.rewedigital.schema.validator.runner.SchemaValidator;
import org.junit.runner.RunWith;

import java.util.Collection;

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



