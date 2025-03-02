package com.devteria.Demo_Spring_boot.service;

import com.devteria.Demo_Spring_boot.dto.request.PermissionRequest;
import com.devteria.Demo_Spring_boot.dto.response.PermissionResponse;
import com.devteria.Demo_Spring_boot.entity.Permission;
import com.devteria.Demo_Spring_boot.mapper.PermissionMapper;
import com.devteria.Demo_Spring_boot.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;//DI
    PermissionMapper permissionMapper;//DI

    public PermissionResponse create(PermissionRequest request){
        Permission permission = permissionMapper.toPermission(request);//map data từ request vào permission
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }
    public List<PermissionResponse> getAll(){
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    public void delete(String permission){
        permissionRepository.deleteById(permission);
    }
}
