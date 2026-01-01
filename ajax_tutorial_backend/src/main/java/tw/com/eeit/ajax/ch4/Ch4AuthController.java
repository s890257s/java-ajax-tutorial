package tw.com.eeit.ajax.ch4;

import lombok.Data;
import org.springframework.web.bind.annotation.*;
import tw.com.eeit.ajax.ch4.utils.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/ch4")
public class Ch4AuthController {

    private final JwtUtil jwtUtil;

    public Ch4AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        // 模擬驗證：只要帳號是 admin 密碼是 1234 就通過
        if ("admin".equals(request.getUsername()) && "1234".equals(request.getPassword())) {
            String token = jwtUtil.generateToken(request.getUsername());
            return Map.of("token", token);
        } else {
            throw new RuntimeException("Login Failed");
        }
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
