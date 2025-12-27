package ma.ensaj.orderservice.repositories;

import ma.ensaj.orderservice.entities.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLineRepository  extends JpaRepository<OrderLine, Long> {
}
