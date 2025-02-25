package ru.practicum.dto.compilation;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompilationRequestDto {
    @Size(max = 50)
    private String title;
    private Boolean pinned;
    private List<Long> events;
}