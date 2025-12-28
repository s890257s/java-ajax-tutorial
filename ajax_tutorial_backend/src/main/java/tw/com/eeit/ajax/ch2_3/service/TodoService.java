package tw.com.eeit.ajax.ch2_3.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import tw.com.eeit.ajax.ch2_3.model.Todo;

@Service
public class TodoService {

	private static List<Todo> todos = new ArrayList<>();

	static {
		todos.add(new Todo(1, 1, "購買牛奶", false));
		todos.add(new Todo(1, 2, "繳納電費", true));
		todos.add(new Todo(1, 3, "學習 Ajax", false));
		todos.add(new Todo(2, 4, "撰寫報告", false));
		todos.add(new Todo(2, 5, "整理房間", true));
	}

	public List<Todo> getAllTodos() {
		return todos;
	}

	public Todo getTodoById(Integer id) {
		return todos.stream()
				.filter(todo -> todo.getId().equals(id))
				.findFirst()
				.orElse(null);
	}
}
