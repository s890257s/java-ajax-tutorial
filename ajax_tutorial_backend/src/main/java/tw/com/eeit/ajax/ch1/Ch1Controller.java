package tw.com.eeit.ajax.ch1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.com.eeit.ajax.ch1.model.User;

import java.util.List;

@RestController
@RequestMapping("/ch1")
public class Ch1Controller {

    @GetMapping("/test")
    public String test() {
        return "Hello Ajax from Spring Boot!";
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return List.of(
            new User(1, "Alice", List.of("Java", "Spring Boot"), "I love coding"),
            new User(2, "Bob", List.of("HTML", "CSS"), "I hate bugs")
        );
    }
}
