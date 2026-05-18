package distributed.systems.starterapp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Flashcard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String question;
    private String answer;
    private String category;
    private boolean learned;

    public Flashcard() {
    }

    public Flashcard(Integer id, String question, String answer, String category, boolean learned) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.learned = learned;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isLearned() {
        return learned;
    }

    public void setLearned(boolean learned) {
        this.learned = learned;
    }
}
