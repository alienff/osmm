package com.alienff.osmm.server;

import com.alienff.osmm.server.entity.Point;
import com.alienff.osmm.server.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
    public String index() {
        log.debug("Index");
        return "{\"version\":\"0.0+\"}";
    }

    @RequestMapping(value = "/add", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @Transactional
    public String add(@RequestParam(required = false) String login,
                      @RequestParam(required = false) String password,
                      @RequestParam double lat,
                      @RequestParam double lon,
                      @RequestParam(required = false) Long timestamp,
                      @RequestParam(required = false) Double hdop,
                      @RequestParam(required = false) Double altitude,
                      @RequestParam(required = false) Double speed,
                      @RequestParam(required = false) Double bearing) {
        log.debug("Add point ({}, {})", lat, lon);
        final List<User> existingAdmins = em.createQuery("select u from User u where u.login = :login", User.class)
                .setParameter("login", "admin")
                .setMaxResults(1)
                .getResultList();
        final User user;
        if (existingAdmins.isEmpty()) {
            user = new User();
            user.setLogin("admin");
            em.persist(user);
        } else {
            user = existingAdmins.get(0);
        }

        final Point point = new Point();
        point.setUser(user);
        point.setLat(lat);
        point.setLon(lon);
        em.persist(point);

        return "{\"result\":true}";
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
}
