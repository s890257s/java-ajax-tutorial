package tw.pers.allen.ajax_tutorial_backend.ch1.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tw.pers.allen.ajax_tutorial_backend.ch1.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
