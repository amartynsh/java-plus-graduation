package ru.practicum.clients.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserRequestDto;

import java.util.List;

@FeignClient(name = "admin-user-service")
public interface AdminUserClient {
    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                  @Positive @RequestParam(defaultValue = "10") Integer size);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserDto registerUser(@RequestBody @Valid UserRequestDto userRequestDto);

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable(name = "userId") Long userId);

    @GetMapping("/{userId}")
    UserDto getById(@PathVariable(name = "userId") Long userId);
}
