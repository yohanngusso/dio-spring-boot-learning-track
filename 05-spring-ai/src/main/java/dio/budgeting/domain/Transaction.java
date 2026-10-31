package dio.budgeting.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Transaction {
    public static final int MAX_DESCRIPTION_LENGTH = 255;

    private TransactionId id;
    private String description;
    private long amount;
    private Category category;

    public Transaction(String description, long amount, Category category) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("A descrição da transação é obrigatória");
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(
                    "A descrição da transação deve ter no máximo " + MAX_DESCRIPTION_LENGTH + " caracteres");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser maior que zero (em centavos)");
        }
        if (category == null) {
            throw new IllegalArgumentException("A categoria da transação é obrigatória");
        }

        this.id = new TransactionId();
        this.description = description;
        this.amount = amount;
        this.category = category;
    }
}
