package com.nelani.recipe_search_backend.util;

import com.nelani.recipe_search_backend.model.DateFilter;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateRangeUtil {

    public static LocalDateTime[] getDateRange(DateFilter filter) {
        LocalDateTime now = LocalDateTime.now();
        switch (filter) {
            case TODAY -> {
                return new LocalDateTime[] {
                        now.toLocalDate().atStartOfDay(),
                        now.toLocalDate().plusDays(1).atStartOfDay()
                };
            }
            case THIS_WEEK -> {
                LocalDate startOfWeek = now.toLocalDate().with(java.time.DayOfWeek.MONDAY);
                LocalDate endOfWeek = startOfWeek.plusDays(7);
                return new LocalDateTime[] { startOfWeek.atStartOfDay(), endOfWeek.atStartOfDay() };
            }
            case THIS_MONTH -> {
                LocalDate startOfMonth = now.toLocalDate().withDayOfMonth(1);
                LocalDate endOfMonth = startOfMonth.plusMonths(1);
                return new LocalDateTime[] { startOfMonth.atStartOfDay(), endOfMonth.atStartOfDay() };
            }
            default -> {
                return new LocalDateTime[] { null, null };
            }
        }
    }
}
