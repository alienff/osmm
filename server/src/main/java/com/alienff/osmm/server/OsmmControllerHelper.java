package com.alienff.osmm.server;

import com.alienff.osmm.server.entity.*;

import javax.persistence.TypedQuery;
import java.time.Instant;
import java.util.*;

/**
 * @author mike
 * @since 17.01.2016 14:25
 */
public class OsmmControllerHelper {
    static List<Track> splitToTracks(Iterable<? extends Point> points, int trackDelayThreshold) {
        final List<Track> result = new ArrayList<>();
        Instant last = Instant.MIN;
        Track track = null;
        for (Point point : points) {
            if (point.getTimestamp().isAfter(last.plusSeconds(trackDelayThreshold))) {
                if (track != null) {
                    result.add(track);
                }
                track = new Track();
            }
            if (track != null) {
                track.getPoints().add(point);
            } else {
                throw new IllegalStateException();
            }
            last = point.getTimestamp();
        }
        result.add(track);
        return result;
    }

    static List<Track> fromSinglePoint(Point point) {
        return Collections.singletonList(new Track(point));
    }

    static <Entity> Entity findSingleResult(TypedQuery<Entity> query) {
        query.setMaxResults(1);
        final List<Entity> list = query.getResultList();
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }
}
