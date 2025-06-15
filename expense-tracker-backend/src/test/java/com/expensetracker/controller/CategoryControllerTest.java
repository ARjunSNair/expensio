package com.expensetracker.controller;

import com.expensetracker.model.Category;
import com.expensetracker.model.User;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CategoryService categoryService;
    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@example.com").status(User.Status.ACTIVE).build();
        category = Category.builder().id(1L).name("Food").user(user).build();
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getCategories_shouldReturnCategoriesForUser() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(categoryService.getCategoriesByUserId(1L)).thenReturn(List.of(category));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Food"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void createCategory_shouldCreateCategoryForUser() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        Category req = Category.builder().name("Travel").build();

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(categoryService).createCategory(any(Category.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void updateCategory_shouldUpdateCategoryForUser() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.of(category));
        
        Category updateReq = Category.builder().name("Updated Food").build();

        mockMvc.perform(put("/api/categories/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());

        verify(categoryService).updateCategory(eq(1L), any(Category.class));
    }

    @Test
    @WithMockUser(username = "other@example.com", roles = {"USER"})
    void updateCategory_shouldForbidOtherUsersCategory() throws Exception {
        User otherUser = User.builder().id(2L).email("other@example.com").status(User.Status.ACTIVE).build();
        when(userService.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.of(category));

        Category updateReq = Category.builder().name("Updated Food").build();

        mockMvc.perform(put("/api/categories/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).updateCategory(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void deleteCategory_shouldDeleteCategoryForUser() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.of(category));

        mockMvc.perform(delete("/api/categories/1")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(categoryService).deleteCategory(1L);
    }

    @Test
    @WithMockUser(username = "other@example.com", roles = {"USER"})
    void deleteCategory_shouldForbidOtherUsersCategory() throws Exception {
        User otherUser = User.builder().id(2L).email("other@example.com").status(User.Status.ACTIVE).build();
        when(userService.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.of(category));

        mockMvc.perform(delete("/api/categories/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).deleteCategory(any());
    }

    @Test
    void getCategories_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }
} 