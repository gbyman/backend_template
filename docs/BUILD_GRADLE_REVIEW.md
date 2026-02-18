# Build.gradle 검토 보고서

## 📋 검토 일시
- **날짜:** 2026-02-17
- **대상 파일:**
  - `/build.gradle` (루트)
  - `/module-api/build.gradle`
  - `/module-batch/build.gradle`
  - `/module-common/build.gradle`

---

## ✅ 현재 구조 분석

### 1. 루트 build.gradle (273줄)
- **역할:** 프로젝트 전체 설정 및 코드 품질 도구
- **플러그인:** Spring Boot, Checkstyle, PMD, SpotBugs, Spotless, Jacoco
- **특징:**
  - ✅ Git Hooks 자동 설치
  - ✅ Nexus/Maven Central repository 동적 선택
  - ✅ 코드 품질 도구 통합 설정
  - ✅ 환경별 리소스 디렉토리 지원 (local/dev/prod)

### 2. module-api/build.gradle (114줄)
- **역할:** REST API 모듈
- **주요 의존성:**
  - module-common (기본)
  - JWT (0.12.6)
  - MyBatis (3.0.4)
  - Flyway (DB 마이그레이션)
  - AWS S3 (2.31.3)
  - Azure Blob (12.29.1)
  - Email (spring-boot-starter-mail)
  - Rate Limiting (Resilience4j 2.2.0)

### 3. module-batch/build.gradle (44줄)
- **역할:** 배치 작업 모듈
- **주요 의존성:**
  - module-common (기본)
  - Spring Batch
  - Quartz Scheduler
  - ShedLock (분산 락, 5.10.0)

### 4. module-common/build.gradle (63줄)
- **역할:** 공통 라이브러리 (java-library)
- **주요 의존성 (api 스코프):**
  - Spring Web, Security, Validation
  - JPA, Redis, QueryDSL
  - AOP, P6Spy
  - Swagger (2.8.5)
  - Apache Commons, POI
  - Jasypt, Logstash

---

## ⚠️ 발견된 문제점

### 1. 중복 코드 (HIGH)

#### 문제: module-api와 module-batch의 bootJar/bootWar 설정 중복

**module-api/build.gradle:**
```gradle
apply plugin: 'war'

bootJar {
    enabled = true
}

bootWar {
    enabled = false
}

war {
    enabled = false
}
```

**module-batch/build.gradle:**
```gradle
apply plugin: 'war'

bootJar {
    enabled = true
}

bootWar {
    enabled = false
}

war {
    enabled = false
}
```

**개선 방안:**
```gradle
// build.gradle (루트) - subprojects 블록에 추가
configure(subprojects.findAll { it.name in ['module-api', 'module-batch'] }) {
    apply plugin: 'war'

    bootJar {
        enabled = true
    }

    bootWar {
        enabled = false
    }

    war {
        enabled = false
    }
}
```

---

### 2. 의존성 버전 관리 (MEDIUM)

#### 문제: 버전이 여러 곳에 하드코딩됨

**현재:**
```gradle
// module-api/build.gradle
implementation 'commons-io:commons-io:2.18.0'
implementation 'software.amazon.awssdk:s3:2.31.3'
implementation 'com.azure:azure-storage-blob:12.29.1'

// module-batch/build.gradle
implementation 'net.javacrumbs.shedlock:shedlock-spring:5.10.0'

// module-common/build.gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5'
implementation 'org.apache.poi:poi-ooxml:5.2.5'
```

**개선 방안:**
```gradle
// build.gradle (루트) - ext 블록에 추가
ext {
    mapstructVersion = '1.6.3'

    // 추가 버전 변수
    commonsIoVersion = '2.18.0'
    awsSdkVersion = '2.31.3'
    azureBlobVersion = '12.29.1'
    shedlockVersion = '5.10.0'
    springdocVersion = '2.8.5'
    poiVersion = '5.2.5'
    resilience4jVersion = '2.2.0'
}

// 각 모듈에서 사용
implementation "commons-io:commons-io:${commonsIoVersion}"
implementation "software.amazon.awssdk:s3:${awsSdkVersion}"
```

---

### 3. QueryDSL 설정 중복 (MEDIUM)

#### 문제: module-api와 module-common 모두에 QueryDSL annotationProcessor 설정

**module-api/build.gradle:**
```gradle
annotationProcessor "com.querydsl:querydsl-apt:${dependencyManagement.importedProperties['querydsl.version']}:jakarta"
annotationProcessor "jakarta.annotation:jakarta.annotation-api"
annotationProcessor "jakarta.persistence:jakarta.persistence-api"
```

