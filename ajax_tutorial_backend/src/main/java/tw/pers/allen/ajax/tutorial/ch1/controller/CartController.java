package tw.pers.allen.ajax.tutorial.ch1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import tw.pers.allen.ajax.tutorial.ch1.model.CartItem;
import tw.pers.allen.ajax.tutorial.ch1.service.CartService;

import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart/page")
    public String viewCart(Model model) {
        List<CartItem> items = cartService.getCurrentCart();
        model.addAttribute("cartItems", items);
        return "ch1_1/cart_comparison.html";
    }

    // Deprecated separate pages
    // @GetMapping("/cart/mvc-page")...
    // @GetMapping("/cart/ajax-page")...

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "test";
    }

    // Existing MVC Update (PRG)
    @PostMapping("/cart/update")
    public String updateQuantity(@RequestParam Long productId, @RequestParam Integer newQty, Model model) {
        cartService.updateQuantity(productId, newQty);
        return "redirect:/cart/page";
    }

    // New API for AJAX Update
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
