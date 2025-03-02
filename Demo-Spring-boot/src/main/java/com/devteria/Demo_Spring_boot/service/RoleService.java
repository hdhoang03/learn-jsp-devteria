package com.devteria.Demo_Spring_boot.service;

import com.devteria.Demo_Spring_boot.dto.request.RoleRequest;
import com.devteria.Demo_Spring_boot.dto.response.RoleResponse;
import com.devteria.Demo_Spring_boot.mapper.RoleMapper;
import com.devteria.Demo_Spring_boot.repository.PermissionRepository;
import com.devteria.Demo_Spring_boot.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;//DI
    PermissionRepository permissionRepository;//DI
    RoleMapper roleMapper;//DI

    public RoleResponse create(RoleRequest request){
        var role = roleMapper.toRole(request);

        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getAll(){
        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }

    public void delete(String role){
        roleRepository.deleteById(role);
    }
}
