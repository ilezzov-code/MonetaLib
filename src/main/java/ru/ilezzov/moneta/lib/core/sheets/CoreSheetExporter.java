package ru.ilezzov.moneta.lib.core.sheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import ru.ilezzov.moneta.lib.api.FinanceManager;
import ru.ilezzov.moneta.lib.api.SheetExporter;
import ru.ilezzov.moneta.lib.api.model.Response;
import ru.ilezzov.moneta.lib.api.model.Stats;
import ru.ilezzov.moneta.lib.database.repository.expense.Expense;
import ru.ilezzov.moneta.lib.database.repository.export.LastExport;
import ru.ilezzov.moneta.lib.database.repository.product.Product;
import ru.ilezzov.moneta.lib.database.repository.purchase.Purchase;
import ru.ilezzov.moneta.lib.database.repository.sale.Sale;
import ru.ilezzov.moneta.lib.enums.MonthEnum;
import ru.ilezzov.moneta.lib.utils.DateUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static ru.ilezzov.moneta.lib.core.sheets.Formulas.*;
import static ru.ilezzov.moneta.lib.core.sheets.Formulas.AVG_MARGIN;
import static ru.ilezzov.moneta.lib.core.sheets.Formulas.ROI;
import static ru.ilezzov.moneta.lib.core.sheets.Formulas.SALES_COUNT;
import static ru.ilezzov.moneta.lib.core.sheets.Formulas.TOTAL_PROFIT;

public class CoreSheetExporter implements SheetExporter {
    private final FinanceManager financeManager;
    private final Sheets service;
    private final String spreadSheetId;

    private final String financeSheet;
    private final String salesSheet;
    private final String expenseSheet;
    private final String purchaseSheet;
    private final String productSheet;

    public CoreSheetExporter(final FinanceManager financeManager, final Sheets service, final String spreadSheetId) {
        this.financeManager = financeManager;
        this.service = service;
        this.spreadSheetId = spreadSheetId;
        this.financeSheet = "Финансы";
        this.salesSheet = "Продажи";
        this.expenseSheet = "Расходы";
        this.purchaseSheet = "Закупки";
        this.productSheet = "Товары";
    }

    public CoreSheetExporter(final FinanceManager financeManager, final Sheets service, final String spreadSheetId, final String financeSheet, final String salesSheet, final String expenseSheet, final String purchaseSheet, final String productSheet) {
        this.financeManager = financeManager;
        this.service = service;
        this.spreadSheetId = spreadSheetId;
        this.financeSheet = financeSheet;
        this.salesSheet = salesSheet;
        this.expenseSheet = expenseSheet;
        this.purchaseSheet = purchaseSheet;
        this.productSheet = productSheet;
    }

