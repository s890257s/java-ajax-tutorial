package tw.com.eeit.ajax.ch3.dto;

import lombok.Data;

@Data
public class UserResponse {
    private Integer id;
    private String username;
    private String email;
    // 注意：這裡沒有 password 和 phoneNumber
}
