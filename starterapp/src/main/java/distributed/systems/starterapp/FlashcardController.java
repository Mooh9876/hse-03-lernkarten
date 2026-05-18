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

@RestController
@RequestMapping("/flashcards")
@CrossOrigin(originPatterns = "${app.cors.allowed-origin-patterns:http://localhost:*}")
public class FlashcardController {

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @GetMapping
    public ResponseEntity<List<Flashcard>> getFlashcards() {
        return ResponseEntity.ok(flashcardService.getAllFlashcards());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Flashcard> getFlashcardById(@PathVariable Integer id) {
        return ResponseEntity.of(flashcardService.getFlashcardById(id));
    }

    @PostMapping
    public ResponseEntity<Flashcard> addFlashcard(@RequestBody Flashcard flashcard) {
        Flashcard createdFlashcard = flashcardService.createFlashcard(flashcard);
        return ResponseEntity.created(URI.create("/flashcards/" + createdFlashcard.getId())).body(createdFlashcard);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Flashcard> updateFlashcard(@PathVariable Integer id, @RequestBody Flashcard updatedFlashcard) {
        return ResponseEntity.of(flashcardService.updateFlashcard(id, updatedFlashcard));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlashcard(@PathVariable Integer id) {
        if (flashcardService.deleteFlashcard(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