**module-common/build.gradle:**
```gradle
api "com.querydsl:querydsl-jpa:${dependencyManagement.importedProperties['querydsl.version']}:jakarta"
annotationProcessor "com.querydsl:querydsl-apt:${dependencyManagement.importedProperties['querydsl.version']}:jakarta"
annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
```

**분석:**
- module-common이 이미 QueryDSL을 설정함
- module-api에서 Entity를 정의하는 경우에만 annotationProcessor 필요
- module-api의 Entity가 없다면 제거 가능

**확인 필요:**
```bash
# module-api에 Entity가 있는지 확인
find module-api -name "*Entity.java" -type f
```

---

### 4. Windows 관련 설정 누락 (LOW)

#### 문제: module-api에만 isWindows 조건부 설정

**module-api/build.gradle:**
```gradle
def isWindows = System.getProperty('os.name').toLowerCase().contains('windows')

sourceSets {
    main {
        if (!isWindows) {
            java {
                srcDirs += 'build/generated/sources/annotationProcessor/java/main'
            }
        }
    }
}

clean {
    if (isWindows) {
        delete file('src/main/generated')
    } else {
        delete file('build/generated/sources/annotationProcessor')
    }
}
```

**분석:**
- module-common도 QueryDSL, MapStruct을 사용하므로 동일한 설정 필요 가능성
- module-batch는 필요 없을 수 있음 (Entity가 없으면)

**개선 방안:**
```gradle
// build.gradle (루트) - subprojects 블록에 추가
def isWindows = System.getProperty('os.name').toLowerCase().contains('windows')

configure(subprojects.findAll { it.name in ['module-api', 'module-common'] }) {
    sourceSets {
        main {
            if (!isWindows) {
                java {
                    srcDirs += 'build/generated/sources/annotationProcessor/java/main'
                }
            }
        }
    }

    clean {
        if (isWindows) {
            delete file('src/main/generated')
        } else {
            delete file('build/generated/sources/annotationProcessor')
        }
    }
}
```

---

### 5. 선택적 의존성 관리 (LOW)

#### 문제: 사용하지 않을 수 있는 의존성이 기본 포함

**module-api/build.gradle:**
```gradle
// AWS S3 (S3 저장소를 사용하지 않으면 이 의존성과 s3 패키지를 삭제하세요)
implementation 'software.amazon.awssdk:s3:2.31.3'

// Azure Blob Storage (Azure를 사용하지 않으면 이 의존성과 azure 패키지를 삭제하세요)
implementation 'com.azure:azure-storage-blob:12.29.1'

// Email (이메일 기능을 사용하지 않으면 이 의존성과 mail 패키지를 삭제하세요)
implementation 'org.springframework.boot:spring-boot-starter-mail'
```

**분석:**
- AWS S3, Azure Blob, Email은 선택적 기능
- 실제로 사용하지 않으면 불필요한 의존성으로 빌드 시간, JAR 크기 증가

**권장 사항:**
1. 실제 사용 중인 기능 확인
2. 사용하지 않는 의존성 제거
3. 필요 시 주석 처리로 변경

```gradle
// 클라우드 스토리지 (필요한 것만 선택)
// implementation 'software.amazon.awssdk:s3:2.31.3'  // AWS S3 사용 시 활성화
// implementation 'com.azure:azure-storage-blob:12.29.1'  // Azure 사용 시 활성화

// 이메일 (사용 시 활성화)
// implementation 'org.springframework.boot:spring-boot-starter-mail'
```

---

### 6. TestContainers 설정 (INFO)

#### 현재 상태: 주석 처리

**module-api/build.gradle:**
```gradle
// TestContainers (실제 DB 컨테이너로 통합 테스트 - 선택적)
// 필요시 주석 해제
// testImplementation 'org.testcontainers:testcontainers:1.19.0'
// testImplementation 'org.testcontainers:postgresql:1.19.0'
// testImplementation 'org.testcontainers:junit-jupiter:1.19.0'
```

**권장 사항:**
- 통합 테스트가 필요하면 활성화
- 현재 H2 Database로 테스트 중이므로 당장은 불필요

---

### 7. MapStruct annotationProcessor 중복 (MEDIUM)

#### 문제: module-api와 module-common 모두 설정

**module-api/build.gradle:**
```gradle
annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
testAnnotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
```

**module-common/build.gradle:**
```gradle
api "org.mapstruct:mapstruct:${mapstructVersion}"
annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
```

