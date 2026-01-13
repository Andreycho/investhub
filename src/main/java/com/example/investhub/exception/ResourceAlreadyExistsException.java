package com.example.investhub.exception;

/**
 * Exception thrown when a resource already exists (conflict).
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    private final String resourceType;
    private final String identifier;

    public ResourceAlreadyExistsException(String resourceType, String identifier) {
        super(String.format("%s already exists: %s", resourceType, identifier));
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

