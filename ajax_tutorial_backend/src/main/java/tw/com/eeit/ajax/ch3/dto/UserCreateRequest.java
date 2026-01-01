package tw.com.eeit.ajax.ch3.dto;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String username;
    private String email;
    private String password;
    private String phoneNumber; // 假設這是不想回傳給前端的敏感資料
}
