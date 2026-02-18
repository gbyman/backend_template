package app.backend.core.jwt.domain.refreshtoken.repository;

import org.springframework.data.repository.CrudRepository;

import app.backend.core.jwt.domain.refreshtoken.entity.RefreshTokenEntity;

public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, String> {}
