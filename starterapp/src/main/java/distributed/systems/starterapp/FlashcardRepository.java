package distributed.systems.starterapp;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardRepository extends JpaRepository<Flashcard, Integer> {
}
