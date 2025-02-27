/* package ru.practicum.clients.categories;


import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.dto.categories.NewCategoryDto;

import java.util.List;

@FeignClient(name = "event-service")
public interface CategoriesClient {

   @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto newCategoryDto);

    @PatchMapping("/admin/categories/{id}")
    public CategoryDto updateCategory(@PathVariable("id") Long id, @Valid @RequestBody NewCategoryDto newCategoryDto);

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/categories/{id}")
    public void deleteCategory(@PathVariable("id") Long id);



    @GetMapping("/categories")
    public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") Integer from,
                                           @RequestParam(defaultValue = "10") Integer size);

    //Получение категории по id
    @GetMapping("/categories/{catId}")
    public CategoryDto getCategoryBy(@PathVariable Long catId);



}
*/