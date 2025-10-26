package ru.ilezzov.moneta.lib.core.sheets;

public class Formulas {
    public static final String TOTAL_TURNOVER = "=SUM(B2:B13)";
    public static final String TOTAL_REVENUE = "=SUM(C2:C13)";
    public static final String TOTAL_EXPENSE = "=SUM(D2:D13)";
    public static final String TOTAL_PROFIT = "=SUM(E2:E13)";
    public static final String ROI = "=IF(D14=0; 0; E14/D14)";
    public static final String AVG_MARGIN = "=AVERAGE(G2:G13)";
    public static final String SALES_COUNT = "=SUM(H2:H13)";
}
