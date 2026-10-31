package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersistTransactionUseCaseTest {
    private final TransactionRepository repository = mock(TransactionRepository.class);
    private final PersistTransactionUseCase useCase = new PersistTransactionUseCase(repository);

    @Test
    void should_persist_and_return_value_in_reais_when_input_is_valid() {
        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var output = useCase.execute(new PersistTransactionInput("Mercado", 8000, Category.GROCERIES));

        assertThat(output.description()).isEqualTo("Mercado");
        assertThat(output.category()).isEqualTo("GROCERIES");
        assertThat(output.value()).isEqualTo(80.00);
    }

    @Test
    void should_not_reach_repository_when_amount_is_zero() {
        assertThatThrownBy(() -> useCase.execute(new PersistTransactionInput("Mercado", 0, Category.GROCERIES)))
                .isInstanceOf(IllegalArgumentException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void should_not_reach_repository_when_category_is_missing() {
        assertThatThrownBy(() -> useCase.execute(new PersistTransactionInput("Mercado", 8000, null)))
                .isInstanceOf(IllegalArgumentException.class);

        verify(repository, never()).save(any());
    }
}
