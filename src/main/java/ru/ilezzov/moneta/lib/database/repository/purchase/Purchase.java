package ru.ilezzov.moneta.lib.database.repository.purchase;

import ru.ilezzov.moneta.lib.utils.DateUtil;

import java.time.LocalDateTime;

public class Purchase {
    private long id;
    private LocalDateTime date;
    private long productId;
    private String productName;
    private double costPrice;
    private int quantity;
    private double totalPrice;
    private String supplier;
    private String comment;

    public Purchase(long id, LocalDateTime date, long productId, String productName, double costPrice,
                    int quantity, double totalPrice, String supplier, String comment) {
        this.id = id;
        this.date = date;
        this.productId = productId;
        this.productName = productName;
        this.costPrice = costPrice;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.supplier = supplier;
        this.comment = comment;
    }

    public Purchase(final long productId, final String productName, final double costPrice, final int quantity, final String supplier, final String comment) {
        this.productId = productId;
        this.productName = productName;
        this.costPrice = costPrice;
        this.quantity = quantity;
        this.supplier = supplier;
        this.comment = comment;
    }

    public long getId() { return id; }

    public void setId(final long id) {
        this.id = id;
    }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
        recalculateTotalPrice();
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        recalculateTotalPrice();
    }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    private void recalculateTotalPrice() {
        this.totalPrice = this.costPrice * this.quantity;
    }

    @Override
    public String toString() {
        return String.format(
                """
                🛒 Закупка #%d
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                📅 Дата:           %s
                🆔 ID товара:      %d
                🏷️ Название:       %s
                💵 Себестоимость:  %.2f ₽
                📦 Количество:     %d шт.
                💰 Общая сумма:    %.2f ₽
                🏭 Поставщик:      %s
                💬 Комментарий:    %s
                """,
                id,
                DateUtil.formatDate(date),
                productId,
                productName,
                costPrice,
                quantity,
                totalPrice,
                supplier,
                comment
        );
    }
}
