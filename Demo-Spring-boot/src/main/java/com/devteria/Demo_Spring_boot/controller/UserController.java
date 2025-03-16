package com.devteria.Demo_Spring_boot.controller;


import com.devteria.Demo_Spring_boot.dto.request.ApiResponse;
import com.devteria.Demo_Spring_boot.dto.request.UserCreationRequest;
import com.devteria.Demo_Spring_boot.dto.request.UserUpdateRequest;
import com.devteria.Demo_Spring_boot.dto.response.UserResponse;
import com.devteria.Demo_Spring_boot.entity.User;
import com.devteria.Demo_Spring_boot.repository.UserRepository;
import com.devteria.Demo_Spring_boot.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController //Đánh dấu là 1 controller
@RequestMapping("/users") //Sử dụng nhiều nên định nghĩa 1 lần, cần post, get, put, delete thì gọi annotation ở dưới thôi
@Slf4j
public class UserController {
    //    @Autowired //(DI) tiêm userservice
//    private final UserService userService;
    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createdUser(@RequestBody @Valid UserCreationRequest request) { //validate object
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getUsers() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("User name: {}", authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUser())
                .build();
    }

    @GetMapping("/myInfo") //trả về thông tin của chính người dùng
    ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .result(userService.getMyInfo())
                .build();
    }

    @GetMapping("/by_lastname/{lastName}") // dùng /by_lastname để tránh trùng đường dẫn
//    User getUserbyLastName(@PathVariable("lastName") String lastName){ //có thể không cần tường minh nếu đặt tên ở String giống với mapping api
//        //(@PathVariable String userId)
//        return userService.getUserByLastName(lastName);
//    }
    ApiResponse<UserResponse> getUserbyLastName(@PathVariable String lastName){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserByLastName(lastName))
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUserbyId(@PathVariable String userId){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserbyId(userId))
                .build();
    }

    @GetMapping("/by_email/{email}")
    ApiResponse<UserResponse> getUserByEmail(@PathVariable String email){
        return ApiResponse.<UserResponse>builder()
                .code(6666)
                .result(userService.getUserByEmail(email))
                .build();
    }

    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable String userId){
        userService.deleteUser(userId);
        return ApiResponse.<String>builder()
                .result("User has been deleted.")
                .build();
    }

}
