package com.abdelrahman.repository;

import com.abdelrahman.model.Book;
import jakarta.persistence.EntityManager;

import java.util.List;

public class BookRepo {

    private final EntityManager em;

    public BookRepo(EntityManager em) {
        this.em = em;
    }

    public List<Book> findBooksByAuthorName(String authorName) {
        List<Book> result = em.createQuery("select b from Book b " + "where b.author.name = :authorName " + "order by b.title", Book.class).setParameter("authorName", authorName).getResultList();

        System.out.println("\nBOOKS BY AUTHOR:");
        result.forEach(System.out::println);

        return result;
    }


    public List<Book> findBooksByPublisher(String publisherName) {
        List<Book> result = em.createQuery("select b from Book b where b.publisher.name = :publisherName " +
                "order by b.title", Book.class)
                .setParameter("publisherName", publisherName)
                .getResultList();

        System.out.println("\nBOOKS BY PUBLISHER:");
        result.forEach(System.out::println);

        return result;
    }


    public Book findBookById(long id) {
        System.out.println("\nFIND BOOK BY ID:");

        try {
            Book result = em.createQuery("select b from Book b where b.id = ?1", Book.class)
                    .setParameter(1, id)
                    .getSingleResult();

            System.out.println(result);
            return result;

        } catch (jakarta.persistence.NoResultException e) {
            System.out.println("No book found with id=" + id + " — NoResultException caught (getSingleResult throws if no row matched)");
            return null;
        }
    }


    public List<Book> standardJpqlQuery() {
        List<Book> result = em.createQuery("select b from Book b where b.publishedYear >= :year " +
                "order by b.publishedYear desc", Book.class)
                .setParameter("year", 1900)
                .getResultList();

        System.out.println("\nSTANDARD JPQL:");
        result.forEach(System.out::println);

        return result;
    }
}