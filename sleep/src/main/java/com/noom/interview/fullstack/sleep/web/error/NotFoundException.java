package com.noom.interview.fullstack.sleep.web.error;

/** Thrown when a requested resource does not exist. Mapped to HTTP 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
