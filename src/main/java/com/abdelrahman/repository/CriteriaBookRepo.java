package com.abdelrahman.repository;

import com.abdelrahman.model.Author;
import com.abdelrahman.model.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;

public class CriteriaBookRepo {

    private final EntityManager em;

    public CriteriaBookRepo(EntityManager em) {
        this.em = em;
    }

    public List<Book> criteriaByTitle(String title) {
        return criteriaOptionalFilters(title, null);
    }


    public List<Book> criteriaOptionalFilters(String title, String authorName) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Book> cq = cb.createQuery(Book.class);

        Root<Book> book = cq.from(Book.class);

        List<Predicate> predicates = new ArrayList<>();

        if (title != null && !title.isBlank()) {

            predicates.add(cb.like(cb.lower(book.get("title")), "%" + title.toLowerCase() + "%"));
        }

        if (authorName != null && !authorName.isBlank()) {

            Join<Book, Author> author = book.join("author");

            predicates.add(cb.equal(cb.lower(author.get("name")), authorName.toLowerCase()));
        }

        cq.select(book).where(predicates.toArray(new Predicate[0])).orderBy(cb.asc(book.get("title")));

        List<Book> result = em.createQuery(cq).getResultList();

        System.out.println("\nCRITERIA API:");

        System.out.println("title=" + title + ", author=" + authorName);

        result.forEach(System.out::println);

        return result;
    }
}