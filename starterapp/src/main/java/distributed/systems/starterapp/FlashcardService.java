package distributed.systems.starterapp;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FlashcardService {
    private final FlashcardRepository flashcardRepository;

    public FlashcardService(FlashcardRepository flashcardRepository) {
        this.flashcardRepository = flashcardRepository;
    }

    public List<Flashcard> getAllFlashcards() {
        return flashcardRepository.findAll();
    }

    public Optional<Flashcard> getFlashcardById(Integer id) {
        return flashcardRepository.findById(id);
    }

    @Transactional
    public Flashcard createFlashcard(Flashcard flashcard) {
        flashcard.setId(null);
        return flashcardRepository.save(flashcard);
    }

    @Transactional
    public Optional<Flashcard> updateFlashcard(Integer id, Flashcard updatedFlashcard) {
        return flashcardRepository.findById(id).map(flashcard -> {
            flashcard.setQuestion(updatedFlashcard.getQuestion());
            flashcard.setAnswer(updatedFlashcard.getAnswer());
            flashcard.setCategory(updatedFlashcard.getCategory());
            flashcard.setLearned(updatedFlashcard.isLearned());
            return flashcard;
        });
    }

    @Transactional
    public boolean deleteFlashcard(Integer id) {
        if (!flashcardRepository.existsById(id)) {
            return false;
        }
        flashcardRepository.deleteById(id);
        return true;
    }
}
