package com.devteria.Demo_Spring_boot.service;

import com.devteria.Demo_Spring_boot.dto.request.UserCreationRequest;
import com.devteria.Demo_Spring_boot.dto.request.UserUpdateRequest;
import com.devteria.Demo_Spring_boot.dto.response.UserResponse;
import com.devteria.Demo_Spring_boot.entity.User;
import com.devteria.Demo_Spring_boot.enums.Role;
import com.devteria.Demo_Spring_boot.exception.AppException;
import com.devteria.Demo_Spring_boot.exception.ErrorCode;
import com.devteria.Demo_Spring_boot.mapper.UserMapper;
import com.devteria.Demo_Spring_boot.repository.RoleRepository;
import com.devteria.Demo_Spring_boot.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.mapping.Set;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j //ghi log
@RequiredArgsConstructor // khỏi cần đánh dấu autowired, tạo constructor mà b iến đánh dấu là final sẽ tự động đưa vào constructor và DI
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
//    @Autowired //DI tiêm userresponsitory
//    private final UserRepository userRepository;
//    @Autowired
//    private final UserMapper userMapper;
    UserMapper userMapper;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    public UserResponse createUser(UserCreationRequest request){
        // Kiểm tra xem username đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername()))
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"); //không dùng vì có class xử lý lỗi rồi
//            throw new RuntimeException("User existed.");
            throw new AppException(ErrorCode.USER_EXISTED); //throw exception với 1 errorcode đã định nghĩa, ở đây là USER_EXISTED
//            throw new RuntimeException("ErrorCode.USER_EXITED");

        User user = userMapper.toUser(request);

//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);//độ khó mã hóa (đã đánh dấu bean bên class SecurityConfig)
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());

//        user.setRoles(roles);
//          thay vì tạo thủ công bằng tay sẽ map dữ liệu từ CreateUserRequest sang user rồi gọi toUser truyền request đó vào
//        user.setUsername(request.getUsername());
//        user.setPassword(request.getPassword());
//        user.setFirstName(request.getFirstName());
//        user.setLastName(request.getLastName());
//        user.setDob(request.getDob());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")//Kiểm tra nếu role là admin mới chạy hàm này, phân quyền theo role
//    @PreAuthorize("hasAuthority('APPROVE_POST')")//để phân quyền theo permission
    public List<UserResponse> getUser(){//Trả về user
        log.info("In method get users");//ghi log
        return userRepository.findAll().stream()
                .map(user -> userMapper.toUserResponse(user)).toList();//userMapper::toUserResponse
    }

    @PostAuthorize("returnObject.username == authentication.name")//lấy user từ id nếu username của mình là username đang đăng nhập thì có thể trả về thông tin của mình
    //Dùng PreAuthorize vẫn tốt hơn
    public UserResponse getUserbyId(String id) {
        log.info("In method getUserbyId.");
        return userMapper.toUserResponse(userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND)));
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
    //theo lastName
    public UserResponse getUserByLastName(String lastName){
        log.error("In method getUserbyLastName.");
        return userMapper.toUserResponse(userRepository.findByLastName(lastName).orElseThrow(() -> new AppException(ErrorCode.USER_LASTNAME)));
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "lastName not found"));
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("lastName not found"));
        userMapper.updateUser(user, request);

//      thay vì làm từng dòng thì làm vậy sẽ nhanh hơn
//        user.setPassword(request.getPassword());
//        user.setFirstName(request.getFirstName());
//        user.setLastName(request.getLastName());
//        user.setDob(request.getDob());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));//Lưu lại user sau khi update
    }

    public void deleteUser(String userId){
        userRepository.deleteById(userId);
    }

    //trả về thông tin chủ tài khoản
    public UserResponse getMyInfo(){
        var getContext = SecurityContextHolder.getContext();//lấy context hiện tại của người dùng đang đăng nhập
        String getName = getContext.getAuthentication().getName();//lấy tên của người dùng đang đăng nhập
//        List<String> roles = getContext.getAuthentication().getAuthorities()
//                .stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList());

        User user = userRepository.findByUsername(getName).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }
    //Viết ngắn gọn hơn cách trên
    public UserResponse getMyInfo2(){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(userName).map(userMapper::toUserResponse)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
