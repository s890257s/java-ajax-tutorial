package tw.com.eeit.ajax.ch1.ch1_1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import tw.com.eeit.ajax.ch1.ch1_1.model.CartItem;
import tw.com.eeit.ajax.ch1.ch1_1.service.CartService;

import java.util.List;

@Controller
@RequestMapping("/ch1_1")
public class Ch1_1_CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart/page")
    public String viewCart(Model model) {
        List<CartItem> items = cartService.getCurrentCart();
        model.addAttribute("cartItems", items);
        return "ch1_1/cart_comparison.html";
    }

    @PostMapping("/cart/update")
    public String updateQuantity(@RequestParam Long productId, @RequestParam Integer newQty, Model model) {
        cartService.updateQuantity(productId, newQty);
        return "redirect:/ch1_1/cart/page";
    }

    @PostMapping("/cart/api/update")
    @ResponseBody
    public CartItem updateQuantityApi(@RequestParam Long productId, @RequestParam Integer newQty) {
        cartService.updateQuantity(productId, newQty);
        return cartService.getCurrentCart().stream()
                .filter(item -> item.getId().equals(productId))
                .findFirst()
                .orElseThrow();
    }
}
