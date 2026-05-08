package distributed.systems.starterapp;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/todos")
@Profile("pox")
public class TodoControllerPOX {
    private final List<TodoItem> todosPOX = new ArrayList<>(
            List.of(
                    new TodoItem(1, "Todo 1", "Description 1", false),
                    new TodoItem(2, "Todo 2", "Description 2", true)
            )
    );

    @GetMapping("/getTodos")
    public  String getTodos() {
        return todosPOX.toString();
    }

    @RequestMapping(value = "/getTodoById", method = RequestMethod.GET)
    public ResponseEntity<TodoItem> getTodoById(@RequestParam Integer id) {
        for (TodoItem todo : todosPOX) {
            if (todo.getId().equals(id)) {
                return new ResponseEntity<>(todo, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/addTodo", method = RequestMethod.POST)
    public ResponseEntity<String> addTodo(@RequestBody TodoItem todo) {
        todosPOX.add(todo);
        return new ResponseEntity<>("Todo item added successfully", HttpStatus.CREATED);
    }
}
