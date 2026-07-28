package com.javasecurityaudit.jsa_core.entity;

import java.util.Date;

import org.springframework.data.redis.core.RedisHash;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RedisHash("BlacklistToken")
public class BlacklistToken {
    @Id
    String id;
    Date expiryTime;
}