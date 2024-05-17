package com.monolithic.demo.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;

import com.monolithic.demo.validation.DobConstraint;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

// tao ra builder cho class cho DTO
public class UserCreationRequest {

    @Size(min = 3, max = 5, message = "USERNAME_INVALID")
    String username;

    @Size(min = 2, max = 5, message = "INVALID_PASSWORD")
    String password;

    String firstName;
    String lastName;

    @DobConstraint(min = 2, max = 4, message = "INVALID_DOB")
    LocalDate dob;
}
