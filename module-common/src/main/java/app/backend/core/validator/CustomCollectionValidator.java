package app.backend.core.validator;

import java.util.Collection;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import jakarta.validation.Validation;

/**
 * Collection 내부 요소에 대한 Bean Validation을 수행하는 Validator
 *
 * <p>Spring의 @Valid는 Collection 자체는 검증하지만 내부 요소의 제약조건은 검증하지 않습니다. 이 Validator는 Collection의 각 요소에 대해
 * Bean Validation을 수행합니다.
 *
 * <p>사용 예시:
 *
 * <pre>
 * @RestController
 * @RequiredArgsConstructor
 * public class UserController {
 *
 *     private final CustomCollectionValidator collectionValidator;
 *
 *     @PostMapping("/users/batch")
 *     public ResponseEntity&lt;?&gt; createUsers(
 *             @RequestBody List&lt;UserCreateDto&gt; users,
 *             Errors errors) {
 *
 *         // Collection 내부 요소 검증
 *         collectionValidator.validate(users, errors);
 *
 *         if (errors.hasErrors()) {
 *             return ResponseEntity.badRequest().body(errors.getAllErrors());
 *         }
 *
 *         // 비즈니스 로직 수행
 *         return ResponseEntity.ok().build();
 *     }
 * }
 * </pre>
 *
 * <p>또는 @InitBinder와 함께 사용:
 *
 * <pre>
 * @InitBinder
 * protected void initBinder(WebDataBinder binder) {
 *     binder.addValidators(collectionValidator);
 * }
 *
 * @PostMapping("/users/batch")
 * public ResponseEntity&lt;?&gt; createUsers(@Valid @RequestBody List&lt;UserCreateDto&gt; users) {
 *     // Collection 내부 요소가 자동으로 검증됨
 *     return ResponseEntity.ok().build();
 * }
 * </pre>
 */
@Component
public class CustomCollectionValidator implements Validator {

    private final SpringValidatorAdapter validator;

    public CustomCollectionValidator() {
        this.validator =
                new SpringValidatorAdapter(
                        Validation.buildDefaultValidatorFactory().getValidator());
    }

    /**
     * 모든 클래스를 지원 (Collection과 일반 객체 모두)
     *
     * @param clazz 검증할 클래스
     * @return 항상 true
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    /**
     * 객체 검증 수행
     *
     * <p>대상이 Collection이면 각 요소를 검증하고, 일반 객체면 객체 자체를 검증합니다.
     *
     * @param target 검증할 대상 (Collection 또는 일반 객체)
     * @param errors 검증 오류를 저장할 Errors 객체
     */
    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof Collection<?> collection) {
            validateCollection(collection, errors);
        } else {
            validator.validate(target, errors);
        }
    }

    /**
     * Collection의 각 요소에 대해 Bean Validation 수행
     *
     * @param collection 검증할 컬렉션
     * @param errors 검증 오류를 저장할 Errors 객체
     */
    private void validateCollection(Collection<?> collection, Errors errors) {
        int index = 0;
        for (Object object : collection) {
            if (object != null) {
                // 각 요소를 개별적으로 검증
                errors.pushNestedPath("[" + index + "]");
                try {
                    validator.validate(object, errors);
                } finally {
                    errors.popNestedPath();
                }
            }
            index++;
        }
    }
}
