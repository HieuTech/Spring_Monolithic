package com.monolithic.demo.controller;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monolithic.demo.dto.request.UserCreationRequest;
import com.monolithic.demo.dto.response.UserResponse;
import com.monolithic.demo.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
// tao ra 1 mock request đến controller
@AutoConfigureMockMvc
public class UserControllerTest {

    // Tao các mock Request
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserCreationRequest request;

    private UserResponse response;
    private LocalDate dob;

    //        phụ thuộc bên third party thì ko cần phải moc
    //    @BeforeEach
    //    void initData() {
    //        dob = LocalDate.of(2022, 2, 2);
    //
    //        request = UserCreationRequest.builder()
    //                .username("user1")
    //                .firstName("Java")
    //                .dob(dob)
    //                .lastName("Hieu")
    //                .password("1231")
    //                .build();
    //        response = UserResponse.builder()
    //                .id("b65e6fa7-4f86-47db-a4a0-db6eed108e18")
    //                .username("user1")
    //                .firstName("Java")
    //                .dob(dob)
    //                .lastName("Hieu").
    //                build();
    //    }

    @Test
    // tra ve status isOk va ma code = 1000
    void createUser_success() {
        LocalDate dob = LocalDate.of(2022, 01, 01);
        request = UserCreationRequest.builder()
                .username("user1")
                .firstName("Java")
                .password("1231")
                .lastName("java")
                .dob(dob)
                .build();

        response = UserResponse.builder()
                .id("b65e6fa7-4f86-47db-a4a0-db6eed108e18")
                .username("user1")
                .firstName("Java")
                .lastName("java")
                .dob(dob)
                .build();

        // GIVEN
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {

            String content = objectMapper.writeValueAsString(response);
            // gia lap service
            Mockito.when(userService.createUser(ArgumentMatchers.any())).thenReturn(response);
            // when, then
            // Bat dau test
            mockMvc.perform(MockMvcRequestBuilders.post("/users")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(content))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("result.id").value("b65e6fa7-4f86-47db-a4a0-db6eed108e18"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    // tra ve status 400 bad request, va ma code # 1000
    void createUser_userName_failed() {

        request = UserCreationRequest.builder()
                .username("1")
                .firstName("Java")
                .password("1231")
                .lastName("java")
                .dob(dob)
                .build();

        //        response = UserResponse.builder().
        //                id("b65e6fa7-4f86-47db-a4a0-db6eed108e18")
        //                .username("a")
        //                .firstName("Java")
        //                .lastName("java")
        //                .dob(dob).
        //                build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        try {

            String content = objectMapper.writeValueAsString(request);

            // Gia lap service
            //            Mockito.when(userService.createUser(ArgumentMatchers.any())).thenReturn(response);
            // bat dau test
            mockMvc.perform(MockMvcRequestBuilders.post("/users")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(content))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("code").value(1003))
                    .andExpect(MockMvcResultMatchers.jsonPath("message")
                            .value("Username must be at least 3 and 5 characters"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
