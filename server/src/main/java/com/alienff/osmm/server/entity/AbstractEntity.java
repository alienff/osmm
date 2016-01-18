package com.alienff.osmm.server.entity;

import javax.persistence.*;

/**
 * @author mike
 * @since 18.01.2016 00:11
 */
@MappedSuperclass
abstract class AbstractEntity {
    private Long id;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "-" + id;
    }
}
