package ru.yandex.practicum.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;


public interface EventSimilarityService {

    void handleEventSimilarity(EventSimilarityAvro eventSimilarityAvro);
}
