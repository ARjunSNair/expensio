package com.expensetracker.controller;

import com.expensetracker.model.Category;
import com.expensetracker.model.User;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping
    public List<Category> getCategories(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow();
        return categoryService.getCategoriesByUserId(user.getId());
    }

    @PostMapping
    public void createCategory(@RequestBody Category category, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow();
        category.setUser(user);
        categoryService.createCategory(category);
    }
} 