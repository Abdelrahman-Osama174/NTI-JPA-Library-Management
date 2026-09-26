package com.abdelrahman.model;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class Customer extends LibraryUser {
    private String membershipNumber;

    public Customer() {}

    public Customer(String name, String membershipNumber) {
        super(name);
        this.membershipNumber = membershipNumber;
    }

    public String getMembershipNumber() {
        return membershipNumber;
    }

    public void setMembershipNumber(String membershipNumber) {
        this.membershipNumber = membershipNumber;
    }

    @Override
    public String toString() {
        return "Customer{id=" + getId() + ", name='" + getName() +
               "', membershipNumber='" + membershipNumber + "'}";
    }
}
