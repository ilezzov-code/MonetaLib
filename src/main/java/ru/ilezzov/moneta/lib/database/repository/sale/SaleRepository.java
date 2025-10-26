package ru.ilezzov.moneta.lib.database.repository.sale;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import ru.ilezzov.moneta.lib.api.model.Response;
import ru.ilezzov.moneta.lib.database.SQLDatabase;
import ru.ilezzov.moneta.lib.database.repository.DataRepository;
import ru.ilezzov.moneta.lib.enums.Marketplace;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SaleRepository implements DataRepository<Long, Sale> {
    private final SQLDatabase database;
    private final Cache<Long, Sale> cache;

    public SaleRepository(final SQLDatabase database) {
        this.database = database;
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000) // Максимум 1000 записей в кеше
                .expireAfterWrite(5, TimeUnit.MINUTES) 
                .removalListener((Long value, Sale sale, RemovalCause removalCause) -> {
                    if (removalCause == RemovalCause.EXPIRED || removalCause == RemovalCause.SIZE) {
                        save(sale);
                    }
                })
                .build();
    }

    @Override
    public CompletableFuture<Sale> get(final Long id) {
        final Sale sale = cache.getIfPresent(id);

        if (sale != null) {
            return CompletableFuture.completedFuture(sale);
        }

        return CompletableFuture.supplyAsync(() -> loadFromDatabase(id));
    }

    private Sale loadFromDatabase(final Long id) {
        if (id == null) {
            return null;
        }

        final String sql = "SELECT * FROM sales WHERE id = ?";

        try (final ResultSet resultSet = database.executePreparedQuery(sql, id)) {
            if (!resultSet.next()) {
                return null;
            }

            final Sale sale = getSaleFromResultSet(id, resultSet);

            this.cache.put(id, sale);
            return sale;
        } catch (final SQLException e) {
            return null;
        }
    }

    @Override
    public CompletableFuture<List<Sale>> getAll(final boolean addToCache) {
        return getAll(addToCache, LocalDateTime.MIN);
    }

    @Override
    public CompletableFuture<List<Sale>> getAll(final boolean addToCache, final LocalDateTime date) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT * FROM sales WHERE sale_date > ?";
            final Object[] params = new Object[]{date};

            try (final ResultSet resultSet = database.executePreparedQuery(sql, params)) {
                final List<Sale> saleList = new ArrayList<>();

                while (resultSet.next()) {
                    final long id = resultSet.getLong("id");
                    final Sale sale = getSaleFromResultSet(id, resultSet);

                    if (addToCache) {
                        this.cache.put(id, sale);
                    }

                    saleList.add(sale);
                }

                return saleList;
            } catch (final SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Void> insert(final Sale value) {
        return CompletableFuture.runAsync(() -> {
            if (value == null) {
                return;
            }

            final String sql = "INSERT INTO sales (product_id, product_name, quantity, unit_price, cost_price, marketplace, comment) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id, sale_date, total_price, margin";

            final Object[] params = {
                value.getProductId(),
                value.getProductName(),
                value.getQuantity(),
                value.getUnitPrice(),
                value.getCostPrice(),
                value.getMarketplace().getMarketplace(),
                value.getComment()
            };

            try {
                final ResultSet resultSet = database.executePreparedQuery(sql, params);

                if (resultSet.next()) {
                    final long id = resultSet.getLong("id");
                    final LocalDateTime date = resultSet.getTimestamp("sale_date").toLocalDateTime();
                    final double total_price = resultSet.getDouble("total_price");
                    final double margin = resultSet.getDouble("margin");

                    value.setId(id);
                    value.setSaleDate(date);
                    value.setTotalPrice(total_price);
                    value.setMargin(margin);

                    cache.put(id, value);
                }
            } catch (final SQLException e) {

            }
        });
    }

    @Override
    public CompletableFuture<Void> save(final Sale value) {
        return CompletableFuture.runAsync(() -> {
            if (value == null) {
                return;
            }

            final String sql = """
                UPDATE sales
                SET sale_date = ?,
                    product_id = ?,
                    product_name = ?,
                    quantity = ?,
                    unit_price = ?,
                    cost_price = ?,
                    marketplace = ?,
                    comment = ?
                WHERE id = ?;
                """;

            final Object[] params = {
                    value.getSaleDate(),
                    value.getProductId(),
                    value.getProductName(),
                    value.getQuantity(),
                    value.getUnitPrice(),
                    value.getCostPrice(),
                    value.getMarketplace().getMarketplace(),
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
                UPDATE sales
                SET sale_date = ?,
                    product_id = ?,
                    product_name = ?,
                    quantity = ?,
                    unit_price = ?,
                    cost_price = ?,
                    marketplace = ?,
                    comment = ?
                WHERE id = ?;
                """;

            final Map<Long, Sale> map = cache.asMap();
            final List<Object[]> batchParams = getObjects(map);

            try {
                database.executePreparedBatchUpdate(sql, batchParams);
            } catch (final SQLException e) {

            }
        });
    }

    public CompletableFuture<SaleStats> getSaleStatsByDate(final LocalDate start, final LocalDate end) {
        return CompletableFuture.supplyAsync(() ->{
            final String sql = """
            SELECT COUNT(*) AS sales_count,
                COALESCE(SUM(total_price), 0) AS total_turnover,
                COALESCE(SUM(margin), 0) AS total_revenue,
                COALESCE(AVG(margin), 0) AS average_margin
            FROM sales
            WHERE sale_date >= ? AND sale_date < ?;
            """;

            final Object[] params = {
                    Timestamp.valueOf(start.atStartOfDay()),
                    Timestamp.valueOf(end.atStartOfDay())
            };

            try {
                final ResultSet resultSet = database.executePreparedQuery(sql, params);

                if (!resultSet.next()) {
                    return null;
                }

                final int salesCount = resultSet.getInt("sales_count");
                final double totalTurnover = resultSet.getDouble("total_turnover");
                final double totalRevenue = resultSet.getDouble("total_revenue");
                final double averageMargin = resultSet.getDouble("average_margin");
                return new SaleStats(salesCount, totalTurnover, totalRevenue, averageMargin);

            } catch (SQLException e) {
                return null;
            }
        });
    }

    public CompletableFuture<List<SaleStats>> getSaleStatsGroupByMonth(final int year) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = """
            SELECT m.month,
                   COALESCE(s.sales_count, 0) AS sales_count,
                   COALESCE(s.total_turnover, 0) AS total_turnover,
                   COALESCE(s.total_revenue, 0) AS total_revenue,
                   COALESCE(s.average_margin, 0) AS average_margin
            FROM generate_series(1, 12) AS m(month)
            LEFT JOIN (
                SELECT EXTRACT(MONTH FROM sale_date) AS month,
                       COUNT(*) AS sales_count,
                       SUM(total_price) AS total_turnover,
                       SUM(margin) AS total_revenue,
                       AVG(margin) AS average_margin
                FROM sales
                WHERE EXTRACT(YEAR FROM sale_date) = ?
                GROUP BY month
            ) s ON s.month = m.month
            ORDER BY m.month;
            """;

            final Object[] params = { year };
            final List<SaleStats> result = new ArrayList<>();

            try {
                final ResultSet rs = database.executePreparedQuery(sql, params);

                while (rs.next()) {
                    int salesCount = rs.getInt("sales_count");
                    double totalTurnover = rs.getDouble("total_turnover");
                    double totalRevenue = rs.getDouble("total_revenue");
                    double averageMargin = rs.getDouble("average_margin");

                    result.add(new SaleStats(salesCount, totalTurnover, totalRevenue, averageMargin));
                }
                return result;

            } catch (SQLException e) {
                return null;
            }
        });
    }

    private List<Object[]> getObjects(final Map<Long, Sale> map) {
        final List<Object[]> batchParams = new ArrayList<>(map.size());

        for (final Sale value: map.values()) {
            final Object[] params = {
                    value.getSaleDate(),
                    value.getProductId(),
                    value.getProductName(),
                    value.getQuantity(),
                    value.getUnitPrice(),
                    value.getCostPrice(),
                    value.getMarketplace().getMarketplace(),
                    value.getComment(),
                    value.getId()
            };
            batchParams.add(params);
        }
        return batchParams;
    }

    private Sale getSaleFromResultSet(final long id, final ResultSet resultSet) throws SQLException {
        final LocalDateTime date = resultSet.getTimestamp("sale_date").toLocalDateTime();
        final long productId = resultSet.getLong("product_id");
        final String productName = resultSet.getString("product_name");
        final int quantity = resultSet.getInt("quantity");
        final double unitPrice = resultSet.getDouble("unit_price");
        final double total_price = resultSet.getDouble("total_price");
        final double costPrice = resultSet.getDouble("cost_price");
        final double margin = resultSet.getDouble("margin");
        final Marketplace marketPlace = Marketplace.parseMarketPlace(resultSet.getString("marketplace"));
        final String comment = resultSet.getString("comment");

        return new Sale(
                id,
                date,
                productId,
                productName,
                quantity,
                unitPrice,
                total_price,
                costPrice,
                margin,
                marketPlace,
                comment
        );
    }
}
