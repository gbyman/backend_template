# Nexus Repository Manager 설정 가이드

## 개요

Nexus Repository Manager를 사용하여 라이브러리 의존성을 관리하고, 사내 라이브러리를 배포할 수 있습니다.

---

## Nexus를 사용하는 이유

### 장점
- ✅ **빠른 빌드 속도**: 라이브러리를 캐싱하여 외부 다운로드 최소화
- ✅ **안정적인 빌드**: 외부 저장소 장애 시에도 빌드 가능
- ✅ **보안 강화**: 승인된 라이브러리만 사용 가능
- ✅ **사내 라이브러리 배포**: private 라이브러리 공유
- ✅ **라이선스 관리**: 라이브러리 라이선스 검토 가능
- ✅ **네트워크 트래픽 절감**: 중복 다운로드 방지

---

## 1. Nexus Repository 종류

### Proxy Repository
외부 저장소(Maven Central, Google 등)를 프록시하여 캐싱
```
maven-central-proxy → https://repo1.maven.org/maven2/
google-proxy → https://dl.google.com/dl/android/maven2/
gradle-plugin-portal-proxy → https://plugins.gradle.org/m2/
```

### Hosted Repository
사내 라이브러리를 저장하는 저장소
```
maven-releases → 릴리스 버전 라이브러리
maven-snapshots → 개발 중인 SNAPSHOT 라이브러리
```

### Group Repository
여러 저장소를 하나로 묶은 통합 저장소
```
maven-public (Group)
  ├── maven-central-proxy
  ├── google-proxy
  ├── maven-releases
  └── maven-snapshots
```

---

## 2. Gradle 설정

### Step 1: gradle.properties 생성

```bash
# 예시 파일 복사
cp gradle.properties.example gradle.properties
```

**gradle.properties:**
```properties
# Nexus Repository URL
nexusUrl=https://nexus.example.com/repository

# Maven Group Repository
mavenGroupUrl=${nexusUrl}/maven-public

# Maven Releases/Snapshots
mavenReleasesUrl=${nexusUrl}/maven-releases
mavenSnapshotsUrl=${nexusUrl}/maven-snapshots

# Gradle Plugin Portal Proxy
gradlePluginPortalUrl=${nexusUrl}/gradle-plugin-portal

# 인증 정보 (환경변수 사용 권장)
nexusUsername=${env.NEXUS_USERNAME}
nexusPassword=${env.NEXUS_PASSWORD}
```

### Step 2: 환경변수 설정

**로컬 개발 환경:**
```bash
# ~/.bashrc 또는 ~/.zshrc
export NEXUS_USERNAME=your-username
export NEXUS_PASSWORD=your-password
```

**CI/CD 환경:**
```yaml
# GitHub Actions
env:
  NEXUS_USERNAME: ${{ secrets.NEXUS_USERNAME }}
  NEXUS_PASSWORD: ${{ secrets.NEXUS_PASSWORD }}

# GitLab CI
variables:
  NEXUS_USERNAME: ${NEXUS_USERNAME}
  NEXUS_PASSWORD: ${NEXUS_PASSWORD}
```

**Docker Compose:**
```yaml
services:
  backend:
    environment:
      - NEXUS_USERNAME=${NEXUS_USERNAME}
      - NEXUS_PASSWORD=${NEXUS_PASSWORD}
```

### Step 3: build.gradle 확인

build.gradle이 자동으로 Nexus를 사용하도록 설정되어 있습니다:

```gradle
allprojects {
    repositories {
        if (project.hasProperty('nexusUrl')) {
            maven {
                url = project.property('mavenGroupUrl')
                credentials {
                    username = project.findProperty('nexusUsername')
                    password = project.findProperty('nexusPassword')
                }
            }
        } else {
            mavenCentral()
        }
    }
}
```

---

## 3. 빌드 테스트

### Nexus 사용 확인
```bash
# 의존성 다운로드 확인
./gradlew dependencies --refresh-dependencies

# 빌드
./gradlew clean build

# Nexus에서 가져온 라이브러리 확인
./gradlew dependencies | grep -i "nexus.example.com"
```

### 연결 실패 시 디버깅
```bash
# 상세 로그 출력
./gradlew clean build --info

# 스택트레이스 출력
./gradlew clean build --stacktrace

# 디버그 모드
./gradlew clean build --debug
```

---

## 4. 사내 라이브러리 배포

### Step 1: module-common을 라이브러리로 배포

**module-common/build.gradle에 추가:**
```gradle
plugins {
    id 'java-library'
    id 'maven-publish'
}

publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java
            groupId = 'app.backend'
            artifactId = 'common'
            version = project.version
        }
    }

    repositories {
        maven {
            name = 'nexus'
            url = project.version.endsWith('SNAPSHOT')
                ? project.property('mavenSnapshotsUrl')
                : project.property('mavenReleasesUrl')
            credentials {
                username = project.findProperty('publishUsername')
                password = project.findProperty('publishPassword')
            }
        }
    }
}
```

### Step 2: 배포 실행

```bash
# SNAPSHOT 버전 배포
./gradlew :module-common:publish

# Release 버전 배포
# 1. build.gradle에서 version = '1.0.0' 설정
# 2. 배포
./gradlew :module-common:publish
```

### Step 3: 다른 프로젝트에서 사용

```gradle
dependencies {
    implementation 'app.backend:common:1.0.0'
}
```

---

## 5. Nexus 서버 설정 (관리자)

### Docker Compose로 Nexus 실행

