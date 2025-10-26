package ru.ilezzov.moneta.lib.database.repository.product;

import ru.ilezzov.moneta.lib.enums.ProductCategory;
import ru.ilezzov.moneta.lib.enums.ProductStatus;
import ru.ilezzov.moneta.lib.enums.ProductUnit;

public class Product {
    private long id;
    private String name;
    private ProductCategory category;
    private double costPrice;
    private double retailPrice;
    private ProductUnit unit;
    private String supplier;
    private int stock;
    private int minimum;
    private ProductStatus status;

    public Product(long id, String name, ProductCategory category, double costPrice, double retailPrice,
                   ProductUnit unit, String supplier, int stock, int minimum, ProductStatus status) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.costPrice = costPrice;
        this.retailPrice = retailPrice;
        this.unit = unit;
        this.supplier = supplier;
        this.stock = stock;
        this.minimum = minimum;
        this.status = status;
    }

    public Product(final String name, final ProductCategory category, final double costPrice, final double retailPrice, final ProductUnit unit, final String supplier, final int stock, final int minimum, final ProductStatus status) {
        this.name = name;
        this.category = category;
        this.costPrice = costPrice;
        this.retailPrice = retailPrice;
        this.unit = unit;
        this.supplier = supplier;
        this.stock = stock;
        this.minimum = minimum;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public double getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(double retailPrice) {
        this.retailPrice = retailPrice;
    }

    public ProductUnit getUnit() {
        return unit;
    }

    public void setUnit(ProductUnit unit) {
        this.unit = unit;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void reduceStock(int stock) {
        this.stock -= stock;
    }

    public void increaseStock(int stock) {
        this.stock += stock;
    }

    public int getMinimum() {
        return minimum;
    }

    public void setMinimum(int minimum) {
        this.minimum = minimum;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format(
                """
                🧾 Товар: %s
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                🆔 ID:               %d
                🏷️ Категория:        %s
                📦 Единица:          %s
                🏭 Поставщик:        %s
                💵 Себестоимость:    %.2f ₽
                💰 Розничная цена:   %.2f ₽
                📊 Остаток:          %d шт.
                ⚠️ Мин. остаток:     %d шт.
                📌 Статус:           %s
                """,
                name,
                id,
                category.getCategory(),
                unit.getUnit(),
                supplier,
                costPrice,
                retailPrice,
                stock,
                minimum,
                status.getStatus()
        );
    }
}
