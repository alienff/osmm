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

import static com.alienff.osmm.server.OsmmControllerHelper.findSingleResult;
import static com.alienff.osmm.server.OsmmControllerHelper.fromSinglePoint;
import static com.alienff.osmm.server.OsmmControllerHelper.splitToTracks;
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
        final List<Point> allPoints = em.createQuery("select p from Point p order by p.timestampRaw desc", Point.class).getResultList();
        final Map<String, List<Point>> userPoints = new HashMap<>();
        allPoints.forEach(point -> userPoints.computeIfAbsent(point.getUser().getLogin(), o -> new ArrayList<>()).add(point));
        final Map<String, List<Track>> result = new HashMap<>(userPoints.size());
        userPoints.entrySet().forEach(entry -> result.put(entry.getKey(), splitToTracks(entry.getValue(), TRACK_DELAY_THRESHOLD)));
        return result;
    }

    @RequestMapping(value = "/get-all", params = "login")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<Track> getAllForUser(@RequestParam String login, @RequestParam String key) throws HttpException {
        log.debug("Get all points for user {}", login);
        checkKey(key, false, Collections.singleton(login));
        final List<Point> points = em.createQuery("select p from Point p where p.user.login = :login order by p.timestampRaw desc", Point.class)
                .setParameter("login", login).getResultList();
        return splitToTracks(points, TRACK_DELAY_THRESHOLD);
    }

    @RequestMapping("/get-last-one")
    @ResponseBody
    @Transactional(readOnly = true)
    public Map<String, List<Track>> getLastOnes(@RequestParam String key) throws HttpException {
        log.debug("Get last point for everybody");
        checkKey(key, true, emptySet());
        final List<User> users = em.createQuery("select u from User u", User.class).getResultList();
        final Map<String, List<Track>> result = new HashMap<>(users.size());
        for (User user : users) {
            final Point point = findSingleResult(
                    em.createQuery("select p from Point p where p.user = :user order by p.timestampRaw desc", Point.class).setParameter("user", user));
            if (point != null) {
                result.put(user.getLogin(), fromSinglePoint(point));
            }
        }
        return result;
    }

    private User getOrAddUser(String login, String password) throws HttpException {
        if (login.length() == 0 || password.length() == 0) {
            throw new HttpException(401, "login.password.nonnull");
        }
        final User existingUser = findSingleResult(
                em.createQuery("select u from User u where u.login = :login", User.class).setParameter("login", login));
        final User user;
        if (existingUser != null) { // Existing user, check pwd
            user = existingUser;
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
        final ApiKey apiKey = findSingleResult(
                em.createQuery("select k from ApiKey k where k.key = :key", ApiKey.class).setParameter("key", key));
        if (apiKey != null) {
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
