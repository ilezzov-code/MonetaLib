package ru.ilezzov.moneta.lib.enums;

public enum ProductStatus {
    ACTIVE("Активен"),
    ARCHIVE("В архиве"),
    SOLD("Распродан");

    final String status;

    ProductStatus(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static ProductStatus parseStatus(final String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        for (final ProductStatus productStatus : ProductStatus.values()) {
            if (productStatus.getStatus().equals(status)) {
                return productStatus;
            }
        }

        return null;
    }
}
