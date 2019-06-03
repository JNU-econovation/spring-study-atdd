package codesquad.domain;

import codesquad.CannotDeleteException;
import codesquad.UnAuthorizedException;
import codesquad.dto.QuestionDTO;
import org.hibernate.annotations.Where;
import support.domain.AbstractEntity;
import support.domain.UrlGeneratable;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Question extends AbstractEntity implements UrlGeneratable {
    @Size(min = 3, max = 100)
    @Column(length = 100, nullable = false)
    private String title;

    @Size(min = 3)
    @Lob
    private String contents;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_question_writer"))
    private User writer;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @Where(clause = "deleted = false")
    @OrderBy("id ASC")
    private List<Answer> answers = new ArrayList<>();

    private boolean deleted = false;

    public Question() {
    }

    public Question(String title, String contents) {
        this.title = title;
        this.contents = contents;
    }

    public Question(User writer, QuestionDTO questionDTO) {
        this.writer = writer;
        this.title = questionDTO.getTitle();
        this.contents = questionDTO.getContents();
    }

    public String getTitle() {
        return title;
    }

    public Question setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getContents() {
        return contents;
    }

    public Question setContents(String contents) {
        this.contents = contents;
        return this;
    }

    public User getWriter() {
        return writer;
    }

    public void writeBy(User loginUser) {
        this.writer = loginUser;
    }

    public void addAnswer(Answer answer) {
        answer.toQuestion(this);
        answers.add(answer);
    }

    public boolean isOwner(User loginUser) {
        return writer.equals(loginUser);
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public String generateUrl() {
        return String.format("/questions/%d", getId());
    }

    @Override
    public String toString() {
        return "Question [id=" + getId() + ", title=" + title + ", contents=" + contents + ", writer=" + writer + "]";
    }

    public void update(User loginUser, QuestionDTO updatedQuestionDTO) {
        if (!isOwner(loginUser)) {
            throw new UnAuthorizedException();
        }

        this.title = updatedQuestionDTO.getTitle();
        this.contents = updatedQuestionDTO.getContents();
    }

    public List<DeleteHistory> delete(User loginUser) throws CannotDeleteException {
        if (!isOwner(loginUser)) {
            throw new UnAuthorizedException();
        }

        if (!hasNotOthersAnswer()) {
            throw new CannotDeleteException("exist others' answer");
        }

        this.deleted = true;
        List<DeleteHistory> histories = new ArrayList<>();
        histories.add(new DeleteHistory(ContentType.QUESTION, getId(), loginUser, LocalDateTime.now()));

        answers.stream().map(answer -> answer.delete(loginUser));
        answers.stream().forEach(answer ->
                histories.add(new DeleteHistory(ContentType.ANSWER, answer.getId(), loginUser, LocalDateTime.now())));

        return histories;
    }

    private boolean hasNotOthersAnswer() {
        return answers.stream().allMatch(answer -> answer.getWriter().equals(writer));
    }

    public boolean isEqualsTitleAndContents(Question target) {
        if (Objects.isNull(target)) {
            return false;
        }

        return this.title.equals(target.title) &&
                this.contents.equals(target.contents);
    }
}
