package ma.ensaj.cartservice.repositories;

import ma.ensaj.cartservice.dtos.CartResponse;
import ma.ensaj.cartservice.entities.Cart;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CartRepository extends CrudRepository<Cart, String> {

    Optional<Cart> findByUserId(Long userId);
    void deleteByUserId(Long userId);


}
