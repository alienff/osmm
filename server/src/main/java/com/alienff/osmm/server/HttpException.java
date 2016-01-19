package com.alienff.osmm.server;

/**
 * @author mike
 * @since 19.01.2016 17:11
 */
public class HttpException extends Exception {
    private final int httpResponseCode;
    private final String messageCode;

    public HttpException(int httpResponseCode, String messageCode) {
        this.httpResponseCode = httpResponseCode;
        this.messageCode = messageCode;
    }

    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    public String getMessageCode() {
        return messageCode;
    }
}
