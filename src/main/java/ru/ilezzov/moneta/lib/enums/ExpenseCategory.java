package ru.ilezzov.moneta.lib.enums;


public enum ExpenseCategory {
    ADVERTISEMENT("Реклама"),
    PURCHASE("Закупка"),
    TAXES("Налоги"),
    OTHER("Прочее");

    final String category;

    ExpenseCategory(final String description) {
        this.category = description;
    }

    public String getCategory() {
        return category;
    }

    public static ExpenseCategory parseCategory(final String category) {
        for (final ExpenseCategory expenseCategory : ExpenseCategory.values()) {
            if (expenseCategory.getCategory().equals(category)) {
                return expenseCategory;
            }
        }
        return null;
    }
}
