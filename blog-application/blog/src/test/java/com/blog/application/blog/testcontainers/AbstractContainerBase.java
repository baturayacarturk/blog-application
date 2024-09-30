//package com.blog.application.blog.testcontainers;
//
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.KafkaContainer;
//import org.testcontainers.containers.MySQLContainer;
//import org.testcontainers.containers.RabbitMQContainer;
//import org.testcontainers.containers.wait.strategy.Wait;
//import org.testcontainers.elasticsearch.ElasticsearchContainer;
//import org.testcontainers.utility.DockerImageName;
//
//public abstract class AbstractContainerBase {
//
//    static final MySQLContainer<?> MY_SQL_CONTAINER;
//    static final ElasticsearchContainer ELASTICSEARCH_CONTAINER;
//    static final KafkaContainer KAFKA_CONTAINER;
//    static final RabbitMQContainer RABBITMQ_CONTAINER;
//
//    static {
//        MY_SQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
//                .withDatabaseName("testdb")
//                .withUsername("bloguser")
//                .withPassword("baturayacarturk")
//                .withExposedPorts(3306)
//                .withCommand("--default-authentication-plugin=mysql_native_password")
//                .waitingFor(Wait.forListeningPort());
//
//        ELASTICSEARCH_CONTAINER = new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.17.10"))
//                .withExposedPorts(9200)
//                .waitingFor(Wait.forListeningPort());
//
//        KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
//                .withExposedPorts(9092)
//                .waitingFor(Wait.forListeningPort());
//
//        RABBITMQ_CONTAINER = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3-management"))
//                .withExposedPorts(5672, 15672)
//                .waitingFor(Wait.forListeningPort());
//
//        try {
//            System.out.println("Starting MySQL container...");
//            MY_SQL_CONTAINER.start();
//            System.out.println("MySQL container started.");
//
//            System.out.println("Starting Elasticsearch container...");
//            ELASTICSEARCH_CONTAINER.start();
//            System.out.println("Elasticsearch container started.");
//
//            System.out.println("Starting Kafka container...");
//            KAFKA_CONTAINER.start();
//            System.out.println("Kafka container started.");
//
//            System.out.println("Starting RabbitMQ container...");
//            RABBITMQ_CONTAINER.start();
//            System.out.println("RabbitMQ container started.");
//        } catch (Exception e) {
//            System.err.println("Error starting containers:");
//            e.printStackTrace();
//            if (MY_SQL_CONTAINER.isRunning()) {
//                System.err.println("MySQL container logs:");
//                System.err.println(MY_SQL_CONTAINER.getLogs());
//            }
//            throw new RuntimeException("Could not start containers", e);
//        }
//    }
//
//    @DynamicPropertySource
//    public static void dynamicPropertySource(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", () -> String.format("jdbc:mysql://%s:%d/testdb?useSSL=false", MY_SQL_CONTAINER.getHost(), MY_SQL_CONTAINER.getFirstMappedPort()));
//        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
//        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
//        registry.add("spring.elasticsearch.uris", () -> String.format("http://%s:%d", ELASTICSEARCH_CONTAINER.getHost(), ELASTICSEARCH_CONTAINER.getFirstMappedPort()));
//        registry.add("spring.kafka.bootstrap-servers", () -> String.format("%s:%d", KAFKA_CONTAINER.getHost(), KAFKA_CONTAINER.getFirstMappedPort()));
//        registry.add("spring.rabbitmq.host", RABBITMQ_CONTAINER::getHost);
//        registry.add("spring.rabbitmq.port", () -> RABBITMQ_CONTAINER.getMappedPort(5672));
//    }
//}