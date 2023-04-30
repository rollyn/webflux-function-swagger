package dev.rollyn;

import dev.rollyn.handler.ProductHandler;
import dev.rollyn.model.Product;
import dev.rollyn.repository.ProductRepository;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Product APIs", version = "1.0", description = "Documentation APIs v1.0"))
public class WebfluxFunctionalApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebfluxFunctionalApplication.class, args);
	}

	@Bean
	CommandLineRunner init(ProductRepository repository) {
		return args -> {
			Flux<Product> productFlux = Flux.just(
				new Product(null, "Colgate", 12.0),
					new Product(null, "Coke", 2.0),
					new Product(null, "Chocolate", 1.75)
			).flatMap(repository::save);
			productFlux
					.thenMany(repository.findAll())
					.subscribe(System.out::println);
		};
	}

	@Bean
	@RouterOperations(
			{
					@RouterOperation(path = "/products", produces = {
							MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET,
							operation = @Operation(operationId = "getProducts", responses = {
									@ApiResponse(responseCode = "200",
											content = @Content(array = @ArraySchema(schema = @Schema(implementation = Product.class))))}
					)),
					@RouterOperation(path = "/products/{id}", produces = {
							MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET,
							operation = @Operation(operationId = "getProduct", responses = {
							@ApiResponse(responseCode = "200", content =  @Content(schema = @Schema(implementation = Product.class))),
							@ApiResponse(responseCode = "400", description = "Invalid Product ID supplied"),
							@ApiResponse(responseCode = "404", description = "Product not found")}, parameters = {
							@Parameter(in = ParameterIn.PATH, name = "id")}
					))
			})
	RouterFunction<ServerResponse> routes(ProductHandler handler) {
		return route()
//				.GET("/products/events", accept(TEXT_EVENT_STREAM), handler::getProductEvent)
				.GET("/products/{id}", accept(APPLICATION_JSON), handler::getProduct)
				.GET("/products", accept(APPLICATION_JSON), handler::getProducts)
//				.PUT("/products/{id}", accept(APPLICATION_JSON), handler::updateProduct)
//				.POST("/products", accept(APPLICATION_JSON), handler::saveProduct)
//				.DELETE("/products/{id}", accept(APPLICATION_JSON), handler::deleteProduct)
//				.DELETE("/products", accept(APPLICATION_JSON), handler::deleteProducts)
			   .build();
	}
}
