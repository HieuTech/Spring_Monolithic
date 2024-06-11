package com.monolithic.demo.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OutboundUserResponse {
    String id;
    String email;
    Boolean verifiedEmail;
    String name;
    String givenName;
    String familyName;
    String picture;
}
