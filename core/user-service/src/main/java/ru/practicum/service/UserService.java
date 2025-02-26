package ru.practicum.service;


import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserRequestDto;
import ru.practicum.dto.user.UserShortDto;

import java.util.List;

public interface UserService {

    UserDto getById(Long userId);

    List<UserShortDto> getUsers(List<Long> ids);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    UserDto registerUser(UserRequestDto userRequestDto);

    void delete(Long userId);
}
