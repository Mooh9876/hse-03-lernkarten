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
	private FlashcardRepository flashcardRepository;

	@BeforeEach
	void clearFlashcards() {
		flashcardRepository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void flashcardsEndpointReturnsJson() throws Exception {
		mockMvc.perform(get("/flashcards"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
	}

	@Test
	void createFlashcardReturnsCreatedResource() throws Exception {
		mockMvc.perform(post("/flashcards")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"id": 123,
									"question": "What is CAP theorem?",
									"answer": "Consistency, Availability, Partition tolerance",
									"category": "Distributed Systems",
									"learned": false
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/flashcards/")))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.question").value("What is CAP theorem?"));
	}

	@Test
	void updateFlashcardUsesPathIdAndReturnsNotFoundForMissingFlashcard() throws Exception {
		Flashcard flashcard = flashcardRepository.save(new Flashcard(null, "Old question", "Old answer", "General", false));

		mockMvc.perform(put("/flashcards/{id}", flashcard.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"id": 999,
									"question": "New question",
									"answer": "New answer",
									"category": "Cloud",
									"learned": true
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(flashcard.getId()))
				.andExpect(jsonPath("$.question").value("New question"))
				.andExpect(jsonPath("$.learned").value(true));

		mockMvc.perform(put("/flashcards/{id}", 999)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"question": "Missing",
									"answer": "Does not exist",
									"category": "General",
									"learned": false
								}
								"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteFlashcardReturnsNoContentOrNotFound() throws Exception {
		Flashcard flashcard = flashcardRepository.save(new Flashcard(null, "Delete me", "Delete answer", "General", false));

		mockMvc.perform(delete("/flashcards/{id}", flashcard.getId()))
				.andExpect(status().isNoContent());

		mockMvc.perform(delete("/flashcards/{id}", flashcard.getId()))
				.andExpect(status().isNotFound());
	}
}
