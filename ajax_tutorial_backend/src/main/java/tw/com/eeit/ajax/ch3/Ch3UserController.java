package tw.com.eeit.ajax.ch3;

import org.springframework.web.bind.annotation.*;
import tw.com.eeit.ajax.ch1.model.User;
import tw.com.eeit.ajax.ch3.dto.UserCreateRequest;
import tw.com.eeit.ajax.ch3.dto.UserResponse;
import tw.com.eeit.ajax.ch3.mapper.UserMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ch3/users")
public class Ch3UserController {

    private final List<User> userDb = new ArrayList<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    private final UserMapper userMapper;

    public Ch3UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
        // Init data
        userDb.add(new User(idGenerator.getAndIncrement(), "Alice", List.of("Java"), "Secret123"));
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userDb.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public UserResponse create(@RequestBody UserCreateRequest request) {
        // DTO -> Entity
        User newUser = userMapper.toEntity(request);
        newUser.setId(idGenerator.getAndIncrement());
        
        userDb.add(newUser);

        // Entity -> DTO
        return userMapper.toDto(newUser);
    }
}
