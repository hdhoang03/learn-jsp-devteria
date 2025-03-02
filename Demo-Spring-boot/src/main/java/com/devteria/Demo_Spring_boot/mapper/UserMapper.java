//package com.devteria.Demo_Spring_boot.mapper;
//
//import com.devteria.Demo_Spring_boot.dto.request.UserCreationRequest;
//import com.devteria.Demo_Spring_boot.dto.request.UserUpdateRequest;
//import com.devteria.Demo_Spring_boot.dto.response.UserResponse;
//import com.devteria.Demo_Spring_boot.entity.User;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.MappingTarget;
//
//@Mapper(componentModel = "spring") //MapStruct sẽ tự động tạo ra một bean Spring và sử dụng @Autowired để tiêm vào bất kỳ đâu trong Spring.
//public interface UserMapper {
//    User toUser(UserCreationRequest request);
//
////    @Mapping(source = "firstName", target = "lastName") //map cho firstname = lastname
//    //nếu để ource = "firstName", ignore = true thì lastname sẽ null
//
//    UserResponse toUserResponse(User user);
//
//    @Mapping(target = "roles", ignore = true)
//    void updateUser(@MappingTarget User user, UserUpdateRequest request);//map userupdaterequest vào trong dối tượng User
//}

package com.devteria.Demo_Spring_boot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.devteria.Demo_Spring_boot.dto.request.UserCreationRequest;
import com.devteria.Demo_Spring_boot.dto.request.UserUpdateRequest;
import com.devteria.Demo_Spring_boot.dto.response.UserResponse;
import com.devteria.Demo_Spring_boot.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true) //ignore roles vì khác trường dữ liệu, mapper vào sẽ gặp lỗi khác dữ liệu
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
