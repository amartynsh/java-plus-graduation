package ru.practicum.compilation.service;



import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationRequestDto;
import ru.practicum.dto.compilation.UpdateCompilationRequestDto;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getById(Long compilationId);

    CompilationDto addCompilation(CompilationRequestDto compilationRequestDto);

    CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequestDto compilationRequestDto);

    void delete(Long compilationId);
}