**분석:**
- module-common이 api로 mapstruct를 제공
- module-api에서 Mapper 인터페이스를 정의하는 경우에만 annotationProcessor 필요
- 실제 사용 패턴 확인 필요

---

## 📊 의존성 통계

### module-api (가장 많은 의존성)
- **총 의존성:** ~25개
- **선택적 의존성:** 3개 (AWS S3, Azure Blob, Email)
- **테스트 의존성:** 6개

### module-batch (가장 적은 의존성)
- **총 의존성:** ~6개
- **주요 기능:** Spring Batch, Quartz, ShedLock

### module-common (공통 라이브러리)
- **총 의존성:** ~20개
- **api 스코프:** 대부분 (다른 모듈에 전이)

---

## 🎯 개선 우선순위

### Priority 1 (HIGH) - 즉시 적용 권장

1. **의존성 버전 통합 관리**
   - ext 블록에 모든 버전 변수 추가
   - 유지보수성 향상

2. **중복 코드 제거**
   - bootJar/bootWar 설정 통합
   - Windows 관련 설정 통합

### Priority 2 (MEDIUM) - 검토 후 적용

3. **QueryDSL 설정 최적화**
   - module-api의 Entity 존재 여부 확인
   - 불필요한 annotationProcessor 제거

4. **MapStruct 설정 검토**
   - 실제 Mapper 사용 패턴 확인
   - 중복 설정 제거

### Priority 3 (LOW) - 선택적 적용

5. **선택적 의존성 정리**
   - 사용하지 않는 AWS S3, Azure Blob, Email 제거
   - 필요 시 주석으로 변경

6. **TestContainers 결정**
   - 통합 테스트 전략 수립
   - 필요 시 활성화

---

## 📝 개선 체크리스트

### 즉시 적용 가능 (Breaking Change 없음)

- [x] ext 블록에 버전 변수 추가 ✅ **완료** (2026-02-17)
- [x] bootJar/bootWar 설정 통합 ✅ **완료** (2026-02-17)
- [x] Windows 설정 통합 (필요한 모듈만) ✅ **완료** (2026-02-17)
  - module-api, module-common에만 적용 (annotationProcessor 사용)
  - module-batch는 제외 (Entity 없음)

### 검토 후 적용 (코드 확인 필요)

- [x] module-api Entity 존재 여부 확인 → QueryDSL 설정 결정 ✅ **완료** (2026-02-17)
  - Entity 6개 존재: UserEntity, MlgDetailEntity, MlgGroupEntity, SampleEntity, MailHistoryEntity, RefreshTokenEntity
  - QueryDSL 설정 유지 (module-api, module-common 모두 필요)
- [x] module-api Mapper 사용 여부 확인 → MapStruct 설정 결정 ✅ **완료** (2026-02-17)
  - MapStruct Mapper 3개 존재: MlgDetailMapStruct, MlgGroupMapStruct, SampleMapStruct
  - module-common의 MapStruct annotationProcessor 제거 (Config만 제공)
- [ ] AWS S3 실제 사용 여부 확인
- [ ] Azure Blob 실제 사용 여부 확인
- [ ] Email 기능 실제 사용 여부 확인

### 장기 검토 (프로젝트 전략)

- [ ] TestContainers 도입 여부 결정
- [ ] 통합 테스트 전략 수립
- [ ] CI/CD에서 코드 품질 검사 활용 방안

---

## 💡 추가 권장 사항

### 1. gradle.properties 활용

**현재:** 버전이 build.gradle 여러 곳에 분산

**개선:**
```properties
# gradle.properties
# Dependency Versions
mapstructVersion=1.6.3
commonsIoVersion=2.18.0
awsSdkVersion=2.31.3
azureBlobVersion=12.29.1
shedlockVersion=5.10.0
springdocVersion=2.8.5
poiVersion=5.2.5
resilience4jVersion=2.2.0
```

### 2. 의존성 BOM 활용

Spring Boot BOM을 활용하여 호환되는 버전 자동 관리:
```gradle
// Spring Boot가 관리하는 의존성은 버전 명시 불필요
implementation 'org.springframework.boot:spring-boot-starter-mail'
```

### 3. 코드 품질 리포트 통합

현재 여러 곳에 분산된 리포트를 통합 리포트로 제공:
```gradle
task codeQualityReport {
    group = 'reporting'
    description = '모든 코드 품질 리포트를 한 곳에 모음'
    // 구현 필요
}
```

---

## 📌 결론

### 현재 상태: **양호 (Good)**

- ✅ 모듈 분리가 명확함
- ✅ 코드 품질 도구가 잘 설정됨
- ✅ 의존성 관리가 대체로 양호함

