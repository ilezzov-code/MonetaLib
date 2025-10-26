package ru.ilezzov.moneta.lib.database.repository.sale;

import ru.ilezzov.moneta.lib.enums.Marketplace;
import ru.ilezzov.moneta.lib.utils.DateUtil;

import java.time.LocalDateTime;

public class Sale {
    private long id;
    private LocalDateTime saleDate;
    private long productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private double costPrice;
    private double margin;
    private Marketplace marketplace;
    private String comment;

    public Sale(long id, LocalDateTime saleDate, long productId, String productName,
                int quantity, double unitPrice, double totalPrice, double costPrice,
                double margin, Marketplace marketplace, String comment) {
        this.id = id;
        this.saleDate = saleDate;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.costPrice = costPrice;
        this.margin = margin;
        this.marketplace = marketplace;
        this.comment = comment;
    }

    public Sale(final long productId, final String productName, final int quantity, final double unitPrice, final double costPrice, final Marketplace marketplace, final String comment) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.costPrice = costPrice;
        this.marketplace = marketplace;
        this.comment = comment;
    }

    public long getId() { return id; }
    public void  setId(long id) { this.id = id; }

    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }

    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }

    public double getMargin() { return margin; }
    public void setMargin(double margin) { this.margin = margin; }

    public Marketplace getMarketplace() { return marketplace; }
    public void setMarketplace(Marketplace marketplace) { this.marketplace = marketplace; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    @Override
    public String toString() {
        return String.format(
                """
                🛒 Продажа #%d
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                📅 Дата:           %s
                🆔 ID товара:      %d
                🏷️ Название:       %s
                📦 Количество:     %d
                💵 Цена за шт.:    %.2f ₽
                💰 Общая сумма:    %.2f ₽
                💸 Себестоимость:  %.2f ₽
                📊 Маржа:          %.2f ₽
                🛍️ Маркетплейс:    %s
                💬 Комментарий:    %s
                """,
                id,
                DateUtil.formatDate(saleDate),
                productId,
                productName,
                quantity,
                unitPrice,
                totalPrice,
                costPrice,
                margin,
                marketplace.getMarketplace(),
                comment
        );
    }
}
