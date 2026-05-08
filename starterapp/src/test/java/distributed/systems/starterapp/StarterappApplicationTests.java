package distributed.systems.starterapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StarterappApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TodoRepository todoRepository;

	@BeforeEach
	void clearTodos() {
		todoRepository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void todosEndpointReturnsJson() throws Exception {
		mockMvc.perform(get("/todos"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
	}

	@Test
	void createTodoReturnsCreatedResource() throws Exception {
		mockMvc.perform(post("/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"id": 123,
									"title": "Write tests",
									"description": "Cover REST behavior",
									"completed": false
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/todos/")))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.title").value("Write tests"));
	}

	@Test
	void updateTodoUsesPathIdAndReturnsNotFoundForMissingTodo() throws Exception {
		TodoItem todo = todoRepository.save(new TodoItem(null, "Old title", "Old description", false));

		mockMvc.perform(put("/todos/{id}", todo.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"id": 999,
									"title": "New title",
									"description": "New description",
									"completed": true
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(todo.getId()))
				.andExpect(jsonPath("$.title").value("New title"))
				.andExpect(jsonPath("$.completed").value(true));

		mockMvc.perform(put("/todos/{id}", 999)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"title": "Missing",
									"description": "Does not exist",
									"completed": false
								}
								"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteTodoReturnsNoContentOrNotFound() throws Exception {
		TodoItem todo = todoRepository.save(new TodoItem(null, "Delete me", "Delete description", false));

		mockMvc.perform(delete("/todos/{id}", todo.getId()))
				.andExpect(status().isNoContent());

		mockMvc.perform(delete("/todos/{id}", todo.getId()))
				.andExpect(status().isNotFound());
	}
}
