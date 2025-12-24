package tw.pers.allen.ajax.tutorial.ch1.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tw.pers.allen.ajax.tutorial.ch1.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
