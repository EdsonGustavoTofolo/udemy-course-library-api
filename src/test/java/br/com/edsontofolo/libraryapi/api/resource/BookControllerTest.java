package br.com.edsontofolo.libraryapi.api.resource;

import br.com.edsontofolo.libraryapi.api.dto.BookDTO;
import br.com.edsontofolo.libraryapi.exception.BusinessException;
import br.com.edsontofolo.libraryapi.model.entity.Book;
import br.com.edsontofolo.libraryapi.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = {BookController.class}) // controller define quais controllers serão criados para o test, no caso somente o BookController.class
@AutoConfigureMockMvc
public class BookControllerTest {

    public static final String ISBN_JA_CADASTRADO = "Isbn já cadastrado.";
    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc; // simula a requisição
    @MockBean
    BookService service;

    @Test
    @DisplayName("Must create one valid book")
    public void createBookTest() throws Exception {
        BookDTO dto = new BookDTO(0L, "As Cronicas de Arthur", "OCara", "123456");

        Book savedBook = new Book(1L, "As Cronicas de Arthur", "OCara", "123456");

        BDDMockito
                .given(service.save(Mockito.any(Book.class)))
                .willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(1))
                .andExpect(jsonPath("title").value(dto.getTitle()))
                .andExpect(jsonPath("author").value(dto.getAuthor()))
                .andExpect(jsonPath("isbn").value(dto.getIsbn()));
    }

    @Test
    @DisplayName("Must create one invalid book")
    public void createInvalidBookTest() throws Exception {
        BookDTO bookDTO = new BookDTO();

        String json = new ObjectMapper().writeValueAsString(bookDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("Must throws an error when create book with isbn already in use")
    public void createBookWithDuplicateIsbn() throws Exception {
        BookDTO dto = createNewBookDTO();

        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito
                .given(service.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(ISBN_JA_CADASTRADO));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(ISBN_JA_CADASTRADO));

    }

    @Test
    @DisplayName("Get book informations")
    public void getBookDetailsTest() throws Exception {
        //Cenario (given)
        Long id = 1L;
        BookDTO dto = createNewBookDTO();
        Book book = Book.builder().id(id).title(dto.getTitle()).author(dto.getAuthor()).isbn(dto.getIsbn()).build();
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        // execucao (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/").concat(Long.toString(id)))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(dto.getTitle()))
                .andExpect(jsonPath("author").value(dto.getAuthor()))
                .andExpect(jsonPath("isbn").value(dto.getIsbn()));
    }

    @Test
    @DisplayName("Must return NOT FOUND when searched book is not found")
    public void bookNotFoundTest() throws Exception {
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // execucao (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/").concat(Long.toString(1)))
                .accept(MediaType.APPLICATION_JSON);

        // verificacao
        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Must delete book")
    public void deleteBookTest() throws Exception {
        //cenario
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(1L).build()));

        // execucao (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/").concat(Long.toString(1)))
                .accept(MediaType.APPLICATION_JSON);

        // verificacao
        mvc.perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Must return NOT FOUND when searched book to delete is not found")
    public void deleteNotFoundBookTest() throws Exception {
        //cenario
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // execucao (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/").concat(Long.toString(1)))
                .accept(MediaType.APPLICATION_JSON);

        // verificacao
        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update a book")
    public void updateBookTest() throws Exception {
        //cenario
        Long id = 1L;
        BookDTO dto = createNewBookDTO();
        String json = new ObjectMapper().writeValueAsString(dto);
        Book book = Book.builder().id(id).title("Some title").author("Some author").isbn("99999").build();
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));
        Book updatedBook = Book.builder().id(id).title(dto.getTitle()).author(dto.getAuthor()).isbn(book.getIsbn()).build();
        BDDMockito.given(service.update(book)).willReturn(updatedBook);

        // execucao (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/").concat(Long.toString(id)))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        // verificacao
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(dto.getTitle()))
                .andExpect(jsonPath("author").value(dto.getAuthor()))
                .andExpect(jsonPath("isbn").value(book.getIsbn()));
    }

    @Test
    @DisplayName("Must return NOT FOUND when searched book to update is not found")
    public void updateNotFoundBookTest() throws Exception {
        //cenario
        Long id = 1L;
        BookDTO dto = createNewBookDTO();
        String json = new ObjectMapper().writeValueAsString(dto);
        BDDMockito.given(service.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        // execucao (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/").concat(Long.toString(id)))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        // verificacao
        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Must filter books")
    public void findBooksTest() throws Exception {
        Long id = 1L;
        BookDTO dto = createNewBookDTO();
        Book book = Book.builder()
                .id(id)
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .isbn(dto.getIsbn())
                .build();

        BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));

        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                book.getTitle(), book.getAuthor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));

    }

    private BookDTO createNewBookDTO() {
        return BookDTO.builder().author("Edson").title("My incredible life").isbn("25091991").build();
    }
}
