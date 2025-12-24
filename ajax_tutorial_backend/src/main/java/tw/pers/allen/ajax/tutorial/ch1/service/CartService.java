package tw.pers.allen.ajax.tutorial.ch1.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.pers.allen.ajax.tutorial.ch1.model.CartItem;
import tw.pers.allen.ajax.tutorial.ch1.repo.CartItemRepository;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @PostConstruct
    public void init() {
        // Init data for testing
        if (cartItemRepository.count() == 0) {
            cartItemRepository.save(new CartItem(null, "手機", 1));
            cartItemRepository.save(new CartItem(null, "平板", 1));
            cartItemRepository.save(new CartItem(null, "筆電", 1));
            cartItemRepository.save(new CartItem(null, "藍牙耳機", 1));
            cartItemRepository.save(new CartItem(null, "耳罩式耳機", 1));
            cartItemRepository.save(new CartItem(null, "無線滑鼠", 1));
            cartItemRepository.save(new CartItem(null, "電視", 1));
            cartItemRepository.save(new CartItem(null, "電競筆電", 1));
            cartItemRepository.save(new CartItem(null, "電腦螢幕", 1));
            cartItemRepository.save(new CartItem(null, "機械鍵盤", 1));
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
