package ma.ensaj.productservice.repositories;

import ma.ensaj.productservice.entities.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findBySku(String sku);
    List<Product> findBySkuIn(List<String> skus);
}
