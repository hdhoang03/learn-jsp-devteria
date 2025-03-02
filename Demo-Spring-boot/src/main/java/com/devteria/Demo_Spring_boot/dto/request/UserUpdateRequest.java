package com.devteria.Demo_Spring_boot.dto.request;

import com.devteria.Demo_Spring_boot.validator.DobConstraint;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String password;
    String firstName;
    String lastName;

    @DobConstraint(min = 2, message = "INVALID_DOB")//annotation đánh dấu validate ngày sinh
    LocalDate dob;
    List<String> roles;
}
