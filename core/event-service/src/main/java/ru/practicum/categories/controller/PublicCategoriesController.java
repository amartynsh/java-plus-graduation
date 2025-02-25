package ru.practicum.categories.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.clients.categories.PublicCategoriesClient;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.categories.service.CategoriesService;

import java.util.List;

@Slf4j
@Validated
@RequestMapping("/categories")
@RequiredArgsConstructor
@RestController
public class PublicCategoriesController implements PublicCategoriesClient {
    private final CategoriesService categoriesService;

    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") Integer from,
                                           @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /categories?from={}&size={}", from, size);
        return categoriesService.findBy(from, size);
    }

    //Получение категории по id
    @GetMapping("/{catId}")
    public CategoryDto getCategoryBy(@PathVariable Long catId) {
        log.info("GET /categories/{}", catId);
        return categoriesService.findBy(catId);
    }
}