package com.example.investhub.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String identifier;

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(String.format("%s not found: %s", resourceType, identifier));
        this.resourceType = resourceType;
        this.identifier = identifier;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getIdentifier() {
        return identifier;
    }
}

