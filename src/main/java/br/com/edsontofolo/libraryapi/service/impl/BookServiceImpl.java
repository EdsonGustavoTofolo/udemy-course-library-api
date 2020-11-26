package br.com.edsontofolo.libraryapi.service.impl;

import br.com.edsontofolo.libraryapi.exception.BusinessException;
import br.com.edsontofolo.libraryapi.model.entity.Book;
import br.com.edsontofolo.libraryapi.model.repository.BookRepository;
import br.com.edsontofolo.libraryapi.service.BookService;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {
    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if (repository.existsByIsbn(book.getIsbn())) {
            throw new BusinessException("Isbn já cadastrado.");
        }
        return repository.save(book);
    }
}
