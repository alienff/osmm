package com.alienff.osmm.server.entity;

/**
 * @author mike
 * @since 19.01.2016 17:39
 */
public class HttpErrorResponse extends HttpResponse {
    private final String errorCode;
    private final String errorMessage;

    public HttpErrorResponse(String errorCode, String errorMessage) {
        super(false);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
