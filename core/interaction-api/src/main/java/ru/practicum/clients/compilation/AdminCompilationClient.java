package ru.practicum.clients.compilation;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationRequestDto;
import ru.practicum.dto.compilation.UpdateCompilationRequestDto;

@FeignClient(name = "addmin-compilation-event")
public interface AdminCompilationClient {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CompilationDto addCompilation(@RequestBody @Valid CompilationRequestDto compilationRequestDto);

    @PatchMapping("/{compId}")
    CompilationDto updateCompilation(@PathVariable(name = "compId") Long compilationId,
                                            @RequestBody @Valid UpdateCompilationRequestDto compilationRequestDto);

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable(name = "compId") Long compilationId);
}