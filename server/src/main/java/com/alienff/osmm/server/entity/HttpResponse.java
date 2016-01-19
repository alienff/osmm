package com.alienff.osmm.server.entity;

/**
 * @author mike
 * @since 19.01.2016 17:39
 */
public class HttpResponse {
    private final boolean ok;

    public HttpResponse(boolean ok) {
        this.ok = ok;
    }

    public boolean isOk() {
        return ok;
    }
}
