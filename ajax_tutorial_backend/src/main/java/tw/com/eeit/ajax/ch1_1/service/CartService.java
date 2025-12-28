package tw.com.eeit.ajax.ch1_1.service;

import jakarta.annotation.PostConstruct;
import tw.com.eeit.ajax.ch1_1.model.entity.CartItem;
import tw.com.eeit.ajax.ch1_1.repository.CartItemRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @PostConstruct
    public void init() {
        if (cartItemRepository.count() == 0) {
            List<CartItem> items = List.of(
                    new CartItem(null, "手機", 1),
                    new CartItem(null, "平板", 1),
                    new CartItem(null, "筆電", 1),
                    new CartItem(null, "藍牙耳機", 1),
                    new CartItem(null, "耳罩式耳機", 1),
                    new CartItem(null, "無線滑鼠", 1),
                    new CartItem(null, "電視", 1),
                    new CartItem(null, "電競筆電", 1),
                    new CartItem(null, "電腦螢幕", 1),
                    new CartItem(null, "機械鍵盤", 1));

            cartItemRepository.saveAll(items);
        }
    }

    public List<CartItem> getCurrentCart() {
        return cartItemRepository.findAll();
    }

    @Transactional
    public void updateQuantity(Long productId, Integer newQty) {

        if (productId == null) {
            throw new IllegalArgumentException("Product ID 不可為空");
        }

        cartItemRepository.findById(productId).ifPresent(item -> {
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        });
    }
}
