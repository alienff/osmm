package com.alienff.osmm.server.entity;

/**
 * @author mike
 * @since 19.01.2016 17:48
 */
public class Version {
    public static final Version CURRENT = new Version("0.0+");

    private final String version;

    private Version(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
