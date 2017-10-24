package com.rewedigital.schema.validator.runner;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static com.github.fge.jackson.JsonLoader.fromResource;

final class SchemaResourceScanner {


    static Map<String, JsonSchema> getSchemaResources(final String path, final JsonSchemaFactory factory) throws IOException, ProcessingException {
        Map<String, JsonSchema> schemaFilenames = new HashMap<>();
        try (
                InputStream in = getResourceAsStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;
            while ((resource = br.readLine()) != null) {
                if (resource.endsWith("json")) {
                    final String resourcePath = String.format("/%s/%s", path, resource);
                    schemaFilenames.put(resourcePath, factory.getJsonSchema(
                            fromResource(resourcePath)
                    ));
                }
            }
        }
        return schemaFilenames;
    }

    private static InputStream getResourceAsStream(String resource) {
        final InputStream in
                = getContextClassLoader().getResourceAsStream(resource);

        return in == null ?
                SchemaResourceScanner.class.getClassLoader().getResourceAsStream(resource) : in;
    }

    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
