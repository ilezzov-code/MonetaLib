package ru.ilezzov.moneta.lib.core;

import ru.ilezzov.moneta.lib.api.FinanceManager;
import ru.ilezzov.moneta.lib.api.model.Response;
import ru.ilezzov.moneta.lib.api.model.Stats;
import ru.ilezzov.moneta.lib.database.SQLDatabase;
import ru.ilezzov.moneta.lib.database.repository.expense.Expense;
import ru.ilezzov.moneta.lib.database.repository.expense.ExpenseRepository;
import ru.ilezzov.moneta.lib.database.repository.expense.ExpenseStats;
import ru.ilezzov.moneta.lib.database.repository.export.LastExport;
import ru.ilezzov.moneta.lib.database.repository.export.LastExportRepository;
import ru.ilezzov.moneta.lib.database.repository.product.Product;
import ru.ilezzov.moneta.lib.database.repository.product.ProductRepository;
import ru.ilezzov.moneta.lib.database.repository.purchase.Purchase;
import ru.ilezzov.moneta.lib.database.repository.purchase.PurchaseRepository;
import ru.ilezzov.moneta.lib.database.repository.sale.Sale;
import ru.ilezzov.moneta.lib.database.repository.sale.SaleRepository;
import ru.ilezzov.moneta.lib.database.repository.sale.SaleStats;
import ru.ilezzov.moneta.lib.enums.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CoreFinanceManager implements FinanceManager {
    private final SQLDatabase database;

    private final ExpenseRepository expenseRepository;
    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final SaleRepository saleRepository;
    private final LastExportRepository lastExportRepository;

    private final String DEFAULT_COMMENT = "———";

    public CoreFinanceManager(final SQLDatabase database) {
        this.database = database;

        this.expenseRepository = new ExpenseRepository(database);
        this.productRepository = new ProductRepository(database);
        this.purchaseRepository = new PurchaseRepository(database);
        this.saleRepository = new SaleRepository(database);
        this.lastExportRepository = new LastExportRepository(database);
    }

    @Override
    public CompletableFuture<Response> addSale(final String productName, final int quantity, final Marketplace marketplace) {
        return addSale(productName, quantity, marketplace, this.DEFAULT_COMMENT);
    }

    @Override
    public CompletableFuture<Response> addSale(final String productName, final int quantity, final Marketplace marketplace, final String comment) {
        return productRepository.getByName(productName)
                .thenCompose(product -> {
                    if (product == null) {
                        return CompletableFuture.completedFuture(Response.error("Product not found"));
                    }

                    final Sale sale = new Sale(product.getId(), productName, quantity, product.getRetailPrice(), product.getCostPrice(), marketplace, comment);
                    product.reduceStock(quantity);

                    return saleRepository.insert(sale)
                            .thenApply(v -> Response.ok("Sale added successfully!"));
                })
                .exceptionally(ex -> Response.error("Failed to add sale: " + ex.getMessage()));
    }

    @Override
    public CompletableFuture<List<Sale>> getAllSales(final boolean addToCache) {
        return getAllSales(addToCache, LocalDateTime.MIN);
    }

    @Override
    public CompletableFuture<List<Sale>> getAllSales(final boolean addToCache, final LocalDateTime date) {
        return this.saleRepository.getAll(addToCache, date);
    }

    @Override
    public CompletableFuture<Product> getProduct(final long id) {
        return this.productRepository.get(id);
    }

    @Override
    public CompletableFuture<Product> getProduct(final String productName) {
        return this.productRepository.getByName(productName);
    }

    @Override
    public CompletableFuture<List<Product>> getAllProducts(final boolean addToCache) {
        return this.productRepository.getAll(addToCache);
    }

    @Override
    public CompletableFuture<Response> addExpense(final ExpenseCategory category, final String description, final double amount) {
        return addExpense(category, description, amount, this.DEFAULT_COMMENT);
    }

    @Override
    public CompletableFuture<Response> addExpense(final ExpenseCategory category, final String description, final double amount, final String comment) {
        final Expense expense = new Expense(category,  description, amount, comment);

        return expenseRepository.insert(expense)
                .thenApply(v -> Response.ok("Expense added successfully!"))
                .exceptionally(ex -> Response.error("Failed to add expense: " + ex.getMessage()));
    }

    @Override
    public CompletableFuture<List<Expense>> getAllExpenses(final boolean addToCache) {
        return this.expenseRepository.getAll(addToCache);
    }

    public CompletableFuture<List<Expense>> getAllExpenses(final boolean addToCache, final LocalDateTime date) {
        return this.expenseRepository.getAll(addToCache, date);
    }

    @Override
    public CompletableFuture<Response> addPurchase(final String productName, final int quantity, final boolean includeInSales) {
        return addPurchase(productName, quantity, includeInSales, this.DEFAULT_COMMENT);
    }

    @Override
    public CompletableFuture<Response> addPurchase(final String productName, final int quantity, final boolean includeInExpense, final String comment) {
        return this.productRepository.getByName(productName)
                .thenCompose(product -> {
                    if (product == null) {
                        return CompletableFuture.completedFuture(Response.error("Product not found"));
                    }

                    final Purchase purchase = new Purchase(product.getId(), productName, product.getCostPrice(), quantity, product.getSupplier(), comment);
                    product.increaseStock(quantity);

                    return purchaseRepository.insert(purchase)
                            .thenCompose(v -> {
                                if (includeInExpense) {
                                    return addExpense(ExpenseCategory.PURCHASE, "Закупка товара " + productName, product.getCostPrice() * quantity, comment).thenApply(r -> Response.ok("Purchase and Expense added successfully!"));
                                } else {
                                    return CompletableFuture.completedFuture(Response.ok("Purchase added successfully!"));
                                }
                            });
                }).exceptionally(ex ->  Response.error("Failed to add purchase: " + ex.getMessage()));
    }

    @Override
    public CompletableFuture<List<Purchase>> getAllPurchases(final boolean addToCache) {
        return getAllPurchases(addToCache, LocalDateTime.MIN);
    }

    public CompletableFuture<List<Purchase>> getAllPurchases(final boolean addToCache, final LocalDateTime date) {
        return this.purchaseRepository.getAll(addToCache, date);
    }

    @Override
    public CompletableFuture<Response> addProduct(final String name, final ProductCategory category, final double costPrice, final double retailPrice, final ProductUnit unit, final String supplier, final int minimum, final ProductStatus status) {
        final Product product = new Product(name, category, costPrice, retailPrice, unit, supplier, 0, minimum, status);
        return this.productRepository.insert(product).thenApply(v -> Response.ok("Product added successfully!")).exceptionally(ex ->  Response.error("Failed to add product: " + ex.getMessage()));
    }

    @Override
    public CompletableFuture<Stats> getMonthlyStats(final MonthEnum month) {
        return getMonthlyStats(month, -1);
    }

    @Override
    public CompletableFuture<Stats> getMonthlyStats(final MonthEnum month, int year) {
        if (year == -1) {
            year = LocalDate.now().getYear();
        }

        LocalDate start;
        LocalDate end;
        if (month == MonthEnum.ALL) {
            start = LocalDate.of(year, 1, 1);
            end = LocalDate.of(year, 12, 31);
        } else {
            start = LocalDate.of(year, month.getMonth(), 1);
            end = start.plusMonths(1);
        }
        final int finalYear = year;

        final CompletableFuture<SaleStats> salesFuture = saleRepository.getSaleStatsByDate(start, end);
        final CompletableFuture<ExpenseStats> expensesFuture = expenseRepository.getExpenseStatsByDate(start, end);

        return salesFuture.thenCombine(expensesFuture, (saleStats, expenseStats) -> {
            double totalTurnover;
            double totalRevenue;
            double totalExpenses;
            double averageMargin;

            int salesCount;
            if (saleStats == null) {
                totalTurnover = 0;
                totalRevenue = 0;
                averageMargin = 0;
                salesCount = 0;
            } else {
                totalTurnover = saleStats.totalTurnover();
                totalRevenue = saleStats.totalRevenue();
                averageMargin = saleStats.averageMargin();
                salesCount = saleStats.salesCount();
            }

            if (expenseStats == null) {
                totalExpenses = 0;
            } else {
                totalExpenses = expenseStats.totalAmount();
            }

            double totalProfit = totalRevenue - totalExpenses;
            double roi = totalExpenses == 0 ? 0 : (totalProfit / totalExpenses) * 100;

            return new Stats(month, finalYear, totalTurnover, totalRevenue, totalExpenses, totalProfit, roi, averageMargin, salesCount);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    @Override
    public CompletableFuture<List<Stats>> getYearlyStats() {
        final int year = LocalDate.now().getYear();
        return getYearlyStats(year);
    }

    @Override
    public CompletableFuture<List<Stats>> getYearlyStats(final int year) {
        final CompletableFuture<List<SaleStats>> salesFuture = saleRepository.getSaleStatsGroupByMonth(year);
        final CompletableFuture<List<ExpenseStats>> expensesFuture = expenseRepository.getExpenseStatsGroupByMonth(year);

        return salesFuture.thenCombine(expensesFuture, (saleStats, expenseStats) -> {
           final List<Stats> statsList = new ArrayList<>();

           for (int i = 0; i <= 11; i++) {
               final SaleStats saleStat = saleStats.get(i);
               final ExpenseStats expenseStat = expenseStats.get(i);

               double totalTurnover = saleStat.totalTurnover();
               double totalRevenue = saleStat.totalRevenue();
               double totalExpenses = expenseStat.totalAmount();
               double totalProfit = totalRevenue - totalExpenses;
               double roi = totalExpenses == 0 ? 0 : (totalProfit / totalExpenses) * 100;
               double averageMargin = saleStat.averageMargin();
               int salesCount = saleStat.salesCount();

               statsList.add(new Stats(MonthEnum.parseMonth(i + 1), year, totalTurnover, totalRevenue, totalExpenses, totalProfit, roi, averageMargin, salesCount));
           }
           return statsList;
        });
    }

    @Override
    public CompletableFuture<Stats> getYearSummary() {
        final int  year = LocalDate.now().getYear();
        return getYearSummary(year);
    }

    @Override
    public CompletableFuture<Stats> getYearSummary(final int year) {
        return getMonthlyStats(MonthEnum.ALL, year);
    }

    @Override
    public LastExport getLastExport() {
        return this.lastExportRepository.getLastExportDate();
    }

    @Override
    public void updateLastExport(final LocalDateTime date) {
        this.lastExportRepository.update(date);
    }

    @Override
    public void close() throws SQLException {
        this.expenseRepository.saveCache().join();
        this.productRepository.saveCache().join();
        this.purchaseRepository.saveCache().join();
        this.saleRepository.saveCache().join();

        this.database.disconnect();
    }
}
