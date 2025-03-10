package com.devteria.Demo_Spring_boot.mapper;

import com.devteria.Demo_Spring_boot.dto.request.PermissionRequest;
import com.devteria.Demo_Spring_boot.dto.response.PermissionResponse;
import com.devteria.Demo_Spring_boot.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
