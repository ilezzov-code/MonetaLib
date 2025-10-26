package ru.ilezzov.moneta.lib.enums;

public enum ProductCategory {
    DESING("Дизайн"),
    ACCESSORIES("Аксессуары"),
    FOOTWEAR("Обувь"),
    HATS("Головные уборы"),
    DECORATION("Украшения"),
    CLOTHES("Одежда");

    final String category;

    ProductCategory(final String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public static ProductCategory parseCategory(final String category) {
        if (category == null ||  category.isBlank()) {
            return null;
        }

        for (final ProductCategory productCategory : ProductCategory.values()) {
            if (productCategory.getCategory().equals(category)) {
                return productCategory;
            }
        }

        return null;
    }
}
