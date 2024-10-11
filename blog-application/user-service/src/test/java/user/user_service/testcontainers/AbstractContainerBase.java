//package user.user_service.testcontainers;
//
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.PostgreSQLContainer;
//
//public abstract class AbstractContainerBase {
//    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER;
//
//    static {
//        POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:latest")
//                .withDatabaseName("userdb")
//                .withUsername("bacarturk")
//                .withPassword("bacarturk");
//
//        POSTGRESQL_CONTAINER.start();
//
//    }
//
//    @DynamicPropertySource
//    public static void dynamicPropertySource(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
//        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
//        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
//    }
//}
