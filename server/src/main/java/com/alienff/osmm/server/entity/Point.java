package com.alienff.osmm.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.time.Instant;

/**
 * @author mike
 * @since 17.01.2016 23:41
 */
@Entity
@Table(name = "point")
public class Point extends AbstractEntity {
    private User user;
    private Double lat;
    private Double lon;
    private Instant timestamp;
    private Double hdop;
    private Double altitude;
    private Double speed;
    private Double bearing;

    @Column(name = "lat")
    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    @Column(name = "lon")
    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Column(name = "timestamp")
    @Deprecated
    @JsonProperty("timestamp")
    public Long getTimestampRaw() {
        return timestamp.toEpochMilli();
    }

    public void setTimestampRaw(Long timestampRaw) {
        timestamp = Instant.ofEpochMilli(timestampRaw);
    }

    @Transient
    @JsonIgnore
    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Column(name = "hdop")
    public Double getHdop() {
        return hdop;
    }

    public void setHdop(Double hdop) {
        this.hdop = hdop;
    }

    @Column(name = "altitude")
    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    @Column(name = "speed")
    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    @Column(name = "bearing")
    public Double getBearing() {
        return bearing;
    }

    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + lat + ", " + lon + ")";
    }
}