### 개선 필요 사항:

1. **의존성 버전 통합 관리** (HIGH)
2. **중복 코드 제거** (HIGH)
3. **QueryDSL/MapStruct 설정 최적화** (MEDIUM)
4. **선택적 의존성 정리** (LOW)

### 예상 효과:

- 유지보수성 향상 📈
- 빌드 시간 단축 ⏱️
- JAR 크기 감소 💾
- 코드 가독성 향상 👁️

---

## 🎉 적용 완료 사항 (2026-02-17)

### HIGH Priority ✅

#### 1. 의존성 버전 통합 관리
**변경 파일:** `build.gradle` (루트)
- ext 블록에 15개 버전 변수 추가
- 모든 모듈에서 중앙 집중식 버전 관리

**추가된 버전 변수:**
```gradle
mybatisVersion, p6spyVersion, awsSdkVersion, azureBlobVersion,
shedlockVersion, springdocVersion, resilience4jVersion,
commonsIoVersion, commonsValidatorVersion, jsoupVersion, poiVersion,
jasyptVersion, logstashVersion, logbackElasticsearchVersion
```

#### 2. 중복 코드 제거 (bootJar/bootWar)
**변경 파일:** `build.gradle`, `module-api/build.gradle`, `module-batch/build.gradle`
- 38줄 중복 코드 → 27줄 통합 (29% 감소)
- 루트 build.gradle의 subprojects 블록 내부로 이동

**효과:**
- ✅ 유지보수성 향상
- ✅ 일관성 강화
- ✅ 빌드 시간: 2분 (변경 없음, 정상)

---

### MEDIUM Priority ✅

#### 3. QueryDSL 설정 검토
**조사 결과:**
- module-api: Entity 6개 존재 → annotationProcessor **필요**
- module-common: BaseEntity 존재 → annotationProcessor **필요**
- **결론:** 현재 설정 유지 (변경 없음)

#### 4. MapStruct 설정 최적화
**변경 파일:** `module-common/build.gradle`
- module-common의 MapStruct annotationProcessor 제거

**조사 결과:**
- module-api: MapStruct Mapper 3개 존재 → annotationProcessor **필요**
  - `MlgDetailMapStruct`, `MlgGroupMapStruct`, `SampleMapStruct`
- module-common: MapStructConfig만 존재 → annotationProcessor **불필요**

**변경 내용:**
```gradle
// 이전
api "org.mapstruct:mapstruct:${mapstructVersion}"
annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"

// 이후 (module-common)
api "org.mapstruct:mapstruct:${mapstructVersion}"
// annotationProcessor 제거 (Config만 제공, 실제 Mapper 없음)
```

**검증:**
- ✅ BUILD SUCCESSFUL (1m 40s)
- ✅ module-api의 MapStructImpl 파일 3개 정상 생성
- ✅ module-common에서 불필요한 코드 생성 제거

---

### HIGH Priority (추가) ✅

#### 5. Windows 설정 통합
**변경 파일:** `build.gradle` (루트), `module-api/build.gradle`
- module-api의 Windows 설정 제거 (20줄)
- 루트 build.gradle의 subprojects 블록에 통합 (25줄)

**조사 결과:**
- module-api: QueryDSL Q-class 6개 생성 → Windows 설정 **필요**
- module-common: QueryDSL Q-class 2개 생성 (QBaseEntity, QSysLog) → Windows 설정 **필요**
- module-batch: Entity 없음, Q-class 없음 → Windows 설정 **불필요**

**변경 내용:**
```gradle
// build.gradle (루트) - subprojects 블록 내부
if (project.name in ['module-api', 'module-common']) {
    def isWindows = System.getProperty('os.name').toLowerCase().contains('windows')

    sourceSets {
        main {
            if (!isWindows) {
                java {
                    srcDirs += 'build/generated/sources/annotationProcessor/java/main'
                }
            }
        }
    }

    clean {
        if (isWindows) {
            delete file('src/main/generated')
        } else {
            delete file('build/generated/sources/annotationProcessor')
        }
    }
}
```

**검증:**
- ✅ BUILD SUCCESSFUL (1m 39s)
- ✅ module-api: Q-class 파일 정상 생성
- ✅ module-common: Q-class 파일 정상 생성
- ✅ module-batch: annotationProcessor 실행되지 않음 (정상)

---

**작성자:** Claude Code
**버전:** 3.0
**최종 수정:** 2026-02-17 (HIGH + MEDIUM 우선순위 완료)
