package com.noom.interview.fullstack.sleep.web.error;

import java.time.OffsetDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Consistent error body returned for every non-2xx response. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors
) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(OffsetDateTime.now(), status, error, message, null);
    }

    public static ApiError of(int status, String error, String message, Map<String, String> fieldErrors) {
        return new ApiError(OffsetDateTime.now(), status, error, message, fieldErrors);
    }
}
