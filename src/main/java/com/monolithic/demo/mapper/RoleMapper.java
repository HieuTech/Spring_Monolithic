package com.monolithic.demo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.monolithic.demo.dto.request.RoleRequest;
import com.monolithic.demo.dto.response.RoleResponse;
import com.monolithic.demo.entity.Roles;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Roles toRole(RoleRequest request);

    RoleResponse toRoleResponse(Roles role);
}
