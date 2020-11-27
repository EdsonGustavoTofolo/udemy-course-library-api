package br.com.edsontofolo.libraryapi.model.repository;

import br.com.edsontofolo.libraryapi.model.entity.Book;
import br.com.edsontofolo.libraryapi.model.entity.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private LoanRepository repository;

    @Test
    @DisplayName("Must verify if exists loan for book not returned")
    public void existsByBookAndNotReturned() {
        //cenário
        Book book = createBook();
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Edson").loanDate(LocalDate.now()).build();
        entityManager.persist(loan);

        //execucao
        boolean exists = repository.existsByBookAndNotReturned(book);

        // verificacao
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Must find loan by isbn book or customer")
    public void findByBookIsbnOrCustomerTest() {
        //cenário
        Book book = createBook();
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Edson").loanDate(LocalDate.now()).build();
        entityManager.persist(loan);

        //execucao
        Page<Loan> result = repository.findByBookIsbnOrCustomer("2509119", "Edson", PageRequest.of(0, 10));

        // verificacao
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loan);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Must get loans late")
    public void findByLoansLate() {
        Book book = createBook();
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Edson").loanDate(LocalDate.now().minusDays(5)).build();
        entityManager.persist(loan);

        List<Loan> result = repository.findByLoansDateLessThanAndNotResturned(LocalDate.now().minusDays(4));

        assertThat(result).hasSize(1).contains(loan);
    }

    @Test
    @DisplayName("Must not find loans late")
    public void notFindByLoansLate() {
        Book book = createBook();
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Edson").loanDate(LocalDate.now()).build();
        entityManager.persist(loan);

        List<Loan> result = repository.findByLoansDateLessThanAndNotResturned(LocalDate.now().minusDays(4));

        assertThat(result).isEmpty();
    }

    private Book createBook() {
        return Book.builder().title("My incredible life").author("Edson").isbn("2509119").build();
    }
}