**docker-compose.nexus.yml:**
```yaml
version: '3.8'

services:
  nexus:
    image: sonatype/nexus3:latest
    container_name: nexus
    ports:
      - "8081:8081"
    volumes:
      - nexus-data:/nexus-data
    environment:
      - INSTALL4J_ADD_VM_PARAMS=-Xms1024m -Xmx2048m
    restart: unless-stopped

volumes:
  nexus-data:
    driver: local
```

**실행:**
```bash
docker-compose -f docker-compose.nexus.yml up -d

# 초기 admin 비밀번호 확인
docker exec nexus cat /nexus-data/admin.password
```

**접속:**
- URL: http://localhost:8081
- Username: admin
- Password: (위에서 확인한 비밀번호)

### Repository 생성 (Nexus UI)

#### 1. Maven Central Proxy 생성
1. **Settings** > **Repositories** > **Create repository**
2. **Recipe**: `maven2 (proxy)`
3. **Name**: `maven-central-proxy`
4. **Remote storage**: `https://repo1.maven.org/maven2/`
5. **Create repository**

#### 2. Maven Releases/Snapshots 생성
1. **Create repository** > `maven2 (hosted)`
2. **Name**: `maven-releases`
3. **Version policy**: `Release`
4. **Create repository**

동일하게:
- **Name**: `maven-snapshots`
- **Version policy**: `Snapshot`

#### 3. Maven Group 생성
1. **Create repository** > `maven2 (group)`
2. **Name**: `maven-public`
3. **Member repositories**:
   - maven-central-proxy
   - maven-releases
   - maven-snapshots
4. **Create repository**

#### 4. Gradle Plugin Portal Proxy 생성
1. **Create repository** > `maven2 (proxy)`
2. **Name**: `gradle-plugin-portal`
3. **Remote storage**: `https://plugins.gradle.org/m2/`

---

## 6. 인증 설정

### Nexus 사용자 생성 (관리자)
1. **Settings** > **Security** > **Users** > **Create local user**
2. **ID**: `build-user`
3. **Role**: `nx-repository-view-maven2-maven-public-browse`
4. **Create user**

### Role 권한 설정
```
Read: nx-repository-view-maven2-*-read
Browse: nx-repository-view-maven2-*-browse
Deploy: nx-repository-view-maven2-*-add (배포 권한 필요 시)
```

---

## 7. CI/CD 통합

### GitHub Actions

**.github/workflows/build.yml:**
```yaml
name: Build with Nexus

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Create gradle.properties
        run: |
          cat > gradle.properties << EOF
          nexusUrl=${{ secrets.NEXUS_URL }}
          mavenGroupUrl=\${nexusUrl}/maven-public
          nexusUsername=${{ secrets.NEXUS_USERNAME }}
          nexusPassword=${{ secrets.NEXUS_PASSWORD }}
          EOF

      - name: Build with Gradle
        run: ./gradlew clean build
```

### GitLab CI

**.gitlab-ci.yml:**
```yaml
build:
  stage: build
  image: gradle:8.5-jdk21
  before_script:
    - |
      cat > gradle.properties << EOF
      nexusUrl=${NEXUS_URL}
      mavenGroupUrl=\${nexusUrl}/maven-public
      nexusUsername=${NEXUS_USERNAME}
      nexusPassword=${NEXUS_PASSWORD}
      EOF
  script:
    - ./gradlew clean build
```

---

## 8. 트러블슈팅

### 인증 실패
```
Could not resolve dependencies...
Received status code 401 from server: Unauthorized
```

**해결방법:**
- Nexus 사용자명/비밀번호 확인
- 환경변수 `NEXUS_USERNAME`, `NEXUS_PASSWORD` 확인
- Nexus 사용자 권한 확인

### 연결 실패
```
Could not GET 'https://nexus.example.com/...'
Connection refused
```

**해결방법:**
- Nexus 서버가 실행 중인지 확인
- 네트워크/방화벽 설정 확인
- URL이 올바른지 확인

### SSL 인증서 오류
```
PKIX path building failed
```

**해결방법:**
```gradle
maven {
    url = project.property('mavenGroupUrl')
    allowInsecureProtocol = true  // HTTP 허용 (개발 환경만)
}
```

또는 SSL 인증서를 Java Keystore에 추가:
```bash
keytool -import -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts \
  -storepass changeit -alias nexus -file nexus.crt
```

---

## 9. 성능 최적화

### Gradle 캐시 활성화

**gradle.properties:**
```properties
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.daemon=true
```

### Nexus 캐시 설정

Nexus UI에서:
1. **Repository** > **maven-central-proxy** > **Configuration**
2. **Maximum component age**: `1440` (1일)
3. **Maximum metadata age**: `1440`

---

## 10. 보안 모범 사례

### ✅ 권장사항
- 환경변수로 인증 정보 관리
- gradle.properties를 .gitignore에 포함
- CI/CD Secrets 사용
- Nexus HTTPS 사용
- 정기적인 비밀번호 변경
- 최소 권한 원칙 (Least Privilege)

### ❌ 주의사항
- gradle.properties에 비밀번호 하드코딩 금지
- Git에 인증 정보 커밋 금지
- HTTP 사용 금지 (운영 환경)
- admin 계정 공유 금지

---

## 참고 문서

- [Nexus Repository OSS Documentation](https://help.sonatype.com/repomanager3)
- [Gradle Publishing](https://docs.gradle.org/current/userguide/publishing_maven.html)
- [Maven Coordinates](https://maven.apache.org/guides/mini/guide-naming-conventions.html)
