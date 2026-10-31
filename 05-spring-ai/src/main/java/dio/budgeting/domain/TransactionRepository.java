package dio.budgeting.domain;

import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction);

    List<Transaction> findAllByCategory(Category category);

    /** Soma, em centavos, das transações da categoria. Retorna 0 quando não há nenhuma. */
    long sumAmountByCategory(Category category);
}
