package com.alienff.osmm.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.joining;

/**
 * @author mike
 * @since 20.01.2016 10:32
 */
@Entity
@Table(name = "api_key")
public class ApiKey extends AbstractEntity {
    private String key;
    private String usersRaw;
    private boolean allUsers;

    @Column(name = "key")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Column(name = "users")
    public String getUsersRaw() {
        return usersRaw;
    }

    public void setUsersRaw(String usersRaw) {
        this.usersRaw = usersRaw;
    }

    @Transient
    public Set<String> getUsers() {
        return usersRaw != null ? new HashSet<>(Arrays.asList(usersRaw.split(","))) : new HashSet<>();
    }

    public void setUsers(Collection<String> users) {
        usersRaw = users != null ? users.stream().collect(joining(",")) : null;
    }

    @Column(name = "all_users")
    public boolean isAllUsers() {
        return allUsers;
    }

    public void setAllUsers(boolean allUsers) {
        this.allUsers = allUsers;
    }
}
