package eu.sternenfighter.library.service;

import eu.sternenfighter.library.model.Category;
import eu.sternenfighter.library.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory() {
        Category category = new Category(1L, "Fantasy", "Dragons");
        categoryService.createCategory(category);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory() {
        Category existingCategory = new Category(1L, "Fantasy", "Dragons");
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(existingCategory));
        Category category = new Category();
        category.setName("Horror");
        category.setDescription("Scary");
        categoryService.updateCategory(1L, category);
        verify(categoryRepository).save(any(Category.class));
    }

}