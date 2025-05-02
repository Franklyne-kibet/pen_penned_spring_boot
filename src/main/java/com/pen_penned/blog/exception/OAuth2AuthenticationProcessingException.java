package com.pen_penned.blog.exception;

import lombok.Getter;

import javax.naming.AuthenticationException;
import java.io.Serial;

/**
 * Exception thrown during OAuth2 authentication processing.
 * <p>
 * This exception is used when an error occurs during OAuth2 authentication flow,
 * such as invalid token, missing required attributes, or provider-specific issues.
 *
 * @author pen_penned
 * @version 1.0
 * @since 1.0
 */
@Getter
public class OAuth2AuthenticationProcessingException extends AuthenticationException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * -- GETTER --
     * Gets the provider ID associated with this exception.
     */
    private final String providerId;
    /**
     * -- GETTER --
     * Gets the error code associated with this exception.
     */
    private final String errorCode;

    /**
     * Constructs an OAuth2AuthenticationProcessingException with the specified error message.
     *
     * @param msg the detail message
     */
    public OAuth2AuthenticationProcessingException(String msg) {
        super(msg);
        this.providerId = null;
        this.errorCode = null;
    }

    /**
     * Constructs an OAuth2AuthenticationProcessingException with the specified error message and cause.
     *
     * @param msg   the detail message
     * @param cause the cause of this exception
     */
    public OAuth2AuthenticationProcessingException(String msg, Throwable cause) {
        super(msg);
        this.providerId = null;
        this.errorCode = null;
        initCause(cause);
    }

    /**
     * Constructs an OAuth2AuthenticationProcessingException with the specified error message,
     * provider ID, and error code.
     *
     * @param msg        the detail message
     * @param providerId the ID of the OAuth2 provider (e.g., "google", "GitHub")
     * @param errorCode  the provider-specific error code
     */
    public OAuth2AuthenticationProcessingException(String msg, String providerId, String errorCode) {
        super(msg);
        this.providerId = providerId;
        this.errorCode = errorCode;
    }

    /**
     * Constructs an OAuth2AuthenticationProcessingException with the specified error message,
     * provider ID, error code, and cause.
     *
     * @param msg        the detail message
     * @param providerId the ID of the OAuth2 provider (e.g., "google", "GitHub")
     * @param errorCode  the provider-specific error code
     * @param cause      the cause of this exception
     */
    public OAuth2AuthenticationProcessingException(String msg, String providerId, String errorCode, Throwable cause) {
        super(msg);
        this.providerId = providerId;
        this.errorCode = errorCode;
        initCause(cause);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append(": ").append(getMessage());

        if (providerId != null) {
            sb.append(" [providerId=").append(providerId).append("]");
        }

        if (errorCode != null) {
            sb.append(" [errorCode=").append(errorCode).append("]");
        }

        return sb.toString();
    }
}
