package distributed.systems.starterapp;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing Todo items.
 *
 * Provides endpoints to create, retrieve, update, and delete Todo items.
 */
@RestController
@RequestMapping("/todos")
@CrossOrigin(originPatterns = "${app.cors.allowed-origin-patterns:http://localhost:*}")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public ResponseEntity<List<TodoItem>> getTodos() {
        return ResponseEntity.ok(todoService.getAllTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoItem> getTodoById(@PathVariable Integer id) {
        return ResponseEntity.of(todoService.getTodoById(id));
    }

    @PostMapping
    public ResponseEntity<TodoItem> addTodo(@RequestBody TodoItem todo) {
        TodoItem createdTodo = todoService.createTodo(todo);
        return ResponseEntity.created(URI.create("/todos/" + createdTodo.getId())).body(createdTodo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoItem> updateTodo(@PathVariable Integer id, @RequestBody TodoItem updatedTodo) {
        return ResponseEntity.of(todoService.updateTodo(id, updatedTodo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Integer id) {
        if (todoService.deleteTodo(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
