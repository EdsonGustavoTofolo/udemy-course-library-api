package br.com.edsontofolo.libraryapi.api.resource;

import br.com.edsontofolo.libraryapi.api.dto.BookDTO;
import br.com.edsontofolo.libraryapi.api.dto.LoanDTO;
import br.com.edsontofolo.libraryapi.model.entity.Book;
import br.com.edsontofolo.libraryapi.model.entity.Loan;
import br.com.edsontofolo.libraryapi.service.BookService;
import br.com.edsontofolo.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor // precisa colocar o final pra injetar os fields
@Api("Book API")
@Slf4j // utilizado para fazer logs
public class BookController {

    private final LoanService loanService;
    private final BookService service;
    private final ModelMapper modelMapper; // também existe uma library chamada MapStruct

    /**
     * @Valid é do spring-mvc e valida as anotações @NotEmpty dos fields da classe BookDTO,
     * evitando assim que a requisição seja efetuada
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Create a book")
    public BookDTO create( @RequestBody @Valid BookDTO dto ) {
        log.info("Create a book for isbn: {}", dto.getIsbn());

        Book book = modelMapper.map(dto, Book.class);
        book = service.save(book);
        return modelMapper.map(book, BookDTO.class);
    }

    @GetMapping("{id}")
    @ApiOperation("Get a book by id")
    public BookDTO get(@PathVariable Long id) {
        log.info("Geting a book with id: {}", id);
        return service
                .getById(id)
                .map((book) -> modelMapper.map(book, BookDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Delete a book by id")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Book succesfuly deleted")
    })
    public void delete(@PathVariable Long id) {
        log.info("Deleting book with id: {}", id);

        Book book = service.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        service.delete(book);
    }

    @PutMapping("{id}")
    @ApiOperation("Update a book by id")
    public BookDTO update(@PathVariable Long id, @RequestBody @Valid BookDTO dto) {
        log.info("Updating book with id: {}", id);
        return service.getById(id)
                .map((book -> {
                    book.setAuthor(dto.getAuthor());
                    book.setTitle(dto.getTitle());
                    book = service.update(book);
                    return modelMapper.map(book, BookDTO.class);
                }))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    @ApiOperation("Find a book by props")
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {
        Book filter = modelMapper.map(dto, Book.class);
        Page<Book> result = service.find(filter, pageRequest);
        List<BookDTO> list = result
                .getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());
        return new PageImpl<>(list, pageRequest, result.getTotalElements());
    }

    @GetMapping("{1}/loans")
    @ApiOperation("Find a loans book")
    public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageable) {
        Book foundBook = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Page<Loan> result = loanService.getLoansByBook(foundBook, pageable);

        List<LoanDTO> list = result.getContent().stream().map(loan -> {
            Book book = loan.getBook();
            BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
            LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
            loanDTO.setBook(bookDTO);
            return loanDTO;
        }).collect(Collectors.toList());

        return new PageImpl<LoanDTO>(list, pageable, result.getTotalElements());
    }

}
