package tw.pers.allen.ajax_tutorial_backend.ch1.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.pers.allen.ajax_tutorial_backend.ch1.model.CartItem;
import tw.pers.allen.ajax_tutorial_backend.ch1.repo.CartItemRepository;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @PostConstruct
    public void init() {
        // Init data for testing
        if (cartItemRepository.count() == 0) {
            cartItemRepository.save(new CartItem(null, "Apple", 2));
            cartItemRepository.save(new CartItem(null, "Banana", 5));
            cartItemRepository.save(new CartItem(null, "Orange", 3));
        }
    }

    public List<CartItem> getCurrentCart() {
        return cartItemRepository.findAll();
    }

    @Transactional
    public void updateQuantity(Long productId, Integer newQty) {
        cartItemRepository.findById(productId).ifPresent(item -> {
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        });
    }
}
