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
import java.util.List;

/**
 * @author mike
 * @since 17.01.2016 14:25
 */
@Controller
public class OsmmController {
    private static final Logger log = LoggerFactory.getLogger(OsmmController.class);

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
    public List<Point> getAll() {
        log.debug("Get all points");
        final List<Point> points = em.createQuery("select p from Point p", Point.class).getResultList();
        points.forEach(point -> point.getUser().setPassword(null));
        return points;
    }

    @RequestMapping(value = "/get-all", params = "login")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<Point> getAllForUser(@RequestParam String login) {
        log.debug("Get all points for user {}", login);
        final List<Point> points = em.createQuery("select p from Point p where p.user.login = :login", Point.class)
                .setParameter("login", login).getResultList();
        points.forEach(point -> point.setUser(null));
        return points;
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

    @ExceptionHandler(HttpException.class)
    @ResponseBody
    public HttpErrorResponse handleHttpException(HttpException e, HttpServletResponse response) {
        response.setStatus(e.getHttpResponseCode());
        return new HttpErrorResponse(e.getMessageCode(), e.getMessageCode());
    }
}
