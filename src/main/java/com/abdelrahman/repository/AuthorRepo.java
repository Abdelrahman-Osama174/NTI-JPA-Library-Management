package com.abdelrahman.repository;

import com.abdelrahman.model.Author;
import jakarta.persistence.EntityManager;

import java.util.List;

public class AuthorRepo {

    private final EntityManager em;

    public AuthorRepo(EntityManager em) {
        this.em = em;
    }

    public List<Author> fetchAuthorWithBooks(String authorName) {

        List<Author> result = em.createQuery("select distinct a from Author a " +
                        "left join fetch a.books where a.name = :authorName", Author.class)
                .setParameter("authorName", authorName)
                .getResultList();

        System.out.println("\nJOIN FETCH:");

        result.forEach(author ->
                System.out.println(author + " -> books loaded: " + author.getBooks().size()));

        return result;
    }
}