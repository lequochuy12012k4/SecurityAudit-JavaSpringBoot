package com.javasecurityaudit.jsa_core.repository.JPA;

import org.springframework.data.repository.CrudRepository;

import com.javasecurityaudit.jsa_core.entity.BlacklistToken;

public interface BlacklistTokenRepository extends CrudRepository<BlacklistToken, String> {

}
