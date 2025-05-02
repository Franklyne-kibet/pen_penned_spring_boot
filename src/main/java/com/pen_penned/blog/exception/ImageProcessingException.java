package com.pen_penned.blog.exception;

import lombok.Getter;

@Getter
public class ImageProcessingException extends RuntimeException {

    /**
     * -- GETTER --
     * Returns the error code associated with this exception (e.g., "INVALID_FORMAT").
     */
    private final String code;

    public ImageProcessingException(String message, String code) {
        super(message);
        this.code = code;
    }

    public ImageProcessingException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public static ImageProcessingException invalidFormat() {
        return new ImageProcessingException("Invalid image format", "INVALID_FORMAT");
    }

    public static ImageProcessingException processingFailed(Throwable cause) {
        return new ImageProcessingException("Failed to process image", "PROCESSING_FAILED", cause);
    }

    public static ImageProcessingException uploadFailed(Throwable cause) {
        return new ImageProcessingException("Failed to upload image", "UPLOAD_FAILED", cause);
    }

    @Override
    public String toString() {
        return "ImageProcessingException{code='" + code + "', message='" + getMessage() + "'}";
    }

}
