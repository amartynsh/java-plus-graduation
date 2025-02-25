package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.clients.categories.AdminCategoriesClient;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.dto.categories.NewCategoryDto;
import ru.practicum.service.CategoriesService;

@Validated
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@RestController
public class AdminCategoriesController implements AdminCategoriesClient {
    private final CategoriesService categoriesService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoriesService.addCategory(newCategoryDto);
    }

    @PatchMapping("/{id}")
    public CategoryDto updateCategory(@PathVariable("id") Long id, @Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoriesService.updateCategory(id, newCategoryDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable("id") Long id) {
        categoriesService.deleteCategory(id);
    }
}