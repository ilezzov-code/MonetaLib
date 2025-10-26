package ru.ilezzov.moneta.lib.database.repository.product;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import ru.ilezzov.moneta.lib.database.SQLDatabase;
import ru.ilezzov.moneta.lib.database.repository.DataRepository;
import ru.ilezzov.moneta.lib.enums.ProductCategory;
import ru.ilezzov.moneta.lib.enums.ProductStatus;
import ru.ilezzov.moneta.lib.enums.ProductUnit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ProductRepository implements DataRepository<Long, Product> {
    private final SQLDatabase database;
    private final Cache<Long, Product> cache;

    private final ConcurrentHashMap<String, Long> productsByName = new ConcurrentHashMap<>();

    public ProductRepository(final SQLDatabase database) {
        this.database = database;
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .removalListener((Long value, Product product, RemovalCause removalCause) -> {
                    if (removalCause == RemovalCause.EXPIRED || removalCause == RemovalCause.SIZE) {
                        save(product);
                    }
                })
                .build();
    }

    @Override
    public CompletableFuture<Product> get(final Long key) {
        final Product product = cache.getIfPresent(key);

        if (product != null) {
            return CompletableFuture.completedFuture(product);
        }

        return CompletableFuture.supplyAsync(() -> loadFromDatabase(key));
    }

    public CompletableFuture<Product> getByName(final String name) {
        final Long key = productsByName.get(name);

        if (key == null) {
            return CompletableFuture.supplyAsync(() -> loadFromDatabaseByName(name));
        }

        final Product product = cache.getIfPresent(key);

        if (product != null) {
            return CompletableFuture.completedFuture(product);
        }

        return CompletableFuture.supplyAsync(() -> loadFromDatabase(key));
    }

    private Product loadFromDatabase(final Long id) {
        if (id == null) {
            return null;
        }

        final String sql = "SELECT * FROM products WHERE id = ?";

        try (final ResultSet resultSet = database.executePreparedQuery(sql, id)) {
            if (!resultSet.next()) {
                return null;
            }

            final String name = resultSet.getString("name");
            final Product product = getProductFromResultSet(id, name, resultSet);

            this.cache.put(id, product);
            return product;
        } catch (final SQLException e) {
            return null;
        }
    }

    private Product loadFromDatabaseByName(final String name) {
        if (name == null) {
            return null;
        }

        final String sql = "SELECT * FROM products WHERE name = ?";

        try (final ResultSet resultSet = database.executePreparedQuery(sql, name)) {
            if (!resultSet.next()) {
                return null;
            }

            final long id = resultSet.getLong("id");
            final Product product =  getProductFromResultSet(id, name, resultSet);

            this.productsByName.put(name, id);
            this.cache.put(id, product);
            return product;
        } catch (final SQLException e) {
            return null;
        }
    }

    @Override
    public CompletableFuture<List<Product>> getAll(final boolean addToCache) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT * FROM products";

            try (final ResultSet resultSet = database.executeQuery(sql)) {
                final List<Product> products = new ArrayList<>();

                while (resultSet.next()) {
                    final long id = resultSet.getLong("id");
                    final String productName = resultSet.getString("name");
                    final Product product = getProductFromResultSet(id, productName, resultSet);

                    if (addToCache) {
                        this.cache.put(id, product);
                    }

                    products.add(product);
                }

                return products;
            } catch (final SQLException e) {
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<List<Product>> getAll(final boolean addToCache, final LocalDateTime date) {
        return getAll(addToCache);
    }

    private Product getProductFromResultSet(final Long id, final String productName, final ResultSet resultSet) throws SQLException {
        final ProductCategory category = ProductCategory.parseCategory(resultSet.getString("category"));
        final double cost_price = resultSet.getLong("cost_price");
        final double retail_price = resultSet.getDouble("retail_price");
        final ProductUnit unit = ProductUnit.parseUnit(resultSet.getString("unit"));
        final String supplier = resultSet.getString("supplier");
        final int stock = resultSet.getInt("stock");
        final int minimum = resultSet.getInt("minimum");
        final ProductStatus productStatus = ProductStatus.parseStatus(resultSet.getString("status"));

        return new Product(
                id,
                productName,
                category,
                cost_price,
                retail_price,
                unit,
                supplier,
                stock,
                minimum,
                productStatus
        );
    }

    @Override
    public CompletableFuture<Void> insert(final Product value) {
        return CompletableFuture.runAsync(() -> {
            if (value == null) {
                return;
            }

            final String sql = "INSERT INTO products (name, category, cost_price, retail_price, unit, supplier, stock, minimum, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

            final Object[] params = {
                    value.getName(),
                    value.getCategory().getCategory(),
                    value.getCostPrice(),
                    value.getRetailPrice(),
                    value.getUnit().getUnit(),
                    value.getSupplier(),
                    value.getStock(),
                    value.getMinimum(),
                    value.getStatus().getStatus()
            };

            try {
                final ResultSet resultSet = database.executePreparedQuery(sql, params);

                if (resultSet.next()) {
                    final long id = resultSet.getLong("id");
                    value.setId(id);

                    cache.put(id, value);
                }
            } catch (final SQLException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> save(final Product value) {
        return CompletableFuture.runAsync(() -> {
            if (value == null) {
                return;
            }

            final String sql = """
                    UPDATE products
                    SET name = ?,
                        category = ?,
                        cost_price = ?,
                        retail_price = ?,
                        unit = ?,
                        supplier = ?,
                        stock = ?,
                        minimum = ?,
                        status = ?
                    WHERE id = ?;
                    """;

            final Object[] params = {
                    value.getName(),
                    value.getCategory().getCategory(),
                    value.getCostPrice(),
                    value.getRetailPrice(),
                    value.getUnit().getUnit(),
                    value.getSupplier(),
                    value.getStock(),
                    value.getMinimum(),
                    value.getStatus().getStatus(),
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
                UPDATE products
                    SET name = ?,
                        category = ?,
                        cost_price = ?,
                        retail_price = ?,
                        unit = ?,
                        supplier = ?,
                        stock = ?,
                        minimum = ?,
                        status = ?
                    WHERE id = ?;
                    """;

            final Map<Long, Product> map = this.cache.asMap();
            final List<Object[]> batchParams = getObjects(map);

            try {
                database.executePreparedBatchUpdate(sql, batchParams);
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private static List<Object[]> getObjects(final Map<Long, Product> map) {
        final List<Object[]> batchParams = new ArrayList<>(map.size());

        for (final Product value: map.values()) {
            final Object[] params = {
                    value.getName(),
                    value.getCategory().getCategory(),
                    value.getCostPrice(),
                    value.getRetailPrice(),
                    value.getUnit().getUnit(),
                    value.getSupplier(),
                    value.getStock(),
                    value.getMinimum(),
                    value.getStatus().getStatus(),
                    value.getId()
            };
            batchParams.add(params);
        }
        return batchParams;
    }
}
