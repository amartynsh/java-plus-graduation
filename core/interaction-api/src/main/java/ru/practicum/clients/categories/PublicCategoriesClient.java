package ru.practicum.clients.categories;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.categories.CategoryDto;

import java.util.List;

@FeignClient(name = "public-categories-service")
public interface PublicCategoriesClient {
    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") Integer from,
                                           @RequestParam(defaultValue = "10") Integer size);

    //Получение категории по id
    @GetMapping("/{catId}")
    public CategoryDto getCategoryBy(@PathVariable Long catId);
}