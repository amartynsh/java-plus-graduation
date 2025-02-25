package ru.practicum.clients.categories;


import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.dto.categories.NewCategoryDto;

@FeignClient(name = "admin-categories-service")
public interface AdminCategoriesClient {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto newCategoryDto);

    @PatchMapping("/{id}")
    public CategoryDto updateCategory(@PathVariable("id") Long id, @Valid @RequestBody NewCategoryDto newCategoryDto);

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable("id") Long id);

}

