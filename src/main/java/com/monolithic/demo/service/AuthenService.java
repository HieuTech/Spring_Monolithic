package com.monolithic.demo.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import com.monolithic.demo.constant.PredefinedRole;
import com.monolithic.demo.dto.request.*;
import com.monolithic.demo.entity.Roles;
import com.monolithic.demo.repository.httpclient.OutboundIdentityClient;
import com.monolithic.demo.repository.httpclient.OutboundUserClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.monolithic.demo.dto.response.AuthenticationResponse;
import com.monolithic.demo.dto.response.IntroSpectResponse;
import com.monolithic.demo.entity.InvalidatedToken;
import com.monolithic.demo.entity.User;
import com.monolithic.demo.exception.AppException;
import com.monolithic.demo.exception.ErrorCode;
import com.monolithic.demo.repository.InvalidTokenRepository;
import com.monolithic.demo.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenService {
    InvalidTokenRepository invalidatedTokenRepository;
    UserRepository userRepository;
    OutboundIdentityClient outboundIdentityClient;
    OutboundUserClient outboundUserClient;
    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;


    @NonFinal
    @Value("${outbound.client-id}")
    protected String clientId;
    @NonFinal
    @Value("${outbound.secret-id}")
    protected String secretId;
    @NonFinal
    @Value("${outbound.uri-redirect}")
    protected String uriRedirect;
    @NonFinal
    @Value("${outbound.grant-type}")
    protected String grantType;

    public AuthenticationResponse outboundAuthenResponse(String code) {
        var response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(clientId)
                .clientSecret(secretId)
                .redirectUri(uriRedirect)
                .grantType(grantType)
                .build());

        log.info("TOKEN RESPONSE {}" + response);

        var userInfo = outboundUserClient.getUserInfo("json", response.getAccessToken());

        Set<Roles> roles = new HashSet<>();
        roles.add(Roles.builder()
                .name(PredefinedRole.USER_ROLE)
                .build());

//        onBoard
        var user = userRepository.findByUsername(userInfo.getEmail()).orElseGet(()-> userRepository.save(User.builder()
                        .username(userInfo.getName())
                        .firstName(userInfo.getFamilyName())
                        .lastName(userInfo.getGivenName())
                        .roles(roles)
                .build()));

        var token = generateToken(user);


        //Exchange Google Token To Server Token
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    // key ko dc de static vì nó sẽ chạy trước hàm main, chưa kịp config để gán giá trị
    public IntroSpectResponse IntroSpect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);

        } catch (AppException e) {
            isValid = false;
        }
        return IntroSpectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse authenticated(AuthenRequest authenRequest) {
        String name = authenRequest.getUsername();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean result = passwordEncoder.matches(authenRequest.getPassword(), user.getPassword());
        if (!result) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        var token = generateToken(user);
        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    public void logout(LogoutRequest request) {
        var signToken = verifyToken(request.getToken(), false);

        try {
            String jwtId = signToken.getJWTClaimsSet().getJWTID();
            Date expriedDate = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jwtId)
                    .expiredTime(expriedDate)
                    .build();

            this.invalidatedTokenRepository.save(invalidatedToken);

        } catch (AppException | ParseException e) {
            log.info("Token Already Expired");
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) {
        try {
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);
            log.info("Thong tin token" + signedJWT);

            Date expiriedTime = (isRefresh)
                    ? new Date(signedJWT
                    .getJWTClaimsSet()
                    .getIssueTime()
                    .toInstant()
                    .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                    .toEpochMilli())
                    : signedJWT.getJWTClaimsSet().getExpirationTime();
            // Kiem tra key
            var verified = signedJWT.verify(verifier);

            if (!(verified && expiriedTime.after(new Date()))) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
            if (invalidatedTokenRepository.existsById(
                    signedJWT.getJWTClaimsSet().getJWTID())) throw new AppException(ErrorCode.UNAUTHENTICATED);

            return signedJWT;

        } catch (JOSEException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) {
        // Khi user gửi yêu cầu refresh, thì invalid token cũ.
        // giải mã  token

        var signedJwt = verifyToken(request.getToken(), true);

        try {
            var jwtId = signedJwt.getJWTClaimsSet().getJWTID();
            var expiredDate = signedJwt.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .expiredTime(expiredDate)
                    .id(jwtId)
                    .build();
            this.invalidatedTokenRepository.save(invalidatedToken);

            // và lấy ra thông tin user trong token cũ đó để cấp cho họ 1 token mới
            String name = signedJwt.getJWTClaimsSet().getSubject();

            User user = this.userRepository
                    .findByUsername(name)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));

            String token = generateToken(user);

            return AuthenticationResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .build();

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateToken(User user) {

        // Cau hinh header
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        // Cau Hinh payload
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("vegan.peace.world")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        // Chi luu cac token da log out,

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        // Cho vao payload, header vao JWS
        JWSObject jwsObject = new JWSObject(header, payload);
        // Cau hinh Sign Key
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        // Bởi vì các scope nằm trong mảng ["admin", "user", "manager"].
        // String joiner sẽ ghép nó về thành 1 String duy nhất ngăn cách nhau bởi những khoảng trắng
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(roles -> {
                stringJoiner.add("ROLE_" + roles.getName());
                if (!CollectionUtils.isEmpty(roles.getPermissions())) {
                    roles.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
                }
            });
        }
        return stringJoiner.toString();
    }
}
