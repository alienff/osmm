package com.alienff.osmm.server;

import com.alienff.osmm.server.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.*;

import static java.util.Collections.emptySet;

/**
 * @author mike
 * @since 17.01.2016 14:25
 */
@Controller
public class OsmmController {
    private static final Logger log = LoggerFactory.getLogger(OsmmController.class);

    private static final int TRACK_DELAY_THRESHOLD = 60*10; // seconds

    @PersistenceContext
    private EntityManager em;

    @RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Version index() {
        log.debug("Index");
        return Version.CURRENT;
    }

    @RequestMapping(value = "/add", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @Transactional
    public HttpResponse add(@RequestParam String login,
                            @RequestParam String password,
                            @RequestParam double lat,
                            @RequestParam double lon,
                            @RequestParam(required = false) Long timestamp,
                            @RequestParam(required = false) Double hdop,
                            @RequestParam(required = false) Double altitude,
                            @RequestParam(required = false) Double speed,
                            @RequestParam(required = false) Double bearing) throws HttpException {
        log.debug("Add point ({}, {}) for user {}", lat, lon, login);

        final User user = getOrAddUser(login, password);
        final Point point = new Point();

        point.setUser(user);
        point.setLat(lat);
        point.setLon(lon);
        point.setTimestamp(timestamp != null ? Instant.ofEpochMilli(timestamp) : Instant.now()); //todo mike test
        point.setHdop(hdop);
        point.setAltitude(altitude);
        point.setSpeed(speed);
        point.setBearing(bearing);

        em.persist(point);

        return new HttpResponse(true);
    }

    @RequestMapping("/get-all")
    @ResponseBody
    @Transactional(readOnly = true)
    public Map<String, List<Track>> getAll(@RequestParam String key) throws HttpException {
        log.debug("Get all points");
        checkKey(key, true, emptySet());
        final List<Point> allPoints = em.createQuery("select p from Point p", Point.class).getResultList();
        final Map<String, List<Point>> userPoints = new HashMap<>();
        allPoints.forEach(point -> userPoints.computeIfAbsent(point.getUser().getLogin(), o -> new ArrayList<>()).add(point));
        final Map<String, List<Track>> result = new HashMap<>(userPoints.size());
        userPoints.entrySet().forEach(entry -> result.put(entry.getKey(), splitToTracks(entry.getValue())));
        return result;
    }

    @RequestMapping(value = "/get-all", params = "login")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<Track> getAllForUser(@RequestParam String login, @RequestParam String key) throws HttpException {
        log.debug("Get all points for user {}", login);
        checkKey(key, false, Collections.singleton(login));
        final List<Point> points = em.createQuery("select p from Point p where p.user.login = :login", Point.class)
                .setParameter("login", login).getResultList();
        return splitToTracks(points);
    }

    private static List<Track> splitToTracks(Iterable<? extends Point> points) {
        final List<Track> result = new ArrayList<>();
        Instant last = Instant.MIN;
        Track track = null;
        for (Point point : points) {
            if (point.getTimestamp().isAfter(last.plusSeconds(TRACK_DELAY_THRESHOLD))) {
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

    private User getOrAddUser(String login, String password) throws HttpException {
        if (login.length() == 0 || password.length() == 0) {
            throw new HttpException(401, "login.password.nonnull");
        }
        final List<User> existingUsers = em.createQuery("select u from User u where u.login = :login", User.class)
                .setParameter("login", login).setMaxResults(1).getResultList();
        final User user;
        if (existingUsers.size() > 0) { // Existing user, check pwd
            user = existingUsers.get(0);
            if (!user.getPassword().equals(password)) {
                throw new HttpException(403, "password.invalid");
            }
        } else { // New user
            log.info("Register new user {}", login);
            user = new User();
            user.setLogin(login);
            user.setPassword(password);
            em.persist(user);
        }
        return user;
    }

    private void checkKey(String key, boolean mustBeForAllUsers, Collection<String> mustBeForTheseUsers) throws HttpException {
        final List<ApiKey> keys = em.createQuery("select k from ApiKey k where k.key = :key", ApiKey.class)
                .setParameter("key", key).setMaxResults(1).getResultList();
        if (keys.size() > 0) {
            final ApiKey apiKey = keys.get(0);
            if (mustBeForAllUsers && !apiKey.isAllUsers()) throw new HttpException(403, "api.key.must.be.for.all.users");
            if (!apiKey.isAllUsers() && !apiKey.getUsers().containsAll(mustBeForTheseUsers)) throw new HttpException(403, "api.key.not.valid.for.certain.users");
        } else {
            throw new HttpException(403, "api.key.not.found");
        }
    }

    @ExceptionHandler(HttpException.class)
    @ResponseBody
    public HttpErrorResponse handleHttpException(HttpException e, HttpServletResponse response) {
        response.setStatus(e.getHttpResponseCode());
        return new HttpErrorResponse(e.getMessageCode(), e.getMessageCode());
    }
}
