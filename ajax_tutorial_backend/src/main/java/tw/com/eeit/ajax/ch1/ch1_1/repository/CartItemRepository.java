package tw.com.eeit.ajax.ch1.ch1_1.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tw.com.eeit.ajax.ch1.ch1_1.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
