package dio.budgeting.application;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SumTransactionsByCategoryUseCaseTest {
    private final TransactionRepository repository = mock(TransactionRepository.class);
    private final SumTransactionsByCategoryUseCase useCase = new SumTransactionsByCategoryUseCase(repository);

    @Test
    void should_return_total_in_reais_when_category_has_transactions() {
        when(repository.sumAmountByCategory(Category.GROCERIES)).thenReturn(12050L);

        var output = useCase.execute(Category.GROCERIES);

        assertThat(output.category()).isEqualTo("GROCERIES");
        assertThat(output.total()).isEqualTo(120.50);
    }

    @Test
    void should_return_zero_when_category_has_no_transactions() {
        when(repository.sumAmountByCategory(Category.PHARMA)).thenReturn(0L);

        var output = useCase.execute(Category.PHARMA);

        assertThat(output.category()).isEqualTo("PHARMA");
        assertThat(output.total()).isZero();
    }
}
