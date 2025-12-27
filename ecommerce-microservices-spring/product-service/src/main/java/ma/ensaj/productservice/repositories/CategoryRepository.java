package ma.ensaj.productservice.repositories;

import ma.ensaj.productservice.entities.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category,String> {
}
