package ma.ensaj.productservice.repositories;

import ma.ensaj.productservice.entities.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
