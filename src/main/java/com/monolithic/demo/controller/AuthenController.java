package com.monolithic.demo.controller;

import java.text.ParseException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monolithic.demo.dto.request.*;
import com.monolithic.demo.dto.response.AuthenticationResponse;
import com.monolithic.demo.dto.response.IntroSpectResponse;
import com.monolithic.demo.service.AuthenService;
import com.nimbusds.jose.JOSEException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenController {
    AuthenService authenService;

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenRequest authenRequest) {
        var result = authenService.authenticated(authenRequest);

        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntroSpectResponse> introSpectResponse(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenService.IntroSpect(request);
        return ApiResponse.<IntroSpectResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) {
        this.authenService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(this.authenService.refreshToken(request))
                .build();
    }
}
