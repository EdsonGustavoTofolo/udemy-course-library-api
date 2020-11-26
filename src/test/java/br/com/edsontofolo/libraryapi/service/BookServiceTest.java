package br.com.edsontofolo.libraryapi.service;

import br.com.edsontofolo.libraryapi.exception.BusinessException;
import br.com.edsontofolo.libraryapi.model.entity.Book;
import br.com.edsontofolo.libraryapi.model.repository.BookRepository;
import br.com.edsontofolo.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Must save one book")
    public void saveBookTest() {
        Book book = createValidBook();

        Book bookReturn = new Book();
        bookReturn.setId(1L);
        bookReturn.setTitle("Meu Titulo");
        bookReturn.setAuthor("O Autor");
        bookReturn.setIsbn("651651");

        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when(repository.save(book)).thenReturn(bookReturn);

        Book savedBook = service.save(book);

        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("651651");
        assertThat(savedBook.getTitle()).isEqualTo("Meu Titulo");
        assertThat(savedBook.getAuthor()).isEqualTo("O Autor");
    }

        private Book createValidBook() {
        Book book = new Book();
        book.setTitle("Meu Titulo");
        book.setAuthor("O Autor");
        book.setIsbn("651651");
        return book;
    }

    @Test
    @DisplayName("Must throws a business error on save a duplicated isbn")
    public void mustNotSaveAbookWithDuplicatedIsb() {
        Book book = createValidBook();

        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        Throwable ex = Assertions.catchThrowable(() -> service.save(book));

        assertThat(ex)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");

        // verifica que nunca deve chamar o salvar
        Mockito.verify(repository, Mockito.never()).save(book);
    }
}
