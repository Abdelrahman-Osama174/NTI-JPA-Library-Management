# Library Management System — JPA Day 2

A hands-on JPA project that covers entity relationships, fetching strategies, cascading, inheritance, JPQL, and the Criteria API — all backed by an H2 in-memory database.

---

## Table of Contents

- [Project Structure](#project-structure)
- [Domain Model](#domain-model)
- [Relationship Mappings](#relationship-mappings)
- [Fetch Strategy](#fetch-strategy)
- [Cascade & OrphanRemoval](#cascade--orphanremoval)
- [Inheritance Strategy](#inheritance-strategy)
- [Queries](#queries)
- [Key Demonstrations](#key-demonstrations)
- [How to Run](#how-to-run)

---

## Project Structure

```
src/main/java/com/abdelrahman/
├── model/
│   ├── Author.java
│   ├── Book.java
│   ├── Publisher.java
│   ├── Category.java
│   ├── LibraryUser.java
│   ├── Employee.java
│   └── Customer.java
├── repository/
│   ├── AuthorRepo.java
│   ├── BookRepo.java
│   ├── CriteriaBookRepo.java
│   ├── LazyRepo.java
│   └── StatisticsRepo.java
└── App.java

src/main/resources/
└── META-INF/
    └── persistence.xml
```

---

## Domain Model

```
Author ──< Book >── Publisher
              │
              └──< book_categories >── Category

LibraryUser
    ├── Employee
    └── Customer
```

---

## Relationship Mappings

### Author ↔ Book — Bidirectional OneToMany / ManyToOne

| Side | Class | Annotation | FK / Join |
|---|---|---|---|
| **Owning** | `Book` | `@ManyToOne` | `author_id` column in `books` table |
| **Inverse** | `Author` | `@OneToMany(mappedBy = "author")` | no column — just mirrors the owning side |

`Book` is the owning side because it holds the FK `author_id`.
`Author.books` uses `mappedBy = "author"` which tells JPA it is the inverse side and does not control the FK.

`Author` provides two helpers to keep both sides in sync:

```java
public void addBook(Book book) {
    books.add(book);
    book.setAuthor(this);
}

public void removeBook(Book book) {
    books.remove(book);
    book.setAuthor(null);
}
```

Without these helpers, setting only one side would leave the in-memory object graph inconsistent even if the DB row is correct.

---

### Book → Publisher — Unidirectional ManyToOne

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "publisher_id")
private Publisher publisher;
```

- FK `publisher_id` lives in the `books` table.
- `Publisher` has a `@OneToMany(mappedBy = "publisher")` for convenience queries, but it does **not** own the relationship and has no cascade — deleting a publisher does not cascade to its books.
- A join table is **not** needed here because it is a many-to-one; the FK on the many side is enough.

---

### Book ↔ Category — Bidirectional ManyToMany

```java
// Book (owning side)
@ManyToMany
@JoinTable(
    name = "book_categories",
    joinColumns        = @JoinColumn(name = "book_id"),
    inverseJoinColumns = @JoinColumn(name = "category_id")
)
private Set<Category> categories;

// Category (inverse side)
@ManyToMany(mappedBy = "categories")
private Set<Book> books;
```

A join table **is** required here because neither side can store a single FK for a many-to-many relationship. The join table `book_categories` holds the pair `(book_id, category_id)`.

`Book` is the owning side because it declares `@JoinTable`. `Category` uses `mappedBy` to be the inverse.

`Book` provides helpers:

```java
public void addCategory(Category category) {
    categories.add(category);
    category.getBooks().add(this);
}
```

---

## Fetch Strategy

| Relationship | Fetch Type | Reason |
|---|---|---|
| `Author.books` | `LAZY` | A collection — loaded on demand to avoid unnecessary SQL |
| `Book.author` | `LAZY` | Explicit override; default for `@ManyToOne` is EAGER but LAZY is safer |
| `Book.publisher` | `LAZY` | Same reasoning as above |
| `Book.categories` | `LAZY` | Collection — always LAZY by default |
| `Publisher.books` | `LAZY` | Collection — loaded on demand |

**Rule of thumb:** collections are always `LAZY`. To-one associations default to `EAGER` in JPA spec, but overriding them to `LAZY` avoids surprise N+1 queries and is the recommended Hibernate practice.

---

## Cascade & OrphanRemoval

```java
@OneToMany(
    mappedBy      = "author",
    cascade       = CascadeType.ALL,
    orphanRemoval = true,
    fetch         = FetchType.LAZY
)
private List<Book> books;
```

### CascadeType.ALL

Persisting an `Author` automatically persists all books in its list — no need to call `em.persist()` on each book separately.

```java
Author abdelrahman = new Author("Abdelrahman");
abdelrahman.addBook(new Book("Clean Code", 2008, packt));
em.persist(abdelrahman); // books are persisted automatically
```

### orphanRemoval = true

When a book is removed from `Author.books`, JPA treats it as an orphan and issues a `DELETE` automatically at commit time.

```java
managedAbdelrahman.removeBook(removedBook);
tx.commit(); // DELETE FROM books WHERE id = ?
```

Without `orphanRemoval`, removing from the list would just break the association in memory, but the row would remain in the database.

---

## Inheritance Strategy

```java
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class LibraryUser { ... }
```

**Chosen strategy: `TABLE_PER_CLASS`**

Each concrete subclass gets its own table that contains all columns — both inherited and its own.

| Table | Columns |
|---|---|
| `employees` | `id`, `name`, `department` |
| `customers` | `id`, `name`, `membershipNumber` |

**Why `TABLE_PER_CLASS`?**

- `Employee` and `Customer` have no shared columns beyond `id` and `name`, so a single `SINGLE_TABLE` would carry many nullable columns with no benefit.
- `JOINED` strategy requires a join on every query which adds overhead for a simple case like this.
- `TABLE_PER_CLASS` keeps each table clean and self-contained. The trade-off is that polymorphic queries across `LibraryUser` require a `UNION`, but since we query each subtype independently here, that cost never applies.

**Note:** `@DiscriminatorColumn` / `@DiscriminatorValue` are not needed with `TABLE_PER_CLASS` — they are only relevant for `SINGLE_TABLE`.

`@GeneratedValue(strategy = GenerationType.SEQUENCE)` is required here. `AUTO` with `TABLE_PER_CLASS` can produce duplicate IDs across tables in H2 because each table manages its own identity. A shared `SEQUENCE` guarantees uniqueness across all subtypes.

---

## Queries

### JPQL Queries (`BookRepo`, `AuthorRepo`, `StatisticsRepo`)

#### Q1 — Books by author name
```java
em.createQuery(
    "select b from Book b where b.author.name = :authorName order by b.title",
    Book.class
).setParameter("authorName", authorName).getResultList();
```
Navigates the `Book → Author` association directly in JPQL without an explicit join. Hibernate generates an implicit inner join on `author_id`.

---

#### Q2 — Books by publisher
```java
em.createQuery(
    "select b from Book b where b.publisher.name = :publisherName order by b.title",
    Book.class
).setParameter("publisherName", publisherName).getResultList();
```
Same implicit join pattern through `Book → Publisher`.

---

#### Q3 — Book by ID with positional parameter
```java
em.createQuery("select b from Book b where b.id = ?1", Book.class)
  .setParameter(1, id)
  .getSingleResult();
```
Uses a positional parameter (`?1`) instead of a named one (`:id`). `getSingleResult()` throws `NoResultException` if no row matches — always wrap it in a try-catch.

---

#### Q4 — Author + Books via JOIN FETCH
```java
em.createQuery(
    "select distinct a from Author a left join fetch a.books where a.name = :authorName",
    Author.class
).setParameter("authorName", authorName).getResultList();
```
`JOIN FETCH` forces Hibernate to load the `books` collection in the same SQL query. The result is a fully initialized collection that remains accessible even after the `EntityManager` is closed. `distinct` prevents duplicate `Author` rows caused by the join.

---

#### Q5 — Count books per author (aggregate)
```java
em.createQuery(
    "select a.name, count(b) from Author a left join a.books b " +
    "group by a.id, a.name order by a.name",
    Object[].class
).getResultList();
```
`LEFT JOIN` ensures authors with zero books (like Osama) still appear in the result with a count of 0. `GROUP BY a.id, a.name` is required when mixing aggregate and non-aggregate columns.

---

#### Q9 — Standard JPQL vs HQL
```java
em.createQuery(
    "select b from Book b where b.publishedYear >= :year order by b.publishedYear desc",
    Book.class
).setParameter("year", 1900).getResultList();
```

**Standard JPQL** is portable across all JPA providers.

**HQL difference:** Hibernate-specific HQL supports features beyond the JPA spec, for example:
- `setFirstResult()` / `setMaxResults()` as pagination hints that translate to `LIMIT / OFFSET`
- Native function calls like `str()`, `bit_length()`, `extract()`
- `FILTER` clause on aggregates

For most use cases, standard JPQL is preferred to keep the code provider-agnostic.

---

### Criteria API (`CriteriaBookRepo`)

#### Q6 — Find books by title
```java
public List<Book> criteriaByTitle(String title) {
    return criteriaOptionalFilters(title, null);
}
```
Delegates to the optional-filters method with `authorName = null`.

---

#### Q7 — Optional filters (title + author name)
```java
List<Predicate> predicates = new ArrayList<>();

if (title != null && !title.isBlank()) {
    predicates.add(cb.like(cb.lower(book.get("title")), "%" + title.toLowerCase() + "%"));
}

if (authorName != null && !authorName.isBlank()) {
    Join<Book, Author> author = book.join("author");
    predicates.add(cb.equal(cb.lower(author.get("name")), authorName.toLowerCase()));
}

cq.select(book)
  .where(predicates.toArray(new Predicate[0]))
  .orderBy(cb.asc(book.get("title")));
```

Predicates are built dynamically at runtime. If both parameters are `null`, the `WHERE` clause is empty and all books are returned. This pattern is the main reason to choose the Criteria API over JPQL — a JPQL string with optional filters requires string concatenation, which is error-prone.

---

## Key Demonstrations

### Cascading on persist
```java
em.persist(abdelrahman); // persists Author + all 3 Books in one call
```

### orphanRemoval on remove
```java
managedAbdelrahman.removeBook(removedBook);
tx.commit();
// Hibernate issues: DELETE FROM book_categories WHERE book_id = ?
//                   DELETE FROM books WHERE id = ?
```

### LazyInitializationException (intentional)
```java
EntityManager shortLivedEm = emf.createEntityManager();
shortLivedEm.getTransaction().begin();
Author detachedMostafa = shortLivedEm
    .createQuery("select a from Author a where a.name = :name", Author.class)
    .setParameter("name", "Mostafa")
    .getSingleResult();
shortLivedEm.getTransaction().commit();
shortLivedEm.close(); // session is fully closed here

detachedMostafa.getBooks().size(); // LazyInitializationException
```

**Why it happens:** `books` is `LAZY`. When the `EntityManager` closes, the Hibernate session is gone. Accessing an uninitialized lazy collection without an open session throws `LazyInitializationException`.

**How JOIN FETCH prevents it:**
```java
// books are loaded eagerly inside the query — no open session needed afterwards
Author fetchedAuthor = em.createQuery(
    "select distinct a from Author a left join fetch a.books where a.name = :name",
    Author.class
).setParameter("name", authorName).getSingleResult();

tx.commit();
fetchedAuthor.getBooks().size(); // works fine — collection already initialized
```

### LAZY inside an open transaction (LazyRepo)
```java
tx.begin();
Author lazyAuthor = em.createQuery(...).getSingleResult();
lazyAuthor.getBooks().size(); // works — session is still open, triggers a second SELECT
tx.commit();
```

This works because the session is still active. Hibernate issues a second `SELECT` to load the books when `getBooks()` is called — this is the classic **N+1 problem** when done inside a loop.

---

## How to Run

**Prerequisites:** Java 17+, Maven 3.8+

```bash
mvn clean package
mvn exec:java -Dexec.mainClass="com.abdelrahman.App"
```

Or run `App.java` directly from IntelliJ IDEA.

The H2 database is in-memory (`jdbc:h2:mem:librarydb`) and is created fresh on every run. All tables, constraints, and sample data are set up automatically by Hibernate.