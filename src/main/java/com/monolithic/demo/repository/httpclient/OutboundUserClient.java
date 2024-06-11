package com.monolithic.demo.repository.httpclient;

import com.monolithic.demo.dto.request.ExchangeTokenRequest;
import com.monolithic.demo.dto.response.ExchangeTokenResponse;
import com.monolithic.demo.dto.response.OutboundUserResponse;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@FeignClient(name = "outbound-identity-client", url = "https://www.googleapis.com")
public interface OutboundUserClient {
    @GetMapping(value = "/oauth2/v1/userinfo")
    OutboundUserResponse getUserInfo(@RequestParam("alt") String alt,
                                     @RequestParam("access_token") String access_token
                                        );



}
