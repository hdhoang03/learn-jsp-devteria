package com.devteria.Demo_Spring_boot.service;

import com.devteria.Demo_Spring_boot.constaint.PredefinedRole;
import com.devteria.Demo_Spring_boot.dto.request.PasswordCreationRequest;
import com.devteria.Demo_Spring_boot.dto.request.UserCreationRequest;
import com.devteria.Demo_Spring_boot.dto.request.UserUpdateRequest;
import com.devteria.Demo_Spring_boot.dto.response.UserResponse;
import com.devteria.Demo_Spring_boot.entity.Role;
import com.devteria.Demo_Spring_boot.entity.User;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j //ghi log
@RequiredArgsConstructor // khỏi cần đánh dấu autowired, tạo constructor mà b iến đánh dấu là final sẽ tự động đưa vào constructor và DI
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserMapper userMapper;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    RedisService redisService;

    public UserResponse createUser(UserCreationRequest request){
        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);

        try {//nếu tồn tại nhả lỗi userexisted và ngược lại sẽ lưu user để cho dbms làm chứ không cần làm thủ công
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception){
            if(userRepository.existsByEmail(request.getEmail())){
                throw new AppException(ErrorCode.EMAIL_EXISTED);
            }
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        return userMapper.toUserResponse(user);
    }

    public void createPassword(PasswordCreationRequest request){ //tao password moi khi dang nhap tu google
        var getContext = SecurityContextHolder.getContext();//lấy context hiện tại của người dùng đang đăng nhập
        String getName = getContext.getAuthentication().getName();//lấy tên của người dùng đang đăng nhập

        User user = userRepository.findByUsername(getName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        //kiem tra user co password chua
        if(StringUtils.hasText(user.getPassword())) throw new AppException(ErrorCode.PASSWORD_EXISTED);
        user.setPassword(passwordEncoder.encode(request.getPassword()));//ma hoa password
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")//Kiểm tra nếu role là admin mới chạy hàm này, phân quyền theo role
//    @PreAuthorize("hasAuthority('APPROVE_POST')")//để phân quyền theo permission
    public List<UserResponse> getUser(){//Trả về user
        log.info("In method get users");//ghi log
        return userRepository.findAll().stream()
                .map(user -> userMapper.toUserResponse(user)).toList();//userMapper::toUserResponse
    }

//    @PostAuthorize("returnObject.username == authentication.name")//lấy user từ id nếu username của mình là username đang đăng nhập thì có thể trả về thông tin của mình
    //Dùng PreAuthorize vẫn tốt hơn
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserbyId(String id) {
        log.info("In method getUserbyId.");

        //Kiểm tra xem thông tin người dùng đã lưu trong Redis chưa, nếu có sẽ lấy trong Redis ngược lại truy vấn db
        String redisKey = "user:" + id;
        Object cacheUser = redisService.getValue(redisKey);
        if(cacheUser != null){
            return (UserResponse) cacheUser;
        }

        UserResponse user = userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND)));

        //Lưu vào Redis
        redisService.setValue(redisKey, user, 2);//Lưu cache Redis 2 phút
        return user;
    }

    //theo lastName
    public UserResponse getUserByLastName(String lastName){
        log.error("In method getUserbyLastName.");
        return userMapper.toUserResponse(userRepository.findByLastName(lastName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_LASTNAME)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserByEmail(String email){
        log.error("In method getUserbyEmail");

        String redisKey = "user:email:" + email;
        UserResponse cacheUser = (UserResponse) redisService.getValue(redisKey);
//        Object cacheUser = redisService.getValue(redisKey);
        if(cacheUser != null){
            return (UserResponse) cacheUser;
        }

        UserResponse user = userMapper.toUserResponse(userRepository.findByEmail(email).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND)));

        redisService.setValue(redisKey, user, 3);
        return user;
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("lastName not found"));
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
        String redisKey = "user:myInfo:" + getName;

        Object cacheUser = redisService.getValue(redisKey);
        if(cacheUser != null){
            return (UserResponse) cacheUser;
        }
//        List<String> roles = getContext.getAuthentication().getAuthorities()
//                .stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList());

        User user = userRepository.findByUsername(getName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var userResponse = userMapper.toUserResponse(user);
        redisService.setValue(redisKey, userResponse, 2);
        userResponse.setNoPassword(!StringUtils.hasText(user.getPassword()));//get thong tin coi user co password chua
        return userResponse;
    }
    //Viết ngắn gọn hơn cách trên
    public UserResponse getMyInfo2(){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(userName).map(userMapper::toUserResponse)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
