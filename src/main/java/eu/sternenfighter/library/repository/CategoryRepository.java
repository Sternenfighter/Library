package eu.sternenfighter.library.repository;

import eu.sternenfighter.library.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
