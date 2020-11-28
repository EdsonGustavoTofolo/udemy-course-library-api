package br.com.edsontofolo.libraryapi.service;

import br.com.edsontofolo.libraryapi.api.dto.LoanFilterDTO;
import br.com.edsontofolo.libraryapi.exception.BusinessException;
import br.com.edsontofolo.libraryapi.model.entity.Book;
import br.com.edsontofolo.libraryapi.model.entity.Loan;
import br.com.edsontofolo.libraryapi.model.repository.LoanRepository;
import br.com.edsontofolo.libraryapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    private LoanService service;
    @MockBean
    private LoanRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Must save loan")
    public void saveTest() {
        Book book = Book.builder().id(1L).build();
        Loan savingLoan = Loan.builder()
                .book(book)
                .customer("Edson")
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = Loan.builder()
                .id(1L)
                .loanDate(LocalDate.now())
                .customer("Edson")
                .book(book)
                .build();

        Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
        Mockito.when(repository.save(savingLoan)).thenReturn(savedLoan);

        Loan loan = service.save(savingLoan);

        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Must throws an exception to try make loan of invalid book")
    public void loanedSaveBookTest() {
        Book book = Book.builder().id(1L).build();
        Loan savingLoan = Loan.builder()
                .book(book)
                .customer("Edson")
                .loanDate(LocalDate.now())
                .build();

        Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

        Throwable ex = catchThrowable(() -> service.save(savingLoan));

        assertThat(ex).isInstanceOf(BusinessException.class).hasMessage("Book already loaned");

        verify(repository, never()).save(savingLoan);
    }

    @Test
    @DisplayName("Must get loan informations by id")
    public void getLoanInfoTest() {
        // cenario
        Long id = 1L;
        Loan loan = createLoan();
        loan.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(loan));

        // execucao
        Optional<Loan> result = service.findById(id);

        //verificacao
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getBook().getId()).isEqualTo(loan.getBook().getId());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Must update loan")
    public void updateLoanTest() {
        Loan loan = createLoan();
        loan.setId(1L);
        loan.setReturned(Boolean.TRUE);

        when(repository.save(loan)).thenReturn(loan);

        Loan updatedLoan = service.update(loan);

        assertThat(updatedLoan.getReturned()).isTrue();

        verify(repository).save(loan);
    }

    public Loan createLoan() {
        Book book = Book.builder().id(1L).build();
        return Loan.builder()
                .book(book)
                .customer("Edson")
                .loanDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Must filter loans by properties")
    public void findLoanTest() {
        // cenario
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Edson").isbn("123").build();

        Loan loan = createLoan();
        loan.setId(1L);

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> list = new ArrayList<>();
        list.add(loan);
        Page<Loan> page = new PageImpl<>(list, pageRequest, 1);

        Mockito
                .when(
                        repository
                                .findByBookIsbnOrCustomer(Mockito.anyString(),
                                        Mockito.anyString(),
                                        Mockito.any(PageRequest.class)))
                .thenReturn(page);

        // execucao
        Page<Loan> result = service.find(loanFilterDTO, pageRequest);

        // verificacao
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }
}
