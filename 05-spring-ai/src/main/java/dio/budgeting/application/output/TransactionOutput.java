package dio.budgeting.application.output;

import dio.budgeting.domain.Transaction;

import java.math.BigDecimal;

public record TransactionOutput(String id, String description, String category, double value) {
    public static TransactionOutput from(Transaction transaction) {
        return new TransactionOutput(
                transaction.getId().uuid().toString(),
                transaction.getDescription(),
                transaction.getCategory().name(),
                centsToReais(transaction.getAmount()));
    }

    /** O domínio guarda centavos; a saída (REST e IA) fala em reais. */
    public static double centsToReais(long cents) {
        return BigDecimal.valueOf(cents, 2).doubleValue();
    }
}
