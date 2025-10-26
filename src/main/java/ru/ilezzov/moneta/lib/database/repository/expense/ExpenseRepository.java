package ru.ilezzov.moneta.lib.database.repository.expense;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import ru.ilezzov.moneta.lib.database.SQLDatabase;
import ru.ilezzov.moneta.lib.database.repository.DataRepository;
import ru.ilezzov.moneta.lib.enums.ExpenseCategory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ExpenseRepository implements DataRepository<Long, Expense> {
    private final SQLDatabase database;
    private final Cache<Long, Expense> cache;

    public ExpenseRepository(final SQLDatabase database) {
        this.database = database;
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .removalListener((Long value, Expense Expense, RemovalCause removalCause) -> {
                    if (removalCause == RemovalCause.EXPIRED || removalCause == RemovalCause.SIZE) {
                        save(Expense);
                    }
                })
                .build();
    }

    @Override
    public CompletableFuture<Expense> get(final Long key) {
        final Expense Expense = cache.getIfPresent(key);

        if (Expense != null) {
            return CompletableFuture.completedFuture(Expense);
        }

        return CompletableFuture.supplyAsync(() -> loadFromDatabase(key));
    }

    private Expense loadFromDatabase(final Long id) {
        if (id == null) {
            return null;
        }

        final String sql = "SELECT * FROM expenses WHERE id = ?";

        try (final ResultSet resultSet = database.executePreparedQuery(sql, id)) {
            if (!resultSet.next()) {
                return null;
            }

            final Expense expense = getExpenseFromResultSet(id, resultSet);

            this.cache.put(id, expense);
            return expense;
        } catch (final SQLException e) {
            return null;
        }
    }

    @Override
    public CompletableFuture<List<Expense>> getAll(final boolean addToCache) {
        return getAll(addToCache, LocalDateTime.MIN);
    }

    @Override
    public CompletableFuture<List<Expense>> getAll(final boolean addToCache, final LocalDateTime date) {
       return CompletableFuture.supplyAsync(() -> {
           final String sql = "SELECT * FROM expenses WHERE expense_date > ?";
           final Object[] params = new Object[]{date};

           try(final ResultSet resultSet = database.executePreparedQuery(sql, params)) {
               final List<Expense> expenses = new ArrayList<>();

               while (resultSet.next()) {
                   final long id = resultSet.getLong("id");
                   final Expense expense = getExpenseFromResultSet(id, resultSet);

                   if (addToCache) {
                       this.cache.put(id, expense);
                   }
                   expenses.add(expense);
               }

               return expenses;
           } catch (SQLException e) {
               return null;
           }
       });
    }

    @Override
    public CompletableFuture<Void> insert(final Expense value) {
        return CompletableFuture.runAsync(() -> {
            if (value == null) {
                return;
            }

            final String sql = "INSERT INTO  expenses (category, description, amount, comment) VALUES (?, ?, ?, ?) RETURNING id, expense_date";

            final Object[] params = {
                    value.getCategory().getCategory(),
                    value.getDescription(),
                    value.getAmount(),
                    value.getComment()
            };

            try {
                final ResultSet resultSet = database.executePreparedQuery(sql, params);

                if (resultSet.next()) {
                    final long id = resultSet.getLong("id");
                    final LocalDateTime date = resultSet.getTimestamp("expense_date").toLocalDateTime();

                    value.setId(id);
                    value.setDate(date);

                    cache.put(id, value);
                }
            } catch (final SQLException e) {

            }
        });
    }

    @Override
    public CompletableFuture<Void> save(final Expense value) {
        return CompletableFuture.runAsync(() -> {
            if (value == null) {
                return;
            }

            final String sql = """
                    UPDATE expenses
                    SET expense_date = ?,
                        category = ?,
                        description = ?,
                        amount = ?,
                        comment = ?
                    WHERE id = ?;
                    """;

            final Object[] params = {
                    value.getDate(),
                    value.getCategory().getCategory(),
                    value.getDescription(),
                    value.getAmount(),
                    value.getComment(),
                    value.getId()
            };

            try {
                database.executePreparedUpdate(sql, params);
            } catch (final SQLException e) {

            }
        });
    }

    @Override
    public CompletableFuture<Void> saveCache() {
        return CompletableFuture.runAsync(() -> {
            final String sql = """
                    UPDATE expenses
                    SET expense_date = ?,
                        category = ?,
                        description = ?,
                        amount = ?,
                        comment = ?
                    WHERE id = ?;
                    """;;

            final Map<Long, Expense> map = this.cache.asMap();
            final List<Object[]> batchParams = getObjects(map);

            try {
                database.executePreparedBatchUpdate(sql, batchParams);
            } catch (final SQLException e) {

            }
        });
    }

    private static List<Object[]> getObjects(final Map<Long, Expense> map) {
        final List<Object[]> batchParams = new ArrayList<>(map.size());

        for (final Expense value: map.values()) {
            final Object[] params = {
                    value.getDate(),
                    value.getCategory().getCategory(),
                    value.getDescription(),
                    value.getAmount(),
                    value.getComment(),
                    value.getId()
            };
            batchParams.add(params);
        }
        return batchParams;
    }

    public CompletableFuture<ExpenseStats> getExpenseStatsByDate(final LocalDate start, final LocalDate end) {
        return CompletableFuture.supplyAsync(() ->{
            final String sql = "SELECT COALESCE(SUM(amount), 0) AS total_amount FROM expenses WHERE expense_date >= ? AND expense_date < ?;";

            final Object[] params = {
                    Timestamp.valueOf(start.atStartOfDay()),
                    Timestamp.valueOf(end.atStartOfDay())
            };

            try {
                final ResultSet resultSet = database.executePreparedQuery(sql, params);

                if (!resultSet.next()) {
                    return null;
                }

                final double totalAmount = resultSet.getDouble("total_amount");
                return new ExpenseStats(totalAmount);

            } catch (SQLException e) {
                return null;
            }
        });
    }

    public CompletableFuture<List<ExpenseStats>> getExpenseStatsGroupByMonth(final int year) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = """
            SELECT m.month,
                   COALESCE(e.total_amount, 0) AS total_amount
            FROM generate_series(1,12) AS m(month)
            LEFT JOIN (
                SELECT EXTRACT(MONTH FROM expense_date) AS month,
                       SUM(amount) AS total_amount
                FROM expenses
                WHERE EXTRACT(YEAR FROM expense_date) = ?
                GROUP BY month
            ) e ON e.month = m.month
            ORDER BY m.month;
            """;

            final Object[] params = { year };
            final List<ExpenseStats> result = new ArrayList<>();

            try {
                final ResultSet resultSet = database.executePreparedQuery(sql, params);

                while (resultSet.next()) {
                    final double totalAmount = resultSet.getDouble("total_amount");
                    result.add(new ExpenseStats(totalAmount));
                }

            } catch (SQLException e) {
            }

            return result;
        });
    }

    private Expense getExpenseFromResultSet(final long id, final ResultSet resultSet) throws SQLException {
        final LocalDateTime date = resultSet.getTimestamp("expense_date").toLocalDateTime();
        final ExpenseCategory category = ExpenseCategory.parseCategory(resultSet.getString("category"));
        final String description = resultSet.getString("description");
        final double amount = resultSet.getDouble("amount");
        final String comment = resultSet.getString("comment");

        return new Expense(
                id,
                date,
                category,
                description,
                amount,
                comment
        );
    }
}
