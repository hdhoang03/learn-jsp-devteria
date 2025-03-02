package com.devteria.Demo_Spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data //tạo tự động getter, setter, toString, equals, hashCode bằng lombok mà không cần tạo thủ công như ở dưới
@NoArgsConstructor //tạo constructor không tham số
@AllArgsConstructor //tạo constructor có tất cả tham số
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String username;
    String password;
    String firstName;
    LocalDate dob;
    String lastName;
//    Set<String> roles; //set đảm bảo các phần tử không trùng lặp và tốc độ tìm kiếm nhanh hơn list

    @ManyToMany
    Set<Role> roles;
}
