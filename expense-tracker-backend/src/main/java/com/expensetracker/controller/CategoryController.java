package com.expensetracker.controller;

import com.expensetracker.model.Category;
import com.expensetracker.model.User;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCategory(
            @PathVariable Long id,
            @RequestBody Category category,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow();
        
        return categoryService.getCategoryById(id)
                .map(existingCategory -> {
                    if (!existingCategory.getUser().getId().equals(user.getId())) {
                        return ResponseEntity.status(403).<Void>build();
                    }
                    categoryService.updateCategory(id, category);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow();
        
        return categoryService.getCategoryById(id)
                .map(existingCategory -> {
                    if (!existingCategory.getUser().getId().equals(user.getId())) {
                        return ResponseEntity.status(403).<Void>build();
                    }
                    categoryService.deleteCategory(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 