package com.abdelrahman.model;

import jakarta.persistence.*;

@Entity
@Table(name = "library_users")
public abstract class LibraryUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    protected LibraryUser() {
    }

    protected LibraryUser(String name) {
        this.name = name;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "LibraryUser{id=" + id + ", name='" + name + "'}";
    }
}
