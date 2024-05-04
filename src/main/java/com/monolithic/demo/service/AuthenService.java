package com.monolithic.demo.service;

import com.monolithic.demo.dto.request.AuthenRequest;
import com.monolithic.demo.dto.request.IntrospectRequest;
import com.monolithic.demo.dto.response.AuthenticationResponse;
import com.monolithic.demo.dto.response.IntroSpectResponse;
import com.monolithic.demo.entity.User;
import com.monolithic.demo.exception.AppException;
import com.monolithic.demo.exception.ErrorCode;
import com.monolithic.demo.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenService {
    UserRepository userRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String  SIGNER_KEY;

    //key ko dc de static vì nó sẽ chạy trước hàm main, chưa kịp config để gán giá trị
    public IntroSpectResponse IntroSpect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        System.out.println("Key"+ SIGNER_KEY);

        //truyen vao key de Ma Hoa
        JWSVerifier jwsVerifier = new MACVerifier(SIGNER_KEY.getBytes());

        //kiem tra key trong token
        //Parse header và payload của token để kiếm tra thông tin.
        SignedJWT signedJWT = SignedJWT.parse(token);
        //kiem tra het han
        Date expiredTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        //kiem tra chu ki, tra ve true false
        var verified = signedJWT.verify(jwsVerifier);
        return IntroSpectResponse.builder()
                .valid(verified && expiredTime.after(new Date()))
                .build();
    }

    public AuthenticationResponse authenticated(AuthenRequest authenRequest) {
        String name = authenRequest.getUsername();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean result = passwordEncoder.matches(authenRequest.getPassword(), user.getPassword());
        if (!result) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        var token = generateToken(name);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    private String generateToken(String name) {

        //Cau hinh header
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        //Cau Hinh payload
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(name)
                .issuer("vegan.peace.world")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        //Cho vao payload, header vao JWS
        JWSObject jwsObject = new JWSObject(header, payload);
        //Cau hinh Sign Key
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }

    }
}
