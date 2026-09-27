package com.abdelrahman.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Setter
@Getter
@NoArgsConstructor
//@DiscriminatorValue("CUSTOMER")
public class Customer extends LibraryUser {
    private String membershipNumber;

    public Customer(String name, String membershipNumber) {
        super(name);
        this.membershipNumber = membershipNumber;
    }

    @Override
    public String toString() {
        return "Customer{id=" + getId() + ", name='" + getName() +
               "', membershipNumber='" + membershipNumber + "'}";
    }
}
