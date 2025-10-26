package ru.ilezzov.moneta.lib.api.model;

import ru.ilezzov.moneta.lib.enums.MonthEnum;

public record Stats(MonthEnum month, int year, double turnover, double revenue, double expenses, double profit, double roi,
                        double avgMargin, int salesCount) {
    @Override
    public String toString() {
        return String.format(
                """
                📅 %s %d
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                🔁 Оборот:         %.2f ₽
                💰 Выручка:        %.2f₽
                💸 Расходы:        %.2f₽
                📈 Прибыль:        %.2f₽
                📊 ROI:            %.2f%%
                🧮 Средняя маржа:  %.2f₽
                🛒 Продаж:         %d
                """,
                MonthEnum.toString(month.getMonth()), year, turnover, revenue, expenses, profit, roi, avgMargin, salesCount
        );
    }
}
