package com.abdelrahman.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employees")
@Setter
@Getter
@NoArgsConstructor
//@DiscriminatorValue("EMPLOYEE")
public class Employee extends LibraryUser {

    private String department;

    public Employee(String name, String department) {
        super(name);
        this.department = department;
    }

    @Override
    public String toString() {
        return "Employee{id=" + getId() + ", name='" + getName() +
                "', department='" + department + "'}";
    }
}
