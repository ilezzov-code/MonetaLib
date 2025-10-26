package ru.ilezzov.moneta.lib.enums;

public enum MonthEnum {
    JANUARY(1), FEBRUARY(2), MARCH(3), APRIL(4), MAY(5), JUNE(6),
    JULY(7), AUGUST(8), SEPTEMBER(9), OCTOBER(10), NOVEMBER(11), DECEMBER(12), ALL(-1);

    final int month;

    MonthEnum(final int month) {
        this.month = month;
    }

    public int getMonth() {
        return month;
    }

    public static String toString(final int month) {
        return switch (month) {
            case 1 -> "Январь";
            case 2 -> "Февраль";
            case 3 -> "Март";
            case 4 -> "Апрель";
            case 5 -> "Май";
            case 6 -> "Июнь";
            case 7 -> "Июль";
            case 8 -> "Август";
            case 9 -> "Сентябрь";
            case 10 -> "Октябрь";
            case 11 -> "Ноябрь";
            case 12 -> "Декабрь";
            default -> "Весь год";
        };
    }

    public static MonthEnum parseMonth(final int month) {
        for (final MonthEnum m : MonthEnum.values()) {
            if (m.getMonth() == month) {
                return m;
            }
        }

        return null;
    }
}
