package com.adamfgcross.nolocksumofsquares.service;

public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 4930771441412127268L;

	public ResourceNotFoundException(String message) {
        super(message);
    }
}