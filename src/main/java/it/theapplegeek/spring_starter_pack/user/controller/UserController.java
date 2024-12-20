package it.theapplegeek.spring_starter_pack.user.controller;

import it.theapplegeek.spring_starter_pack.common.annotation.ProvideUserLogged;
import it.theapplegeek.spring_starter_pack.common.exception.BadRequestException;
import it.theapplegeek.spring_starter_pack.security.model.UserLogged;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import it.theapplegeek.spring_starter_pack.user.dto.UserDto;
import it.theapplegeek.spring_starter_pack.user.service.UserService;
import it.theapplegeek.spring_starter_pack.common.util.pagination.PagedListDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
public class UserController {
  private final UserService userService;

  @PostMapping("list")
  @PreAuthorize("hasAnyAuthority('USER_READ')")
  public PagedListDto<UserDto> getAllUsers(
      @RequestParam("page") int page,
      @RequestParam("size") int size,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "direction", required = false) String direction,
      @RequestBody(required = false) UserDto search) {
    return userService.getAllUsers(page, size, sort, direction, search);
  }

  @PostMapping
  @PreAuthorize("hasAnyAuthority('USER_CREATE')")
  public UserDto addUser(@RequestBody @Valid UserDto userDto) {
    return userService.addUser(userDto);
  }

  @PutMapping("{userId}")
  @PreAuthorize("hasAnyAuthority('USER_UPDATE')")
  public UserDto updateUser(@PathVariable("userId") Long userId, @RequestBody UserDto userDto) {
    return userService.updateUser(userId, userDto);
  }

  @PutMapping("change-password")
  @PreAuthorize("hasAnyAuthority('USER_CHANGE_PASSWORD')")
  public void changePassword(@ProvideUserLogged UserLogged userLogged, @RequestBody Map<String, String> data) {
    String oldPassword = data.get("oldPassword");
    String newPassword = data.get("newPassword");

    if (oldPassword == null || newPassword == null) {
      throw new BadRequestException("Old password and new password are required");
    }

    userService.changePassword(userLogged, oldPassword, newPassword);
  }

  @DeleteMapping("{userId}")
  @PreAuthorize("hasAnyAuthority('USER_DELETE')")
  public void deleteUser(@PathVariable("userId") Long userId) {
    userService.deleteUser(userId);
  }
}
