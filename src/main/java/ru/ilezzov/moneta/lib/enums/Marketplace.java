package ru.ilezzov.moneta.lib.enums;

public enum Marketplace {
    AVITO("Avito"),
    OZON("Ozon"),
    WILDBERRIES("Wildberries"),
    PLAYEROK("Playerok");

    final String marketplace;

    Marketplace(final String marketplace) {
        this.marketplace = marketplace;
    }

    public String getMarketplace() {
        return marketplace;
    }

    public static Marketplace parseMarketPlace(final String marketplace) {
        if (marketplace == null || marketplace.isBlank()) {
            return null;
        }

        for (final Marketplace market : Marketplace.values()) {
            if (market.getMarketplace().equals(marketplace)) {
                return market;
            }
        }

        return null;
    }
}
