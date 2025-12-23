package tw.pers.allen.ajax_tutorial_backend.ch1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tw.pers.allen.ajax_tutorial_backend.ch1.model.CartItem;
import tw.pers.allen.ajax_tutorial_backend.ch1.service.CartService;

import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart/page")
    public String viewCart(Model model) {
        List<CartItem> items = cartService.getCurrentCart();
        model.addAttribute("cartItems", items);
        return "cart/page";
    }

    @PostMapping("/cart/update")
    public String updateQuantity(@RequestParam Long productId, @RequestParam Integer newQty, Model model) {
        // 1. 處理業務邏輯 (只改了一行資料)
        cartService.updateQuantity(productId, newQty);

        // 2. 為了回傳畫面，必須重新撈取「所有」購物車資料
        List<CartItem> items = cartService.getCurrentCart();
        model.addAttribute("cartItems", items);

        // 3. 回傳整個 View 名稱，觸發整頁渲染
        return "cart/page";
    }
}
