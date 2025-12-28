package tw.com.eeit.ajax.ch1_2.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private Integer id;
    private String name;
    private List<String> skills;
    private List<UserExperience> experience;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserExperience {
        private String company;
        private String role;
    }
}
