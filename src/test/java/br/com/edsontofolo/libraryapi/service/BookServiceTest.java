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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Test
    @DisplayName("Get book by id")
    public void getByIdTest() {
        Long id = 1L;
        Book book = createValidBook();
        book.setId(id);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> foundBook = service.getById(id);

        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Get empty return when searched book by id does not exists")
    public void getBookNotFoundByIdTest() {
        Long id = 1L;
        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Book> book = service.getById(id);

        assertThat(book.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Must delete book")
    public void deleteTest() {
        Book book = Book.builder().id(1L).build();

        org.junit.jupiter.api.
                Assertions.assertDoesNotThrow(() ->
                    service.delete(book)
        );

        Mockito.verify(repository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Must throws an exception when delete book invalid")
    public void deleteInvalidBookTest() {
        Book book = new Book();

        org.junit.jupiter.api.
                Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        Mockito.verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Must update book")
    public void updateBookTest() {
        long id = 1L;

        Book updatingBook = Book.builder().id(id).build();

        Book updatedBook = createValidBook();
        updatedBook.setId(id);

        Mockito.when(repository.save(updatingBook)).thenReturn(updatedBook);

        Book book = service.update(updatingBook);

        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
    }

    @Test
    @DisplayName("Must throws an exception when update book invalid")
    public void updateInvalidBookTest() {
        Book book = new Book();

        org.junit.jupiter.api.
                Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Must filter books by properties")
    public void findBookTest() {
        // cenario
        Book book = createValidBook();

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> list = Arrays.asList(book);
        Page<Book> page = new PageImpl<>(list, pageRequest, 1);

        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(page);

        // execucao
        Page<Book> result = service.find(book, pageRequest);

        // verificacao
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }
}
