package com.blog.application.blog;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main entry point of the Blog Application.
 * <p>
 * This class is the entry point for the Spring Boot application. It initializes
 * and runs the application. It also sets up Swagger 2.0 for API documentation.
 * </p>
 * <p>
 * Swagger 2.0 documentation can be accessed at:
 * <a href="http://localhost:8080/swagger-ui.html">Swagger UI</a>
 * </p>
 *
 * @see <a href="https://swagger.io/tools/swagger-ui/">Swagger UI Documentation</a>
 */
@SpringBootApplication
public class BlogApplication {

	/**
	 * The main method which is the entry point of the Spring Boot application.
	 *
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(BlogApplication.class, args);
	}
}
