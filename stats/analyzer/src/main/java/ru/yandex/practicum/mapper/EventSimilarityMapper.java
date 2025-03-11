package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.model.EventSimilarity;

@Mapper(componentModel = "spring")
public interface EventSimilarityMapper {
    EventSimilarity map(EventSimilarityAvro avro);
}
