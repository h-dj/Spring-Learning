package cn.reid.springbatchdemo.datagen.generator;

import cn.reid.springbatchdemo.datagen.DataGenConfig.RowContext;
import cn.reid.springbatchdemo.datagen.FieldValueGenerator;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RangeGenerator implements FieldValueGenerator {
    private final long startLong;
    private final long endLong;
    private final LocalDate startDate;
    private final long daysBetween;
    private final boolean isDate;

    public RangeGenerator(List<Object> range, String fieldType) {
        if ("INTEGER".equalsIgnoreCase(fieldType)) {
            this.isDate = false;
            this.startLong = ((Number) range.get(0)).longValue();
            this.endLong = ((Number) range.get(1)).longValue();
            this.startDate = null;
            this.daysBetween = 0;
        } else if ("DATE".equalsIgnoreCase(fieldType)) {
            this.isDate = true;
            this.startDate = LocalDate.parse((String) range.get(0));
            LocalDate endDate = LocalDate.parse((String) range.get(1));
            this.daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
            this.startLong = 0;
            this.endLong = 0;
        } else {
            throw new IllegalArgumentException("RangeGenerator only supports INTEGER or DATE types, got: " + fieldType);
        }
    }

    @Override
    public String generate(RowContext ctx) {
        if (isDate) {
            long days = ctx.getRandom().nextLong(daysBetween + 1);
            return startDate.plusDays(days).toString();
        } else {
            long value;
            if (startLong == endLong) {
                value = startLong;
            } else {
                value = startLong + ctx.getRandom().nextLong(endLong - startLong + 1);
            }
            return String.valueOf(value);
        }
    }
}
