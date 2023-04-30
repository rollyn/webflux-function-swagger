package dev.rollyn.handler;

import dev.rollyn.model.Product;
import dev.rollyn.model.ProductEvent;
import dev.rollyn.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class ProductHandler {

    private final ProductRepository repository;

    public ProductHandler(ProductRepository repository) {
        this.repository = repository;
    }

    public Mono<ServerResponse> getProducts(ServerRequest request) {
        Flux<Product> products = repository.findAll();

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(products, Product.class);
    }

    public Mono<ServerResponse> getProduct(ServerRequest request) {
        String id = request.pathVariable("id");

        Mono<Product> product = repository.findById(id);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return product.flatMap(p -> {
            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(fromValue(p));
        }).switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> saveProduct(ServerRequest request) {
        Mono<Product> product = request.bodyToMono(Product.class);

        return product.flatMap(p ->
            ServerResponse.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(repository.save(p), Product.class));
    }

    public Mono<ServerResponse> updateProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Product> existingProduct = repository.findById(id);
        Mono<Product> product = request.bodyToMono(Product.class);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return product.zipWith(existingProduct,
                (p, existing) ->
                  new Product(existing.getId(), p.getName(), p.getPrice()))
                .flatMap(prod -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(repository.save(prod), Product.class))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Product> existingProduct = repository.findById(id);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return existingProduct
                .flatMap((p -> ServerResponse.ok()
                        .build(repository.delete(p))))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> deleteProducts(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Product> existingProduct = repository.findById(id);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return ServerResponse.ok()
                .build(repository.deleteAll());
    }

    public Mono<ServerResponse> getProductEvent(ServerRequest request) {
       Flux<ProductEvent> eventFlux = Flux.interval(Duration.ofSeconds(1))
               .map(e -> new ProductEvent(e, "Product Event"));

       return ServerResponse.ok()
               .contentType(MediaType.TEXT_EVENT_STREAM)
               .body(eventFlux, ProductEvent.class);
    }
}
