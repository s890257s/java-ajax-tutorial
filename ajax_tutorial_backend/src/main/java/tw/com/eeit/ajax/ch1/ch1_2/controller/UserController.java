package tw.com.eeit.ajax.ch1.ch1_2.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ch1_2")
public class UserController {

    @GetMapping("/test")
    public String test() {
        return "Hello Ajax";
    }
    
    // 回傳所有使用者

}
