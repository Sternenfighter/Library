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

    /**
     * create a new category
     * @param income new category without id
     * @return the new saved category
     * @throws IllegalArgumentException happens when something went wrong
     */
    public Category createCategory(Category income) {
        Category category = new Category();
        category.setName(income.getName());
        category.setDescription(income.getDescription());
        return categoryRepository.save(category);
    }

    /**
     * update one category
     * @param id id from the category
     * @param category category with updated values
     * @return updated category
     * @throws IllegalArgumentException category not found
     */
    public Category updateCategory(long id, Category category) {
        Optional<Category> optional = categoryRepository.findById(id);
        if (optional.isPresent()) {
            category.setId(optional.get().getId());
            return categoryRepository.save(category);
        }
        throw new IllegalArgumentException("Category not Found");
    }

    /**
     * delete category
     * only works if no book is in the category
     * @param id id from the category
     * @throws IllegalArgumentException category cannot be deleted
     */
    public void deleteCategory(long id) {
        if (bookRepository.countAllByCategoryId(id).equals(0L)) {
            categoryRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Category cannot be deleted");
        }
    }

    /**
     * count books in the given category
     * @param id category id
     * @return number of books
     */
    public Long countBooksInCategory(long id) {
        return bookRepository.countAllByCategoryId(id);
    }

    /**
     * find category by given id
     * @param id category id
     * @return category if exist
     */
    public Optional<Category> findCategoryById(long id) {
        return categoryRepository.findById(id);
    }

    /**
     * find all categories
     * @return list with all categories
     */
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }
}
