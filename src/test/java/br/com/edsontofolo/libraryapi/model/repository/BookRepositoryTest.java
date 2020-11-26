package br.com.edsontofolo.libraryapi.model.repository;

import br.com.edsontofolo.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Must return true when exists a book with isbn entered.")
    public void returnTrueWhenIsbnExists() {
        // Cenário
        String isbn = "25091991";
        Book book = Book.builder().title("My incredible life").author("Edson").isbn(isbn).build();
        entityManager.persist(book);

        // Execução
        boolean exists = repository.existsByIsbn(isbn);

        // Verificação
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Must return false when does not exists a book with isbn entered.")
    public void returnTrueWhenIsbnDoesNotExists() {
        // Cenário
        String isbn = "1234537";

        // Execução
        boolean exists = repository.existsByIsbn(isbn);

        // Verificação
        assertThat(exists).isFalse();
    }
}
