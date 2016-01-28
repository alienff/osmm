package com.alienff.osmm.server.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author mike
 * @since 28.01.2016 12:04
 */
public class Track {
    private List<Point> points = new ArrayList<>();

    public Track() {
    }

    public Track(Point... points) {
        this.points = new ArrayList<>(points.length);
        Collections.addAll(this.points, points);
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = Objects.requireNonNull(points);
    }
}
