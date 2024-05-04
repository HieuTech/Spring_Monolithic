package com.monolithic.demo.mapper;


import com.monolithic.demo.dto.request.UserCreationRequest;
import com.monolithic.demo.dto.request.UserUpdateRequest;
import com.monolithic.demo.dto.response.UserResponse;
import com.monolithic.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    //map các field trong UserRequest vào User
    User toUser(UserCreationRequest request);

    UserResponse toResponse(User user);

//    Phải map request sang user, rồi từ user mới map sang userResponse

    void updateUser(@MappingTarget User user, UserUpdateRequest request);


}
