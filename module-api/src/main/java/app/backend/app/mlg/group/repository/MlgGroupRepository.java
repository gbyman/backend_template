package app.backend.app.mlg.group.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.backend.app.mlg.group.entity.MlgGroupEntity;

public interface MlgGroupRepository
        extends JpaRepository<MlgGroupEntity, String>, MlgQueryRepository {

    Optional<MlgGroupEntity> findTopByOrderByMlgCodeValDesc();
}
