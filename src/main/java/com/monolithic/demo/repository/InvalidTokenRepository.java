package com.monolithic.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monolithic.demo.entity.InvalidatedToken;

@Repository
public interface InvalidTokenRepository extends JpaRepository<InvalidatedToken, String> {}
