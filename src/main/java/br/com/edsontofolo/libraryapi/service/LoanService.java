package br.com.edsontofolo.libraryapi.service;

import br.com.edsontofolo.libraryapi.api.dto.LoanFilterDTO;
import br.com.edsontofolo.libraryapi.api.resource.BookController;
import br.com.edsontofolo.libraryapi.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LoanService {
    Loan save(Loan loan);
    Optional<Loan> findById(Long id);
    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO filter, Pageable pageRe);
}
