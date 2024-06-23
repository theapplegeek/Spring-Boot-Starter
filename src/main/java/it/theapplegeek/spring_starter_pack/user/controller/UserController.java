package it.theapplegeek.spring_starter_pack.user.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import it.theapplegeek.spring_starter_pack.user.dto.UserDto;
import it.theapplegeek.spring_starter_pack.user.service.UserService;
import it.theapplegeek.spring_starter_pack.common.util.pagination.PagedListDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
  private final UserService userService;

  @PostMapping("list")
  public PagedListDto<UserDto> getAllUsers(
      @RequestParam("page") int page,
      @RequestParam("size") int size,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "direction", required = false) String direction,
      @RequestBody(required = false) UserDto search) {
    return userService.getAllUsers(page, size, sort, direction, search);
  }

  @PostMapping
  public UserDto addUser(@RequestBody @Valid UserDto userDto) {
    return userService.addUser(userDto);
  }

  @PutMapping("{userId}")
  public UserDto updateUser(@PathVariable("userId") Long userId, @RequestBody UserDto userDto) {
    return userService.updateUser(userId, userDto);
  }

  @DeleteMapping("{userId}")
  public void deleteUser(@PathVariable("userId") Long userId) {
    userService.deleteUser(userId);
  }
}
