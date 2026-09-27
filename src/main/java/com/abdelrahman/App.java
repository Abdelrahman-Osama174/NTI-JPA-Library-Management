package com.abdelrahman;

import com.abdelrahman.model.*;
import com.abdelrahman.repository.*;
import jakarta.persistence.*;

public class App {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("libraryPU");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        // ── insert sample data ──────────────────────────────────────
        tx.begin();

        Publisher packt = new Publisher("Packt Publishing");
        Publisher manning = new Publisher("Manning Publications");
        em.persist(packt);
        em.persist(manning);

        Category algorithms = new Category("Algorithms");
        Category webDev = new Category("Web Development");
        em.persist(algorithms);
        em.persist(webDev);

        Author abdelrahman = new Author("Abdelrahman");
        Book cleanCode = new Book("Clean Code", 2008, packt);
        Book designPatterns = new Book("Design Patterns", 2000, packt);
        Book springInAction = new Book("Spring in Action", 2022, manning);
        cleanCode.addCategory(algorithms);
        cleanCode.addCategory(webDev);
        designPatterns.addCategory(algorithms);
        abdelrahman.addBook(cleanCode);
        abdelrahman.addBook(designPatterns);
        abdelrahman.addBook(springInAction);
        em.persist(abdelrahman);

        Author mostafa = new Author("Mostafa");
        Book dsa = new Book("Data Structures & Algorithms", 2019, manning);
        dsa.addCategory(algorithms);
        mostafa.addBook(dsa);
        em.persist(mostafa);

        // author with no books — tests LEFT JOIN in statistics query
        Author osama = new Author("Osama");
        em.persist(osama);

        Employee ali = new Employee("Ali", "Backend Team");
        Customer kareem = new Customer("Kareem", "SILVER-042");
        em.persist(ali);
        em.persist(kareem);

        tx.commit();
        System.out.println("Sample data inserted successfully\n");

        System.out.println("======================================================================");
        System.out.println("============================== TASK 1 ================================");

        // ── orphanRemoval ───────────────────────────────────────────
        tx.begin();
        Author managedAbdelrahman = em.find(Author.class, abdelrahman.getId());
        Book removedBook = managedAbdelrahman.getBooks().get(0);
        System.out.println("removing: \"" + removedBook.getTitle() + " Book");
        managedAbdelrahman.removeBook(removedBook);
        tx.commit();

        // ── LazyInitializationException (intentional) ───────────────

        EntityManager shortLivedEm = emf.createEntityManager();
        shortLivedEm.getTransaction().begin();

        Author detachedMostafa = shortLivedEm.createQuery("select a from Author a where a.name = :name",
                Author.class).setParameter("name", "Mostafa").getSingleResult();

        shortLivedEm.getTransaction().commit();
        shortLivedEm.close(); // session closed — books not loaded yet

        try {
            int count = detachedMostafa.getBooks().size(); // triggers exception
            System.out.println("count: " + count);
        } catch (Exception e) {
            System.out.println("LazyInitializationException caught — session was closed before accessing books");

        }


        System.out.println("\n======================================================================");
        System.out.println("============================== TASK 2 ================================");

        BookRepo bookRepo = new BookRepo(em);
        AuthorRepo authorRepo = new AuthorRepo(em);
        StatisticsRepo statsRepo = new StatisticsRepo(em);
        CriteriaBookRepo criteriaRepo = new CriteriaBookRepo(em);
        LazyRepo lazyRepo = new LazyRepo(em);

        bookRepo.findBooksByAuthorName("Abdelrahman");
        bookRepo.findBooksByPublisher("Manning Publications");
        bookRepo.findBookById(2); // id=1 was deleted by orphanRemoval

        authorRepo.fetchAuthorWithBooks("Abdelrahman");

        statsRepo.countBooksPerAuthor();

        criteriaRepo.criteriaByTitle("design");
        criteriaRepo.criteriaOptionalFilters("data", "Mostafa");
        criteriaRepo.criteriaOptionalFilters(null, null); // no filters — returns all

        lazyRepo.lazyAndJoinFetch("Mostafa");

        bookRepo.standardJpqlQuery();

        em.close();
        emf.close();
        System.out.println("\nDone — all tasks completed.");
    }
}