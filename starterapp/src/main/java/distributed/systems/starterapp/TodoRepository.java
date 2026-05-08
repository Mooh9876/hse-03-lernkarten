package distributed.systems.starterapp;

import org.springframework.data.jpa.repository.JpaRepository;


public interface TodoRepository extends JpaRepository<TodoItem, Integer> {

}
