package com.abdelrahman.repository;

import com.abdelrahman.model.Author;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class LazyRepo {

    private final EntityManager em;

    public LazyRepo(EntityManager em) {
        this.em = em;
    }


    public void lazyAndJoinFetch(String authorName) {

        EntityTransaction tx = em.getTransaction();


        tx.begin();

        Author lazyAuthor = em.createQuery("select a from Author a where a.name = :name", Author.class)
                .setParameter("name", authorName)
                .getSingleResult();

        System.out.println("\nLAZY VS JOIN FETCH:");

        System.out.println("Normal LAZY query, inside transaction: books=" + lazyAuthor.getBooks().size());

        tx.commit();


        // JOIN FETCH
        tx.begin();

        Author fetchedAuthor = em.createQuery("select distinct a from Author a left join fetch a.books " +
                "where a.name = :name", Author.class)
                .setParameter("name", authorName)
                .getSingleResult();

        tx.commit();

        System.out.println("JOIN FETCH query, after transaction: books=" + fetchedAuthor.getBooks().size());
    }
}