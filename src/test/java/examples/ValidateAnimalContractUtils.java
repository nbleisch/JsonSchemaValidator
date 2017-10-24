package examples;

import com.google.common.collect.Lists;

import java.util.Collection;

import static examples.Animal.AnimalType.CHICKEN;
import static examples.Animal.AnimalType.DOG;
import static examples.Animal.AnimalType.ELEPHANT;

class Animal {

    enum AnimalType {
        ELEPHANT, DOG, CHICKEN, TIGER;
    }

    Animal(final String name, final AnimalType type, final Integer weight, final Integer age, final Boolean isCarnivore) {
        this.name = name;
        this.type = type;
        this.weightInKG = weight;
        this.ageInMonth = age;
        this.isCarnivore = isCarnivore;
    }

    String name;
    AnimalType type;
    Integer weightInKG;
    Integer ageInMonth;
    Boolean isCarnivore;
}


class ZooService {

    final static Collection<examples.Animal> getAnimals() {
        return Lists.newArrayList(
                new Animal("Stuart", CHICKEN, 2, 6, false),
                new Animal(null, CHICKEN, 2, 9, false),
                new Animal("Baxter", DOG, 15, 24, true),
                new Animal("Max", ELEPHANT, 600, 90, false));
    }
}