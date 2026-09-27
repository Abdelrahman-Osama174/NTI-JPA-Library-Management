package com.abdelrahman.repository;

import jakarta.persistence.EntityManager;

import java.util.List;

public class StatisticsRepo {

    private final EntityManager em;

    public StatisticsRepo(EntityManager em) {
        this.em = em;
    }

    public List<Object[]> countBooksPerAuthor() {

        List<Object[]> result = em.createQuery("select a.name, count(b) from Author a left join a.books b " +
                "group by a.id, a.name order by a.name", Object[].class).getResultList();

        System.out.println("\nCOUNT + GROUP BY:");

        result.forEach(row -> System.out.println(row[0] + " -> " + row[1] + " books"));
        return result;
    }
}