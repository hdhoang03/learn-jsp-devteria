package com.devteria.Demo_Spring_boot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Role {
    @Id//đánh trường khóa chính là name
    String name;
    String description;

    @ManyToMany//biểu diễn quan hệ n - n
    Set<Permission> permissions;
}
