package tw.com.eeit.ajax.ch2_3.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Todo {
    private Integer userId;
    private Integer id;
    private String title;
    private Boolean completed;
}
