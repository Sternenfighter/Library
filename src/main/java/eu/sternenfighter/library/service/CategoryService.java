package eu.sternenfighter.library.service;

import eu.sternenfighter.library.model.Category;
import eu.sternenfighter.library.repository.BookRepository;
import eu.sternenfighter.library.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    public Category createCategory(Category income) {
        Category category = new Category();
        category.setName(income.getName());
        category.setDescription(income.getDescription());
        return categoryRepository.save(category);
    }

    public Category updateCategory(long id, Category category) {
        Optional<Category> optional = categoryRepository.findById(id);
        if (optional.isPresent()) {
            category.setId(optional.get().getId());
            return categoryRepository.save(category);
        }
        throw new IllegalArgumentException("Category not Found");
    }

    public void deleteCategory(long id) {
        if (bookRepository.countAllByCategoryId(id).equals(0L)) {
            categoryRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Category cannot be deleted");
        }
    }


    public Long countBooksInCategory(long id) {
        return bookRepository.countAllByCategoryId(id);
    }

    public Optional<Category> findCategoryById(long id) {
        return categoryRepository.findById(id);
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }
}
