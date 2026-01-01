package tw.com.eeit.ajax.ch1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer id;
    private String name;
    private List<String> skills;
    private String secret; // Just to show why DTO is needed later
}
