package com.monolithic.demo.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.monolithic.demo.dto.request.ApiResponse;
import com.monolithic.demo.dto.request.UserCreationRequest;
import com.monolithic.demo.dto.request.UserUpdateRequest;
import com.monolithic.demo.dto.response.UserResponse;
import com.monolithic.demo.service.UserService;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(request));

        return apiResponse;
    }

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(this.userService.getInfoUser())
                .build();
    }

    // Có thể đặt các ràng buộc ở tầng service

    @GetMapping
    ApiResponse<List<UserResponse>> findAll() {
        // lay ra tat ca thong tin cua user dang duoc authen
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        // ghi log phan quyen
        log.info("Username: {}", authentication.getName());
        // check authorize qua log
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        // get thong tin dang dc authenticate thì dùng SecurityContextHolder này sẽ chứa info của user đang đăng nhập
        // hiện tại
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.findAll())
                .build();
    }

    @PutMapping("/{id}")
    UserResponse updateUser(@PathVariable("id") String id, @RequestBody UserUpdateRequest updateRequest) {

        return this.userService.updateUser(id, updateRequest);
    }

    @PostAuthorize("returnObject.username == authentication.name")
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
