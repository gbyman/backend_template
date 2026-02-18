package app.backend.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통합 테스트 Base 클래스
 *
 * <p>전체 스프링 컨텍스트를 로드하여 통합 테스트를 수행합니다. 각 테스트는 트랜잭션으로 감싸져 자동 롤백됩니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestEntityManager
public abstract class BaseIntegrationTest {

    @Autowired protected TestEntityManager entityManager;

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
