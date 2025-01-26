package eu.sternenfighter.library.repository;

import eu.sternenfighter.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    Long countAllByCategoryId(long category);
}
