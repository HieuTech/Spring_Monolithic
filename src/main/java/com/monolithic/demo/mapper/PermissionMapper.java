package com.monolithic.demo.mapper;

import org.mapstruct.Mapper;

import com.monolithic.demo.dto.request.PermissionRequest;
import com.monolithic.demo.dto.response.PermissionResponse;
import com.monolithic.demo.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
