package com.expensetracker.controller;

import com.expensetracker.model.Category;
import com.expensetracker.model.User;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@example.com").status(User.Status.ACTIVE).build();
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getCategories_shouldReturnCategoriesForUser() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(user));
        when(categoryService.getCategoriesByUserId(1L)).thenReturn(List.of(
                Category.builder().id(10L).user(user).name("Food").build()
        ));
        // Simulate extracting userId=1 from JWT (in real, use SecurityContext)
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Food"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void createCategory_shouldCreateCategoryForUser() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(user));
        Category req = Category.builder().name("Travel").build();
        mockMvc.perform(post("/api/categories")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        verify(categoryService).createCategory(any(Category.class));
    }

    @Test
    void getCategories_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }
} 