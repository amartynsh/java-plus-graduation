/*
package ru.practicum.clients.compilation;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.compilation.CompilationDto;

import java.util.List;

@FeignClient(name = "event-service-public-compilation")
public interface PublicCompilationClient {
    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                @Positive @RequestParam(defaultValue = "10") Integer size);

    @GetMapping("/{compId}")
    public CompilationDto getById(@PathVariable(name = "compId") Long compilationId);
}
*/
