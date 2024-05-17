package com.monolithic.demo.service;

import java.util.HashSet;
import java.util.List;
import javax.swing.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.monolithic.demo.dto.request.UserCreationRequest;
import com.monolithic.demo.dto.request.UserUpdateRequest;
import com.monolithic.demo.dto.response.UserResponse;
import com.monolithic.demo.entity.User;
import com.monolithic.demo.enums.Role;
import com.monolithic.demo.exception.AppException;
import com.monolithic.demo.exception.ErrorCode;
import com.monolithic.demo.mapper.UserMapper;
import com.monolithic.demo.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
// Nhung cai Autowired se dc khai bao trong constructor
public class UserService {


    UserRepository userRepository;

    UserMapper userMapper;

    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {
        log.info("Service: createUser");
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<String> roles = new HashSet<>();

        roles.add(Role.USER.name());
        //        user.setRoles(roles);

        return userMapper.toResponse(userRepository.save(user));
    }

    // hasAuthority la se kiem tra claim la scope
    @PreAuthorize("hasAuthority('APPROVE_POST')")
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    public UserResponse getInfoUser() {
        // trong Spring Security, khi 1 user đã đc Authen thành công thì thông tin user sẽ đc lưu vào
        // SpringContextHolder
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user =
                this.userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));
        return userMapper.toResponse(user);
    }

    public UserResponse findById(String id) {
        return userMapper.toResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND)));
    }

    public UserResponse updateUser(String id, UserUpdateRequest updateRequest) {

        User userUpdate = this.userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));

        userMapper.updateUser(userUpdate, updateRequest);

        return userMapper.toResponse(userRepository.save(userUpdate));
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
