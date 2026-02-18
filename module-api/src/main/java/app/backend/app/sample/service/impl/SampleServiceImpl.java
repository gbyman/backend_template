package app.backend.app.sample.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.backend.app.sample.dto.SampleReqDto;
import app.backend.app.sample.dto.SampleRespDto;
import app.backend.app.sample.entity.SampleEntity;
import app.backend.app.sample.mapper.SampleMapper;
import app.backend.app.sample.mapstruct.SampleMapStruct;
import app.backend.app.sample.repository.SampleRepository;
import app.backend.app.sample.service.SampleService;
import app.backend.core.base.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SampleServiceImpl implements SampleService {

    private final SampleRepository sampleRepository;
    private final SampleMapper sampleMapper;
    private final SampleMapStruct sampleMapStruct;

    @Override
    @Transactional
    public SampleRespDto createSample(SampleReqDto.Create reqDto) {
        SampleEntity entity =
                SampleEntity.builder()
                        .title(reqDto.getTitle())
                        .content(reqDto.getContent())
                        .build();

        SampleEntity saved = sampleRepository.save(entity);
        return sampleMapStruct.toDto(saved);
    }

    @Override
    @Cacheable(value = "sample", key = "#id")
    public SampleRespDto getSample(Long id) {
        SampleEntity entity =
                sampleRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new BizException(
                                                HttpStatus.NOT_FOUND,
                                                "NOT_FOUND",
                                                "샘플을 찾을 수 없습니다."));

        return sampleMapStruct.toDto(entity);
    }

    @Override
    public List<SampleRespDto> getAllSamples() {
        return sampleRepository.findByUseYn("Y").stream()
                .map(sampleMapStruct::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "sample", key = "#id")
    public SampleRespDto updateSample(Long id, SampleReqDto.Update reqDto) {
        SampleEntity entity =
                sampleRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new BizException(
                                                HttpStatus.NOT_FOUND,
                                                "NOT_FOUND",
                                                "샘플을 찾을 수 없습니다."));

        entity.update(reqDto.getTitle(), reqDto.getContent());
        return sampleMapStruct.toDto(entity);
    }

    @Override
    @Transactional
    @CacheEvict(value = "sample", key = "#id")
    public void deleteSample(Long id) {
        if (!sampleRepository.existsById(id)) {
            throw new BizException(HttpStatus.NOT_FOUND, "NOT_FOUND", "샘플을 찾을 수 없습니다.");
        }
        sampleRepository.deleteById(id);
    }

    @Override
    public List<SampleRespDto> getAllSamplesByMyBatis() {
        return sampleMapper.findAll();
    }

    @Override
    public SampleRespDto getSampleByMyBatis(Long id) {
        SampleRespDto result = sampleMapper.findById(id);
        if (result == null) {
            throw new BizException(HttpStatus.NOT_FOUND, "NOT_FOUND", "샘플을 찾을 수 없습니다.");
        }
        return result;
    }
}
