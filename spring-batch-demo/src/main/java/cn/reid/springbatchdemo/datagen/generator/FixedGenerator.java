package cn.reid.springbatchdemo.datagen.generator;

import cn.reid.springbatchdemo.datagen.DataGenConfig.RowContext;
import cn.reid.springbatchdemo.datagen.FieldValueGenerator;

public class FixedGenerator implements FieldValueGenerator {
    private final String value;

    public FixedGenerator(String value) {
        this.value = value;
    }

    @Override
    public String generate(RowContext ctx) {
        return value;
    }
}
