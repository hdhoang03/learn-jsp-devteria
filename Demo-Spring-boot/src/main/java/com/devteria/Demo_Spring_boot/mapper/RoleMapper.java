package com.devteria.Demo_Spring_boot.mapper;

import com.devteria.Demo_Spring_boot.dto.request.RoleRequest;
import com.devteria.Demo_Spring_boot.dto.response.RoleResponse;
import com.devteria.Demo_Spring_boot.entity.Role;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)// do bên RoleRequest là Set<String> permission còn bên entity Role là một Set<Permission> nên phải ignore đi không mapping trường permission
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
