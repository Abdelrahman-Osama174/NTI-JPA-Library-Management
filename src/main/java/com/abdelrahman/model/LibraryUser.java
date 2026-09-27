package com.abdelrahman.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "library_users")
@Setter
@Getter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
//@DiscriminatorColumn(name = "user_type")
public abstract class LibraryUser {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String name;

    protected LibraryUser(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "LibraryUser{id=" + id + ", name='" + name + "'}";
    }
}
