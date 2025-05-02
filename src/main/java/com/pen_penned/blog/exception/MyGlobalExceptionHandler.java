package com.pen_penned.blog.exception;

import com.pen_penned.blog.payload.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class MyGlobalExceptionHandler {

    // Handle MethodArgumentNotValidException (e.g., validation errors)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(err -> {
            String fieldName = ((FieldError) err).getField();
            String message = err.getDefaultMessage();
            errors.put(fieldName, message);
        });

        APIResponse response = new APIResponse("Validation failed", false, errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    // Handle ImageProcessingException with custom status and error response
    @ExceptionHandler(ImageProcessingException.class)
    public ResponseEntity<APIResponse> handleImageProcessingException(ImageProcessingException ex) {
        log.error("Image processing error: {}", ex.getMessage(), ex);

        HttpStatus status;
        String message = switch (ex.getCode()) {
            case "INVALID_FORMAT" -> {
                status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
                yield "Invalid image format";
            }
            case "PROCESSING_FAILED" -> {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                yield "Failed to process image";
            }
            case "UPLOAD_FAILED" -> {
                status = HttpStatus.BAD_REQUEST;
                yield "Failed to upload image";
            }
            default -> {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                yield "Unexpected error during image processing";
            }
        };

        return ResponseEntity
                .status(status)
                .body(new APIResponse(message, false));
    }

    // Handle OAuth2AuthenticationProcessingException
    @ExceptionHandler(OAuth2AuthenticationProcessingException.class)
    public ResponseEntity<APIResponse> handleOAuth2AuthenticationProcessingException(
            OAuth2AuthenticationProcessingException ex) {
        log.error("OAuth2 authentication error: {}", ex.getMessage(), ex);

        Map<String, String> details = new HashMap<>();
        if (ex.getProviderId() != null) {
            details.put("providerId", ex.getProviderId());
        }
        if (ex.getErrorCode() != null) {
            details.put("errorCode", ex.getErrorCode());
        }

        APIResponse response = details.isEmpty()
                ? new APIResponse("Authentication failed: " + ex.getMessage(), false)
                : new APIResponse("Authentication failed: " + ex.getMessage(), false, details);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    // Handle MaxUploadSizeExceededException (file size exceeded)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<APIResponse> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        log.error("File size exceeded: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new APIResponse("File size exceeds the maximum allowed limit", false));
    }

    // Handle ResourceNotFoundException (for resources not found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("Resource not found: {}", e.getMessage());
        return new ResponseEntity<>(new APIResponse(e.getMessage(), false), HttpStatus.NOT_FOUND);
    }

    // Handle APIException (generic API exceptions)
    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse> handleAPIException(APIException e) {
        log.error("API error: {}", e.getMessage());
        return new ResponseEntity<>(new APIResponse(e.getMessage(), false), HttpStatus.BAD_REQUEST);
    }

    // Handle ResourceAlreadyExistsException (when resource already exists)
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<APIResponse> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException exception) {
        log.error("Resource already exists: {}", exception.getMessage());
        return new ResponseEntity<>(new APIResponse(exception.getMessage(), false), HttpStatus.CONFLICT);
    }

    // Handle general exceptions and send a generic response
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse> handleGeneralException(Exception ex) {
        log.error("An error occurred: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(new APIResponse("An unexpected error occurred", false),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
