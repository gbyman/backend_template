# Test Configuration

## Test Libraries

This project includes the following test dependencies:

### Core Testing
- **JUnit 5 (Jupiter)** - Test framework
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions
- **Spring Boot Test** - Spring context testing
- **Spring Security Test** - Security testing

### Additional Testing
- **H2 Database** - In-memory database for testing
- **REST Assured** - REST API testing
- **TestContainers** (optional) - Docker containers for integration testing

## Test Configuration Files

- `application-test.yml` - Test profile configuration
  - Uses H2 in-memory database
  - PostgreSQL compatibility mode
  - JPA `ddl-auto: create-drop`
  - Flyway disabled

## Running Tests

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :module-api:test

# Run tests with coverage
./gradlew test jacocoTestReport

# Run tests excluding slow tests
./gradlew test -Dexclude.tags=slow
```

## Test Types

### 1. Unit Test
```java
@ExtendWith(MockitoExtension.class)
class ServiceTest {
    @Mock
    private Repository repository;

    @InjectMocks
    private ServiceImpl service;

    @Test
    void testMethod() {
        // Given
        given(repository.findById(1L)).willReturn(Optional.of(entity));

        // When
        Result result = service.method(1L);

        // Then
        assertThat(result).isNotNull();
        verify(repository).findById(1L);
    }
}
```

### 2. Integration Test (Controller)
```java
@WebMvcTest(Controller.class)
@ActiveProfiles("test")
class ControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Service service;

    @Test
    @WithMockUser
    void testEndpoint() throws Exception {
        mockMvc.perform(get("/api/endpoint"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data").exists());
    }
}
```

### 3. Repository Test
```java
@DataJpaTest
@ActiveProfiles("test")
class RepositoryTest {
    @Autowired
    private Repository repository;

    @Test
    void testQuery() {
        // Given
        Entity entity = repository.save(Entity.builder().build());

        // When
        Optional<Entity> found = repository.findById(entity.getId());

        // Then
        assertThat(found).isPresent();
    }
}
```

### 4. REST API Test (REST Assured)
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class ApiTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void testApi() {
        given()
            .contentType(JSON)
        .when()
            .get("/api/endpoint")
        .then()
            .statusCode(200)
            .body("data", notNullValue());
    }
}
```

## Best Practices

1. **Use @ActiveProfiles("test")** - Always use test profile
2. **Isolate tests** - Each test should be independent
3. **Use meaningful names** - Test method names should describe what they test
4. **Follow AAA pattern** - Arrange, Act, Assert
5. **Mock external dependencies** - Use mocks for external services
6. **Test edge cases** - Test null, empty, invalid inputs
7. **Use @DisplayName** - Add descriptive display names

## Common Annotations

- `@SpringBootTest` - Full Spring context
- `@WebMvcTest` - Only web layer
- `@DataJpaTest` - Only JPA components
- `@MockBean` - Mock Spring bean
- `@WithMockUser` - Mock authenticated user
- `@Transactional` - Rollback after test (default in @DataJpaTest)

## Test Coverage

Generate coverage report:
```bash
./gradlew jacocoTestReport
```

View report:
```
build/reports/jacoco/test/html/index.html
```
