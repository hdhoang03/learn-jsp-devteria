package com.devteria.Demo_Spring_boot.dto.response;

import com.devteria.Demo_Spring_boot.entity.Role;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String username;
//    String password; //thực tế không ai trả về password
    String firstName;
    String lastName;
    LocalDate dob;
    Set<RoleResponse> roles;
}
