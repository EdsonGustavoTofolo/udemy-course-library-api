package br.com.edsontofolo.libraryapi.api.resource;

import br.com.edsontofolo.libraryapi.api.dto.BookDTO;
import br.com.edsontofolo.libraryapi.api.exception.ApiErrors;
import br.com.edsontofolo.libraryapi.exception.BusinessException;
import br.com.edsontofolo.libraryapi.model.entity.Book;
import br.com.edsontofolo.libraryapi.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private BookService service;
    private ModelMapper modelMapper; // também existe uma library chamada MapStruct

    public BookController(BookService service, ModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }
    /**
     * @Valid é do spring-mvc e valida as anotações @NotEmpty dos fields da classe BookDTO,
     * evitando assim que a requisição seja efetuada
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create( @RequestBody @Valid BookDTO dto ) {
        System.out.println(dto.toString());
        Book book = modelMapper.map(dto, Book.class);
        book = service.save(book);
        return modelMapper.map(book, BookDTO.class);
    }

    /**
     *
     * @param ex - quando parâmetro de alguma requisição estiver anotado por @Valid e não for válido cai nessa exceção
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleValidationExceptions(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        return new ApiErrors(bindingResult);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleBussinessException(BusinessException ex) {
        return new ApiErrors(ex);
    }
}
