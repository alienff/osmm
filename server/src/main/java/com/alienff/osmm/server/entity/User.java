package com.alienff.osmm.server.entity;

import javax.persistence.*;

/**
 * @author mike
 * @since 18.01.2016 00:10
 */
@Entity
@Table(name = "t_user")
public class User extends AbstractEntity {
    private String login;
    private String password;

    @Column(name = "login")
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Column(name = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + login + ")";
    }
}
