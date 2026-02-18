package app.backend.app.sample.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import app.backend.app.sample.entity.SampleEntity;

public interface SampleRepository extends JpaRepository<SampleEntity, Long> {
    List<SampleEntity> findByUseYn(String useYn);
}
