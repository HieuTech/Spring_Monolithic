package com.monolithic.demo.service;


import com.monolithic.demo.dto.request.UserCreationRequest;
import com.monolithic.demo.dto.request.UserUpdateRequest;
import com.monolithic.demo.dto.response.UserResponse;
import com.monolithic.demo.entity.User;
import com.monolithic.demo.exception.AppException;
import com.monolithic.demo.exception.ErrorCode;
import com.monolithic.demo.mapper.UserMapper;
import com.monolithic.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//Nhung cai Autowired se dc khai bao trong constructor
public class UserService  {

    /*
    * Ban chat cua Autowired
    * public UserService(final UserRepository userRepository,
    * final UserMapper userMapper
    * ){
    * this.userRepository= userRepository
    * this.userMapper = userMapper
    * }
    * */


    private UserRepository userRepository;

    private UserMapper userMapper;



    public UserResponse createUser(UserCreationRequest request){

        if(userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        //dung de khoi tao doi tuong va truyen tham so nhanh chong
//        Student student = Student.builder()
//                .firstName("John")
//                .lastName("Doe")
//                .age(25)
//                .address("123 Main St")
//                .email("john.doe@example.com")
//                .build();


//        User user = new User();
//        user.setUsername(request.getUserName());
//        user.setPassword(request.getPassword());
//        user.setFirstName(request.getFirstName());
//        user.setLastName(request.getLastName());
//        user.setDob(request.getDob());
        User user = userMapper.toUser(request);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getPassword()));



        //ko dc reponse về cho user đối tượng trong Entity, mà phải response về DTO.
        //Cho nên khi có user rồi phải map các thuộc tính của user vào thuộc tính của response DTO
        return userMapper.toResponse(userRepository.save(user));
    }

    public List<UserResponse> findAll(){
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();

    }

    public UserResponse findById(String id){
        return userMapper.toResponse(userRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOTFOUND)));

    }


    public UserResponse updateUser(String id, UserUpdateRequest updateRequest){

        User userUpdate = this.userRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.USER_NOTFOUND));

        userMapper.updateUser(userUpdate, updateRequest);

        return userMapper.toResponse(userRepository.save(userUpdate));
    }

    public void deleteUser(String id){
         userRepository.deleteById(id);
    }


}
