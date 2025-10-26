package ru.ilezzov.moneta.lib.database.repository.expense;

import ru.ilezzov.moneta.lib.enums.ExpenseCategory;
import ru.ilezzov.moneta.lib.utils.DateUtil;

import java.time.LocalDateTime;

public class Expense {
    private long id;
    private LocalDateTime date;

    private ExpenseCategory category;
    private String description;
    private double amount;
    private String comment;

    public Expense(long id, LocalDateTime date, ExpenseCategory category, String description, double amount, String comment) {
        this.id = id;
        this.date = date;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.comment = comment;
    }

    public Expense(final ExpenseCategory category, final String description, final double amount, final String comment) {
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.comment = comment;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    @Override
    public String toString() {
        return String.format(
                """
                💸 Расход #%d
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                📅 Дата:         %s
                🏷️ Категория:    %s
                📝 Описание:     %s
                💰 Сумма:        %.2f ₽
                💬 Комментарий:  %s
                """,
                id,
                DateUtil.formatDate(date),
                category.getCategory(),
                description,
                amount,
                comment
        );
    }
}

