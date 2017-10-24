package com.rewedigital.schema.validator.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.rewedigital.schema.validator.annotation.Schemas;
import com.rewedigital.schema.validator.annotation.ValidateModel;
import com.rewedigital.schema.validator.annotation.ValidateModels;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rewedigital.schema.validator.runner.SchemaResourceScanner.getSchemaResources;
import static java.lang.String.format;
import static org.junit.runner.Description.createTestDescription;

public class SchemaValidator extends Runner {

    private final TestClass testClass;
    private Object testInstance;
    private final ObjectMapper objectMapper;
    private final Map<String, JsonSchema> schemas = new HashMap<>();

    public SchemaValidator(Class<?> clazz) throws InitializationError, IOException, ProcessingException, IllegalAccessException, InvocationTargetException, InstantiationException {
        objectMapper = new ObjectMapper();


        Schemas providedSchemas = clazz.getAnnotation(Schemas.class);
        if (providedSchemas == null) {
            throw new InitializationError("Schema location should be specified by using " + Schemas.class.getName() + " annotation");
        } else {
            schemas.putAll(
                    getSchemaResources(providedSchemas.value(), JsonSchemaFactory.byDefault()));

        }
        testClass = new TestClass(clazz);
        testInstance = testClass.getOnlyConstructor().newInstance();
    }

    @Override
    public Description getDescription() {
        return Description.createSuiteDescription(testClass.getName(),
                testClass.getAnnotations());
    }

    @Override
    public void run(final RunNotifier originNotifier) {
        try {
            EnrichedRunNotifier notifier = new EnrichedRunNotifier(originNotifier, testClass);
            List<FrameworkMethod> methods = testClass.getAnnotatedMethods(ValidateModel.class);
            for (FrameworkMethod method : methods) {
                final Description description = createTestDescription(testClass.getJavaClass(), method.getName(), method.getAnnotations());
                notifier.fireTestStarted(description);
                final ValidationResult validationResult = validate(method.invokeExplosively(testInstance));
                if (!validationResult.isValid()) {
                    validationResult.errorMessages
                            .forEach(errorMessage ->
                                    notifier.fail(method, new Exception(errorMessage)));
                }
                notifier.fireTestFinished(description);
            }
            methods = testClass.getAnnotatedMethods(ValidateModels.class);
            for (FrameworkMethod method : methods) {
                final Description description = createTestDescription(testClass.getJavaClass(), method.getName(), method.getAnnotations());
                notifier.fireTestStarted(description);
                try {
                    ((Collection) method.invokeExplosively(testInstance)).forEach(object -> {
                                final ValidationResult validationResult = validate(object);
                                if (!validationResult.isValid()) {
                                    validationResult.errorMessages.forEach(errorMessage -> {
                                        notifier.fail(method, new Exception(errorMessage));
                                    });
                                }

                            }
                    );
                } catch (Exception any) {
                    notifier.fail(method, any);
                }
                notifier.fireTestFinished(description);
            }
        } catch (Throwable any) {
            throw new RuntimeException(any);
        }
    }

    private ValidationResult validate(Object object) {
        ValidationResult verificationResult = new ValidationResult();
        try {
            if (objectMapper.canSerialize(object.getClass())) {
                final String jsonString = objectMapper.writeValueAsString(object);
                for (String schemaString : schemas.keySet()) {
                    final ProcessingReport report = schemas.get(schemaString).
                            validate(JsonLoader.fromString(jsonString));
                    if (!report.isSuccess()) {
                        verificationResult.addErrorMessage(format("Did not pass validation with %s and\n%s.\nReason is: %s",
                                schemaString, jsonString, report));
                    }
                }
            } else {
                verificationResult.addErrorMessage(format("Type %s can't be serialized", object.getClass().toString()));
            }
        } catch (Exception any) {
            throw new RuntimeException(any);
        }
        return verificationResult;
    }


    class ValidationResult {

        Collection<String> errorMessages = new ArrayList<>();

        void addErrorMessage(final String errorMessage) {
            errorMessages.add(errorMessage);
        }

        boolean isValid() {
            return errorMessages.isEmpty();
        }
    }

    class EnrichedRunNotifier extends RunNotifier {
        private RunNotifier origin;
        private TestClass sut;

        EnrichedRunNotifier(final RunNotifier origin, TestClass sut) {
            this.origin = origin;
            this.sut = sut;
        }

        void succeed(FrameworkMethod method) {
            origin.fireTestFinished(createTestDescription(sut.getJavaClass(), method.getName(), method.getAnnotations()));

        }

        void fail(FrameworkMethod method, Throwable throwable) {
            origin.fireTestFailure(
                    new Failure(
                            createTestDescription(sut.getJavaClass(), method.getName(), method.getAnnotations()), throwable));
        }

    }

}
