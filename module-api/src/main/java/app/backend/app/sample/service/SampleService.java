package app.backend.app.sample.service;

import java.util.List;

import app.backend.app.sample.dto.SampleReqDto;
import app.backend.app.sample.dto.SampleRespDto;

public interface SampleService {
    SampleRespDto createSample(SampleReqDto.Create reqDto);

    SampleRespDto getSample(Long id);

    List<SampleRespDto> getAllSamples();

    SampleRespDto updateSample(Long id, SampleReqDto.Update reqDto);

    void deleteSample(Long id);

    // MyBatis 사용 예제
    List<SampleRespDto> getAllSamplesByMyBatis();

    SampleRespDto getSampleByMyBatis(Long id);
}
