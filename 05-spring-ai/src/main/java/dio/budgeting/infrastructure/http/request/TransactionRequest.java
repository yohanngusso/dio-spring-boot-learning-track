package dio.budgeting.infrastructure.http.request;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransactionRequest(
        @NotBlank(message = "a descrição é obrigatória")
        @Size(max = Transaction.MAX_DESCRIPTION_LENGTH, message = "a descrição deve ter no máximo 255 caracteres")
        String description,

        @NotNull(message = "a categoria é obrigatória")
        Category category,

        @Positive(message = "o valor deve ser maior que zero (em centavos)")
        long amount) {
    public PersistTransactionInput toInput() {
        return new PersistTransactionInput(description, amount, category);
    }
}
