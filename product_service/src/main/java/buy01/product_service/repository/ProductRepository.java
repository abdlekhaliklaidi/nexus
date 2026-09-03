package buy01.product_service.repository;

import buy01.product_service.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByUserId(String userId);

    void deleteByUserId(String userId);

}