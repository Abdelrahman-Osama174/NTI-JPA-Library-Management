package com.abdelrahman.model;

import jakarta.persistence.*;

@Entity
@Table(name = "employees")
public class Employee extends LibraryUser {

    private String department;

    public Employee() {

    }

    public Employee(String name, String department) {
        super(name);
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "Employee{id=" + getId() + ", name='" + getName() +
               "', department='" + department + "'}";
    }
}
