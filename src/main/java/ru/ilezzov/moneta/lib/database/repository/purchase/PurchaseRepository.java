package ru.ilezzov.moneta.lib.database.repository.purchase;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import ru.ilezzov.moneta.lib.database.SQLDatabase;
import ru.ilezzov.moneta.lib.database.repository.DataRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PurchaseRepository implements DataRepository<Long, Purchase> {
    private final SQLDatabase database;
    private final Cache<Long, Purchase> cache;

    public PurchaseRepository(final SQLDatabase database) {
        this.database = database;
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .removalListener((Long value, Purchase purchase, RemovalCause removalCause) -> {
                    if (removalCause == RemovalCause.EXPIRED || removalCause == RemovalCause.SIZE) {
                        save(purchase);
                    }
                })
                .build();
    }

    @Override
    public CompletableFuture<Purchase> get(final Long key) {
        final Purchase purchase = cache.getIfPresent(key);

        if (purchase != null) {
            return CompletableFuture.completedFuture(purchase);
        }

        return CompletableFuture.supplyAsync(() -> loadFromDatabase(key));
    }

    private Purchase loadFromDatabase(final Long id) {
        if (id == null) {
            return null;
        }

        final String sql = "SELECT * FROM purchases WHERE id = ?";

        try (final ResultSet resultSet = database.executePreparedQuery(sql, id)) {
            if (!resultSet.next()) {
                return null;
            }

            final Purchase purchase = getPurchaseFromResultSet(id, resultSet);

            this.cache.put(id, purchase);
            return purchase;
        } catch (final SQLException e) {
            return null;
        }
    }

    @Override
    public CompletableFuture<List<Purchase>> getAll(final boolean addToCache) {
        return getAll(addToCache, LocalDateTime.MIN);
    }

    @Override
    public CompletableFuture<List<Purchase>> getAll(final boolean addToCache, final LocalDateTime date) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT * FROM purchases WHERE purchase_date > ?";
            final Object[] params = new Object[]{date};

            try (final ResultSet resultSet = database.executePreparedQuery(sql, params)) {
                final List<Purchase> purchases = new ArrayList<>();

                while (resultSet.next()) {
                    final long id = resultSet.getLong("id");
                    final Purchase purchase = getPurchaseFromResultSet(id, resultSet);

                    if (addToCache) {
                        this.cache.put(id, purchase);
                    }

                    purchases.add(purchase);
                }

                return purchases;
            } catch (final SQLException e) {
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Void> insert(final Purchase value) {
        return CompletableFuture.runAsync(() -> {
            final String sql = """
            INSERT INTO purchases (
                product_id, product_name, cost_price, quantity, supplier, comment
            ) VALUES (?, ?, ?, ?, ?, ?) RETURNING id, purchase_date, total_price
        """;

            final Object[] params = {
                    value.getProductId(),
                    value.getProductName(),
                    value.getCostPrice(),
                    value.getQuantity(),
                    value.getSupplier(),
                    value.getComment()
            };

            try {
                final ResultSet resultSet = database.executePreparedQuery(sql, params);

                if (resultSet.next()) {
                    final long id = resultSet.getLong("id");
                    final LocalDateTime date = resultSet.getTimestamp("purchase_date").toLocalDateTime();
                    final double totalPrice = resultSet.getDouble("total_price");

                    value.setId(id);
                    value.setDate(date);
                    value.setTotalPrice(totalPrice);

                    cache.put(id, value);
                }
            } catch (final SQLException e) {

            }
        });
    }

    @Override
    public CompletableFuture<Void> save(final Purchase value) {
        return CompletableFuture.runAsync(() -> {
            final String sql = """
            UPDATE purchases 
            SET purchase_date = ?,
                product_id = ?,
                product_name = ?,
                cost_price = ?,
                quantity = ?,
                supplier = ?,
                comment = ?
            WHERE id = ?
            """;

            final Object[] params = {
                    value.getDate(),
                    value.getProductId(),
                    value.getProductName(),
                    value.getCostPrice(),
                    value.getQuantity(),
                    value.getSupplier(),
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
            UPDATE purchases 
            SET purchase_date = ?,
                product_id = ?,
                product_name = ?,
                cost_price = ?,
                quantity = ?,
                supplier = ?,
                comment = ?
            WHERE id = ?
            """;

            final Map<Long, Purchase> map = cache.asMap();
            final List<Object[]> bathParams = getObjects(map);

            try {
                database.executePreparedBatchUpdate(sql, bathParams);
            } catch (final SQLException e) {

            }
        });
    }

    private static List<Object[]> getObjects(final Map<Long, Purchase> map) {
        final List<Object[]> batchParams = new ArrayList<>(map.size());

        for (final Purchase value: map.values()) {
            final Object[] params = {
                    value.getDate(),
                    value.getProductId(),
                    value.getProductName(),
                    value.getCostPrice(),
                    value.getQuantity(),
                    value.getSupplier(),
                    value.getComment(),
                    value.getId()
            };
            batchParams.add(params);
        }
        return batchParams;
    }

    private Purchase getPurchaseFromResultSet(final long id, final ResultSet resultSet) throws SQLException {
        final LocalDateTime date = resultSet.getTimestamp("purchase_date").toLocalDateTime();
        final long productId = resultSet.getLong("product_id");
        final String productName = resultSet.getString("product_name");
        final double cost_price = resultSet.getDouble("cost_price");
        final int quantity = resultSet.getInt("quantity");
        final double total_price = resultSet.getDouble("total_price");
        final String supplier = resultSet.getString("supplier");
        final String comment = resultSet.getString("comment");

        return new Purchase(id,
                date,
                productId,
                productName,
                cost_price,
                quantity,
                total_price,
                supplier,
                comment);
    }
}
