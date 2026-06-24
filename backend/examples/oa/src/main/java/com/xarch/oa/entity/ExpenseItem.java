package com.xarch.oa.entity;

import java.math.BigDecimal;

/**
 * One line item on an {@link ExpenseReport}. Stored as a JSON object
 * inside {@code oa_expense_report.items} and decoded by the service.
 */
public record ExpenseItem(Long date, BigDecimal amount, String description) {
}
