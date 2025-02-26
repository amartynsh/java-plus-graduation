package ru.practicum.compilation.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.core.error.exception.NotFoundException;
import ru.practicum.core.util.PagingUtil;

import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationRequestDto;
import ru.practicum.dto.compilation.UpdateCompilationRequestDto;
import ru.practicum.event.handler.EventHandler;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;


import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final   CompilationRepository compilationRepository;
    private final  EventRepository eventRepository;
    private final  CompilationMapper compilationMapper;
    private final  EventMapper eventMapper;
    private final EventHandler eventHandler;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("getCompilations params: pinned = {}, from = {}, size = {}", pinned, from, size);
        PageRequest page = PagingUtil.pageOf(from, size);

        return compilationRepository.findAllByPinned(pinned, page)
                .map(compilation -> compilationMapper.toDto(compilation, eventHandler.getListEventShortDto(compilation.getEvents())))
                .getContent();
    }

    @Override
    public CompilationDto getById(Long compilationId) {
        log.info("getById params: id = {}", compilationId);
        Compilation compilation = compilationRepository.findById(compilationId).orElseThrow(() -> new NotFoundException(
                String.format("Подборка с ид %s не найдена", compilationId))
        );
        log.info("getById result compilation = {}", compilation);
        return compilationMapper.toDto(compilation,eventHandler.getListEventShortDto(compilation.getEvents()));
    }

    @Override
    @Transactional
    public CompilationDto addCompilation(CompilationRequestDto compilationRequestDto) {
        log.info("addCompilation params: compilationRequestDto = {}", compilationRequestDto);

        List<Event> events = getAndCheckEventList(compilationRequestDto.getEvents());
        Compilation compilation = compilationRepository.save(compilationMapper.toEntity(compilationRequestDto, events));
        log.info("addCompilation result compilation = {}", compilation);
        return compilationMapper.toDto(compilation,eventHandler.getListEventShortDto(compilation.getEvents()));
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequestDto compilationRequestDto) {
        log.info("update params: compilationId = {}, compilationRequestDto = {}", compilationId, compilationRequestDto);
        Compilation compilation = compilationRepository.findById(compilationId).orElseThrow(() -> new NotFoundException(
                String.format("Подборка с ид %s не найдена", compilationId))
        );
        List<Event> events = getAndCheckEventList(compilationRequestDto.getEvents());
        compilationMapper.update(compilationRequestDto, compilationId, events, compilation);
        compilation = compilationRepository.save(compilation);
        log.info("addCompilation result compilation = {}", compilation);

        return compilationMapper.toDto(compilation, eventHandler.getListEventShortDto(compilation.getEvents()));
    }

    @Override
    @Transactional
    public void delete(Long compilationId) {
        log.info("delete params: compilationId = {}", compilationId);
        compilationRepository.deleteById(compilationId);
    }

    private List<Event> getAndCheckEventList(List<Long> eventIds) {
        log.info("getAndCheckEventList params: eventIds = {}", eventIds);
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<Event> events = eventRepository.findAllById(eventIds);
            log.info("getAndCheckEventList result: events = {}", events);
            if (events.size() != eventIds.size()) {
                throw new NotFoundException("Некорректный список событий");
            }

            return events;
        }
    }
}
