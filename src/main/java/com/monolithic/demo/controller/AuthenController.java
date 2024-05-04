package com.monolithic.demo.controller;

import com.monolithic.demo.dto.request.ApiResponse;
import com.monolithic.demo.dto.request.AuthenRequest;
import com.monolithic.demo.dto.request.IntrospectRequest;
import com.monolithic.demo.dto.response.AuthenticationResponse;
import com.monolithic.demo.dto.response.IntroSpectResponse;
import com.monolithic.demo.entity.User;
import com.monolithic.demo.service.AuthenService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenController {
    AuthenService authenService;

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenRequest authenRequest) {
        var result = authenService.authenticated(authenRequest);

        //cach 1 thong thuong
//        //Chuẩn bị pattern trả về cho user theo chuẩn response
//        ApiResponse apiResponse = new ApiResponse();
//        //chuẩn bị dữ liệu dto gằn vào pattern trả về cho client;
//        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
//        authenticationResponse.setAuthenticated(isAuthen);
//        apiResponse.setResult(authenticationResponse);
        //cach 2 : builder
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }
    @PostMapping("/introspect")
    ApiResponse<IntroSpectResponse> introSpectResponse(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenService.IntroSpect(request);
        return ApiResponse.<IntroSpectResponse>builder()
                .result(result)
                .build();
    }

}
