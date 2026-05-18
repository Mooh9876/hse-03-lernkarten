package distributed.systems.starterapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class StarterappApplication {
	/*  Backend of a flashcard application for exam preparation.
		The new REST API provides CRUD endpoints for learning cards at /flashcards.
		Data is persisted with Spring Data JPA so the application can be scaled more easily.
	*/
	public static void main(String[] args) {
		SpringApplication.run(StarterappApplication.class, args);
	}

	@GetMapping("/hello")
	public String hello() {
		return "Hello, HSE26!";
	}


}
