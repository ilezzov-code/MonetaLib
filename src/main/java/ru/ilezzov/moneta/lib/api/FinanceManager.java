package ru.ilezzov.moneta.lib.api;

import ru.ilezzov.moneta.lib.api.model.Response;
import ru.ilezzov.moneta.lib.api.model.Stats;
import ru.ilezzov.moneta.lib.database.repository.expense.Expense;
import ru.ilezzov.moneta.lib.database.repository.export.LastExport;
import ru.ilezzov.moneta.lib.database.repository.product.Product;
import ru.ilezzov.moneta.lib.database.repository.purchase.Purchase;
import ru.ilezzov.moneta.lib.database.repository.sale.Sale;
import ru.ilezzov.moneta.lib.enums.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Главный интерфейс для управления продуктами, продажами, покупками и расходами.
 * Все методы асинхронны и возвращают результат в виде {@link CompletableFuture}.
 ***/
public interface FinanceManager {
    
    /**
     * Добавляет новую продажу.
     *
     * @param productName  название проданного товара (должен существовать в products)
     * @param quantity     количество проданного товара
     * @param marketplace  площадка, на которой произошла продажа
     * @return {@link CompletableFuture} с результатом выполнения операции
     **/
    CompletableFuture<Response> addSale(final String productName, final int quantity, final Marketplace marketplace);
    
    /**
     * Добавляет новую продажу с комментарием.
     *
     * @param productName  название проданного товара (должен существовать в products)
     * @param quantity     количество проданного товара
     * @param marketplace  площадка, на которой произошла продажа
     * @param comment      комментарий к продаже
     * @return {@link CompletableFuture} с результатом выполнения операции
     **/
    CompletableFuture<Response> addSale(final String productName, final int quantity, final Marketplace marketplace, final String comment);

    /**
     * @param addToCache добавить ли полученные данные в кеш
     * @return список всех {@link Sale}
     */
    CompletableFuture<List<Sale>> getAllSales(final boolean addToCache);

    CompletableFuture<List<Sale>> getAllSales(final boolean addToCache, final LocalDateTime date);

    /**
     * Добавляет расход.
     *
     * @param category    категория расхода
     * @param description описание расхода
     * @param amount      сумма расхода
     * @return {@link CompletableFuture} с результатом выполнения операции
     **/
    CompletableFuture<Response> addExpense(final ExpenseCategory category, final String description, final double amount);
    
    /**
     * Добавляет расход с комментарием.
     *
     * @param category    категория расхода
     * @param description описание расхода
     * @param amount      сумма расхода
     * @param comment     комментарий к расходу
     * @return {@link CompletableFuture} с результатом выполнения операции
     **/
    CompletableFuture<Response> addExpense(final ExpenseCategory category, final String description, final double amount, final String comment);

    /**
     * @param addToCache добавить ли полученные данные в кеш
     * @return список всех {@link Expense}
     */
    CompletableFuture<List<Expense>> getAllExpenses(final  boolean addToCache);

    CompletableFuture<List<Expense>> getAllExpenses(final boolean addToCache, final LocalDateTime date);

    /**
     * Добавляет закупку товара.
     *
     * @param productName     название товара
     * @param quantity        количество закупленных единиц
     * @param includeInSales  учитывать ли закупку в статистике расходов
     * @return {@link CompletableFuture} с результатом выполнения операции
     **/
    CompletableFuture<Response> addPurchase(final String productName, final int quantity, final boolean includeInSales);
    
    /**
     * Добавляет закупку товара с комментарием.
     *
     * @param productName     название товара
     * @param quantity        количество закупленных единиц
     * @param includeInSales  учитывать ли закупку в статистике расходов
     * @param comment         комментарий к закупке
     * @return {@link CompletableFuture} с результатом выполнения операции
     **/
    CompletableFuture<Response> addPurchase(final String productName, final int quantity, final boolean includeInSales, final String comment);

    /**
     * @param addToCache добавить ли полученные данные в кеш
     * @return список всех {@link Purchase}
     */
    CompletableFuture<List<Purchase>> getAllPurchases(final boolean addToCache);

    CompletableFuture<List<Purchase>> getAllPurchases(final boolean addToCache, final LocalDateTime date);

    /**
     * Добавляет новый товар в каталог.
     *
     * @param name         название товара
     * @param category     категория товара
     * @param costPrice    себестоимость
     * @param retailPrice  розничная цена
     * @param unit         единица измерения (например, шт, кг)
     * @param supplier     поставщик
     * @param minimum      минимальное количество на складе для уведомления
     * @param status       текущий статус товара (например, АКТИВЕН, В АРХИВЕ)
     * @return {@link CompletableFuture} с результатом выполнения операции
     **/
    CompletableFuture<Response> addProduct(final String name, final ProductCategory category, final double costPrice, final double retailPrice, final ProductUnit unit, final String supplier, final int minimum, final ProductStatus status);

    /**
     * Возвращает информацию о товаре по его ID.
     *
     * @param id уникальный идентификатор товара
     * @return {@link CompletableFuture} с найденным {@link Product} или null, если товар не найден
     **/
    CompletableFuture<Product> getProduct(final long id);

    /**
     * Возвращает информацию о товаре по его названию.
     *
     * @param productName название товара
     * @return {@link CompletableFuture} с найденным {@link Product} или null, если товар не найден
     **/
    CompletableFuture<Product> getProduct(final String productName);

    /**
     * @param addToCache добавить ли полученные данные в кеш
     * @return список {@link Product}
     */
    CompletableFuture<List<Product>> getAllProducts(final boolean addToCache);


    /**
     * Возвращает статистику за указанный месяц текущего года.
     *
     * @param month месяц, за который нужно получить статистику
     * @return {@link CompletableFuture} со статистикой {@link Stats}
     **/
    CompletableFuture<Stats> getMonthlyStats(final MonthEnum month);
    
    /**
     * Возвращает статистику за указанный месяц конкретного года.
     *
     * @param month месяц
     * @param year  год
     * @return {@link CompletableFuture} со статистикой {@link Stats}
     **/
    CompletableFuture<Stats> getMonthlyStats(final MonthEnum month, final int year);

    /**
     * Возвращает статистику за каждый месяц текущего года.
     *
     * @return {@link CompletableFuture} со списком месячных статистик
     **/
    CompletableFuture<List<Stats>> getYearlyStats();

    /**
     * Возвращает статистику за каждый месяц указанного года.
     *
     * @param year год
     * @return {@link CompletableFuture} со списком месячных статистик
     **/
    CompletableFuture<List<Stats>> getYearlyStats(final int year);

    /**
     * Возвращает статистику за указанный год.
     *
     * @param year год
     * @return {@link CompletableFuture} со сводной статистикой {@link Stats}
     **/
    CompletableFuture<Stats> getYearSummary(final int year);
    
    /**
     * Возвращает статистику по текущему году.
     *
     * @return {@link CompletableFuture} со сводной статистикой {@link Stats}
     **/
    CompletableFuture<Stats> getYearSummary();

    LastExport getLastExport();

    void updateLastExport(final LocalDateTime date);

    /**
     * Закрывает соединения и освобождает ресурсы.
     * Принудительно сохраняет кеш.
     *
     * @throws SQLException если при закрытии произошла ошибка базы данных
     **/
    void close() throws SQLException;

}
