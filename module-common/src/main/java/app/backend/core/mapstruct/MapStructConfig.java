package app.backend.core.mapstruct;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct 공통 설정 모든 Mapper 인터페이스에서 이 설정을 상속받아 사용합니다.
 *
 * <p>사용 예시:
 *
 * <pre>
 * @Mapper(config = MapStructConfig.class)
 * public interface UserMapper {
 *     UserDto toDto(UserEntity entity);
 * }
 * </pre>
 */
@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING, // Spring Bean으로 등록
        unmappedTargetPolicy = ReportingPolicy.IGNORE // 매핑되지 않은 필드 무시
        )
public interface MapStructConfig {}
