package distributed.systems.starterapp;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TodoService {
    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<TodoItem> getAllTodos() {
        return todoRepository.findAll();
    }

    public Optional<TodoItem> getTodoById(Integer id) {
        return todoRepository.findById(id);
    }

    @Transactional
    public TodoItem createTodo(TodoItem todo) {
        todo.setId(null);
        return todoRepository.save(todo);
    }

    @Transactional
    public Optional<TodoItem> updateTodo(Integer id, TodoItem updatedTodo) {
        return todoRepository.findById(id).map(todo -> {
            todo.setTitle(updatedTodo.getTitle());
            todo.setDescription(updatedTodo.getDescription());
            todo.setCompleted(updatedTodo.isCompleted());
            return todo;
        });
    }

    @Transactional
    public boolean deleteTodo(Integer id) {
        if (!todoRepository.existsById(id)) {
            return false;
        }
        todoRepository.deleteById(id);
        return true;
    }
}
