package com.devteria.Demo_Spring_boot.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class AuthenticationRequest {
    String username;
    String password;
}
