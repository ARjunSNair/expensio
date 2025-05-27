package com.expensetracker.service;

import com.expensetracker.model.Category;
import com.expensetracker.model.User;
import com.expensetracker.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private CategoryService categoryService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(1L).email("test@example.com").status(User.Status.ACTIVE).build();
    }

    @Test
    void createCategory_shouldSaveCategory() {
        Category category = Category.builder().user(user).name("Food").build();
        categoryService.createCategory(category);
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getName()).isEqualTo("Food");
    }

    @Test
    void getCategoriesByUserId_shouldReturnCategories() {
        when(categoryRepository.findByUserId(1L)).thenReturn(List.of(
                Category.builder().user(user).name("Travel").build()
        ));
        List<Category> categories = categoryService.getCategoriesByUserId(1L);
        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).getName()).isEqualTo("Travel");
    }
} 