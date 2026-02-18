package app.backend.core.jpa.querydsl;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.Assert;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;

/**
 * Querydsl 4.x 버전을 위한 Repository Support 기본 클래스 Spring Data JPA의 QuerydslRepositorySupport를 개선한
 * 버전입니다.
 *
 * <p>사용 예시:
 *
 * <pre>
 * public class UserRepositoryImpl extends Querydsl4RepositorySupport implements UserRepositoryCustom {
 *     public UserRepositoryImpl(EntityManager em) {
 *         super(UserEntity.class, em);
 *     }
 *
 *     public Page<UserDto> searchUsers(Pageable pageable) {
 *         return applyPagination(pageable,
 *             query -> query.selectFrom(qUser).where(qUser.name.contains("test")),
 *             query -> query.select(qUser.count()).from(qUser)
 *         );
 *     }
 * }
 * </pre>
 */
public abstract class Querydsl4RepositorySupport {

    private final EntityManager entityManager;
    private final Querydsl querydsl;
    private final JPAQueryFactory queryFactory;

    /**
     * Querydsl4RepositorySupport 생성자
     *
     * @param domainClass 엔티티 클래스
     * @param entityManager EntityManager
     */
    public Querydsl4RepositorySupport(Class<?> domainClass, EntityManager entityManager) {
        Assert.notNull(domainClass, "Domain class must not be null!");
        Assert.notNull(entityManager, "EntityManager must not be null!");

        JpaEntityInformation<?, ?> entityInformation =
                JpaEntityInformationSupport.getEntityInformation(domainClass, entityManager);

        SimpleEntityPathResolver resolver = SimpleEntityPathResolver.INSTANCE;
        EntityPath<?> path = resolver.createPath(entityInformation.getJavaType());

        this.entityManager = entityManager;
        this.querydsl =
                new Querydsl(entityManager, new PathBuilder<>(path.getType(), path.getMetadata()));
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @PostConstruct
    public void validate() {
        Assert.notNull(entityManager, "EntityManager must not be null!");
        Assert.notNull(querydsl, "Querydsl must not be null!");
        Assert.notNull(queryFactory, "QueryFactory must not be null!");
    }

    protected JPAQueryFactory getQueryFactory() {
        return queryFactory;
    }

    protected Querydsl getQuerydsl() {
        return querydsl;
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    protected <T> JPAQuery<T> select(Expression<T> expr) {
        return getQueryFactory().select(expr);
    }

    protected <T> JPAQuery<T> selectFrom(EntityPath<T> from) {
        return getQueryFactory().selectFrom(from);
    }

    /** 페이징 적용 (count 쿼리 자동 생성) */
    protected <T> Page<T> applyPagination(
            Pageable pageable, Function<JPAQueryFactory, JPAQuery<T>> contentQuery) {
        JPAQuery<T> jpaQuery = contentQuery.apply(getQueryFactory());
        List<T> content = getQuerydsl().applyPagination(pageable, jpaQuery).fetch();
        return PageableExecutionUtils.getPage(content, pageable, () -> jpaQuery.fetch().size());
    }

    /** 페이징 적용 (count 쿼리 별도 지정) */
    protected <T> Page<T> applyPagination(
            Pageable pageable,
            Function<JPAQueryFactory, JPAQuery<T>> contentQuery,
            Function<JPAQueryFactory, JPAQuery<Long>> countQuery) {

        JPAQuery<T> jpaContentQuery = contentQuery.apply(getQueryFactory());
        List<T> content = getQuerydsl().applyPagination(pageable, jpaContentQuery).fetch();
        Long total = countQuery.apply(getQueryFactory()).fetchOne();

        return PageableExecutionUtils.getPage(content, pageable, () -> total != null ? total : 0L);
    }

    /** 페이징 적용 (pagingYn으로 페이징 여부 제어) */
    protected <T> Page<T> applyPagination(
            Pageable pageable,
            boolean pagingYn,
            Function<JPAQueryFactory, JPAQuery<T>> contentQuery,
            Function<JPAQueryFactory, JPAQuery<Long>> countQuery) {

        JPAQueryFactory queryFactory = getQueryFactory();
        JPAQuery<T> jpaContentQuery = contentQuery.apply(queryFactory);

        if (!pagingYn || pageable.isUnpaged()) {
            // 페이징 안 하고 전체 조회
            List<T> content = jpaContentQuery.fetch();
            return PageableExecutionUtils.getPage(
                    content, Pageable.unpaged(), () -> (long) content.size());
        }

        // 페이징 O
        List<T> content = getQuerydsl().applyPagination(pageable, jpaContentQuery).fetch();
        Long total = countQuery.apply(queryFactory).fetchOne();
        return PageableExecutionUtils.getPage(content, pageable, () -> total != null ? total : 0L);
    }
}
