package app.backend.batch;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * WAR 배포 시 외부 WAS(Tomcat, JBoss 등)의 진입점.
 *
 * <p>JAR 배포(내장 Tomcat)에서는 이 클래스가 사용되지 않으므로 삭제하지 않아도 무방하다.
 *
 * <p>WAR 배포 전환 방법:
 *
 * <ol>
 *   <li>build.gradle에서 {@code bootWar.enabled = true}, {@code bootJar.enabled = false} 로 변경
 *   <li>{@code ./gradlew :module-batch:bootWar -Pprofile=prod} 로 빌드
 *   <li>생성된 WAR 파일을 WAS의 webapps/ 폴더에 배포
 * </ol>
 */
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BatchApplication.class);
    }
}
