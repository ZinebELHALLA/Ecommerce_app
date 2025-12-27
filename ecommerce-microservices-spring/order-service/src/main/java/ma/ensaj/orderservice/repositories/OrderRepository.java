package ma.ensaj.orderservice.repositories;

import ma.ensaj.orderservice.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
