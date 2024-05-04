package com.monolithic.demo.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.monolithic.demo.dto.response.AuthenticationResponse;
import com.monolithic.demo.dto.response.UserResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;





@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)

//Chuẩn API Response
public class ApiResponse <T> {
    int code = 1000;
    String message;
    T result;

}