    @Override
    public CompletableFuture<Response> exportData(final boolean addToCache) {
        return CompletableFuture.runAsync(() -> clearSheet(financeSheet, productSheet))
                .thenCompose(v -> {
                    final LastExport export = this.financeManager.getLastExport();

                    final CompletableFuture<List<Expense>> expensesFuture =
                            this.financeManager.getAllExpenses(addToCache, export.getLastExportDate());
                    final CompletableFuture<List<Purchase>> purchasesFuture =
                            this.financeManager.getAllPurchases(addToCache, export.getLastExportDate());
                    final CompletableFuture<List<Product>> productsFuture =
                            this.financeManager.getAllProducts(addToCache);
                    final CompletableFuture<List<Sale>> salesFuture =
                            this.financeManager.getAllSales(addToCache, export.getLastExportDate());
                    final CompletableFuture<List<Stats>> statsFuture =
                            this.financeManager.getYearlyStats();

                    return CompletableFuture.allOf(
                            expensesFuture, purchasesFuture, productsFuture, salesFuture, statsFuture
                    ).thenCompose(v2 -> {
                        final List<Expense> expenses = expensesFuture.join();
                        final List<Purchase> purchases = purchasesFuture.join();
                        final List<Product> products = productsFuture.join();
                        final List<Sale> sales = salesFuture.join();
                        final List<Stats> stats = statsFuture.join();

                        final CompletableFuture<Void> expensesExport = CompletableFuture.runAsync(() ->
                            appendData(expenses, expenseSheet, e -> List.of(
                                    DateUtil.formatDate(e.getDate()),
                                    e.getCategory().getCategory(),
                                    e.getDescription(),
                                    e.getAmount(),
                                    e.getComment()
                            ))
                        );

                        final CompletableFuture<Void> purchasesExport = CompletableFuture.runAsync(() ->
                            appendData(purchases, purchaseSheet, p -> List.of(
                                    DateUtil.formatDate(p.getDate()),
                                    p.getProductName(),
                                    p.getCostPrice(),
                                    p.getQuantity(),
                                    p.getTotalPrice(),
                                    p.getSupplier(),
                                    p.getComment()
                            ))
                        );

                        final CompletableFuture<Void> salesExport = CompletableFuture.runAsync(() ->
                            appendData(sales, salesSheet, s -> List.of(
                                    DateUtil.formatDate(s.getSaleDate()),
                                    s.getProductName(),
                                    s.getQuantity(),
                                    s.getUnitPrice(),
                                    s.getTotalPrice(),
                                    s.getCostPrice(),
                                    s.getMargin(),
                                    s.getMarketplace().getMarketplace(),
                                    s.getComment()
                            ))
                        );

                        final CompletableFuture<Void> productsExport = CompletableFuture.runAsync(() ->
                            appendData(products, productSheet, p -> List.of(
                                    p.getId(),
                                    p.getName(),
                                    p.getCategory().getCategory(),
                                    p.getCostPrice(),
                                    p.getRetailPrice(),
                                    p.getUnit().getUnit(),
                                    p.getSupplier(),
                                    p.getStock(),
                                    p.getMinimum(),
                                    p.getStatus().getStatus()
                            ))
                        );

                        final CompletableFuture<Void> statsExport = CompletableFuture.runAsync(() ->
                            appendData(stats, financeSheet, s -> List.of(
                                    MonthEnum.toString(s.month().getMonth()) + " " + s.year(),
                                    s.turnover(),
                                    s.revenue(),
                                    s.expenses(),
                                    s.profit(),
                                    s.roi(),
                                    s.avgMargin(),
                                    s.salesCount()
                            ))
                        );

                        return CompletableFuture.allOf(
                                expensesExport,
                                purchasesExport,
                                productsExport,
                                salesExport,
                                statsExport
                        ).thenRunAsync(this::appendResultFinance);
                    }).thenRunAsync(() -> {
                        this.financeManager.updateLastExport(LocalDateTime.now());
                    }).thenApply(v3 ->
                        Response.ok("Данные экспортированы в таблицу. " +
                                "Посмотреть — https://docs.google.com/spreadsheets/d/" + spreadSheetId)
                    );
                });
    }

    private <T> void appendData(List<T> items, String sheetName, java.util.function.Function<T, List<Object>> mapper) {
        if (items == null || items.isEmpty()) return;

        final List<List<Object>> values = new ArrayList<>();
        for (T item : items) {
            values.add(mapper.apply(item));
        }
        appendRow(sheetName, values);
    }

   private void appendResultFinance() {
       final List<List<Object>> values = new ArrayList<>();
       values.add(List.of("Итого за год: ", TOTAL_TURNOVER, TOTAL_REVENUE, TOTAL_EXPENSE, TOTAL_PROFIT, ROI, AVG_MARGIN, SALES_COUNT));
       appendRow(financeSheet, values);
   }

    private void appendRow(final String sheetName, final List<List<Object>> values) {
        try {
            final ValueRange valueRange = new ValueRange().setValues(values);

            this.service.spreadsheets().values()
                    .append(this.spreadSheetId, sheetName.concat("!").concat("A2:Z"), valueRange)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearSheet(final String... sheets) {
        try {
            final ClearValuesRequest clearValuesRequest = new ClearValuesRequest();
            for (final String sheet : sheets) {
                service.spreadsheets().values().clear(
                        spreadSheetId, sheet + "!A2:Z", clearValuesRequest
                ).execute();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
