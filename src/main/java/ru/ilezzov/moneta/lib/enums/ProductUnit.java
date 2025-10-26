package ru.ilezzov.moneta.lib.enums;

public enum ProductUnit {
    PIECE_BY_PIECE("По штучно");

    final String unit;
    ProductUnit(final String piece) {
        this.unit = piece;
    }

    public String getUnit() {
        return unit;
    }

    public static ProductUnit parseUnit(final String unit) {
        if  (unit == null || unit.isBlank()) {
            return null;
        }

        for (final ProductUnit productUnit : ProductUnit.values()) {
            if (productUnit.getUnit().equals(unit)) {
                return productUnit;
            }
        }

        return null;
    }
}
