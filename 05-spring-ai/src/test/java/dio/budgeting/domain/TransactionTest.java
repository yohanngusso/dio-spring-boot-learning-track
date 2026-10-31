package dio.budgeting.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    @Test
    void should_create_transaction_when_data_is_valid() {
        var transaction = new Transaction("Mercado", 8000, Category.GROCERIES);

        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getDescription()).isEqualTo("Mercado");
        assertThat(transaction.getAmount()).isEqualTo(8000);
        assertThat(transaction.getCategory()).isEqualTo(Category.GROCERIES);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void should_reject_transaction_when_description_is_blank(String description) {
        assertThatThrownBy(() -> new Transaction(description, 8000, Category.GROCERIES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descrição");
    }

    @Test
    void should_reject_transaction_when_description_is_too_long() {
        var description = "a".repeat(Transaction.MAX_DESCRIPTION_LENGTH + 1);

        assertThatThrownBy(() -> new Transaction(description, 8000, Category.GROCERIES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("255");
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -8000})
    void should_reject_transaction_when_amount_is_not_positive(long amount) {
        assertThatThrownBy(() -> new Transaction("Mercado", amount, Category.GROCERIES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valor");
    }

    @Test
    void should_reject_transaction_when_category_is_null() {
        assertThatThrownBy(() -> new Transaction("Mercado", 8000, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoria");
    }
}
