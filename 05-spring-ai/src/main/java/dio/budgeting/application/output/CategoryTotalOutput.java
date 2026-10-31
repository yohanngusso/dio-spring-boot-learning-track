package dio.budgeting.application.output;

import dio.budgeting.domain.Category;

public record CategoryTotalOutput(String category, double total) {
    public static CategoryTotalOutput from(Category category, long totalInCents) {
        return new CategoryTotalOutput(category.name(), TransactionOutput.centsToReais(totalInCents));
    }
}
