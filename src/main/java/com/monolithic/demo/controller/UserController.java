package com.monolithic.demo.controller;


import com.monolithic.demo.dto.request.ApiResponse;
import com.monolithic.demo.dto.request.UserCreationRequest;
import com.monolithic.demo.dto.request.UserUpdateRequest;
import com.monolithic.demo.dto.response.UserResponse;
import com.monolithic.demo.entity.User;
import com.monolithic.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(request));

        return apiResponse;
    }

    @GetMapping
    List<UserResponse> findAll() {

        return userService.findAll();
    }

    @PutMapping("/{id}")
    UserResponse updateUser(@PathVariable("id") String id, @RequestBody UserUpdateRequest updateRequest) {
        return this.userService.updateUser(id, updateRequest);
    }

    @GetMapping("/{id}")
    UserResponse findById(@PathVariable("id") String id) {
        return this.userService.findById(id);
    }

    @DeleteMapping("/{id}")
    String deleteUser(@PathVariable("id") String id) {
        this.userService.deleteUser(id);
        return "User has been deleted";
    }

}
