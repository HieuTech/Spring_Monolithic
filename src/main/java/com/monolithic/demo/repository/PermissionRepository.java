package com.monolithic.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monolithic.demo.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {}
