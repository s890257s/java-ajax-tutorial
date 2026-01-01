package tw.com.eeit.ajax.ch2;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/ch2/todos")
public class Ch2TodoController {

    private final List<Todo> todoList = new ArrayList<>();
    // 模擬 DB 的 Auto Increment ID
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    public Ch2TodoController() {
        // 初始化一些假資料
        todoList.add(new Todo(idGenerator.getAndIncrement(), "Learn JavaScript", true));
        todoList.add(new Todo(idGenerator.getAndIncrement(), "Learn Spring Boot", true));
        todoList.add(new Todo(idGenerator.getAndIncrement(), "Learn Ajax", false));
    }

    // 1. Get All
    @GetMapping
    public List<Todo> getAll() {
        return todoList;
    }

    // 2. Get One
    @GetMapping("/{id}")
    public Todo getOne(@PathVariable Integer id) {
        return todoList.stream()
                .filter(todo -> todo.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Todo not found"));
    }

    // 3. Create (Post)
    @PostMapping
    public Todo create(@RequestBody Todo todo) {
        todo.setId(idGenerator.getAndIncrement());
        todoList.add(todo);
        return todo;
    }

    // 4. Update (Put)
    @PutMapping("/{id}")
    public Todo update(@PathVariable Integer id, @RequestBody Todo todo) {
        Todo existing = getOne(id);
        existing.setTitle(todo.getTitle());
        existing.setCompleted(todo.getCompleted());
        return existing;
    }

    // 5. Delete
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        todoList.removeIf(t -> t.getId().equals(id));
    }
}
