package ru.practicum.service;

import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.CompilationRequestDto;
import ru.practicum.dto.UpdateCompilationRequestDto;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getById(Long compilationId);

    CompilationDto addCompilation(CompilationRequestDto compilationRequestDto);

    CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequestDto compilationRequestDto);

    void delete(Long compilationId);
}
