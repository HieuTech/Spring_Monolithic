package com.monolithic.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monolithic.demo.entity.Roles;

@Repository
public interface RoleRepository extends JpaRepository<Roles, String> {}
