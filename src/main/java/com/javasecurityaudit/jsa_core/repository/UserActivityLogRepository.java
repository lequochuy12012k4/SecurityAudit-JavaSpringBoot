package com.javasecurityaudit.jsa_core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javasecurityaudit.jsa_core.entity.UserActivityLog;

public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, String> {
    
}
