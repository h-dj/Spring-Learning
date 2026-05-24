package cn.reid.springbatchdemo.datagen.generator;

import cn.reid.springbatchdemo.datagen.DataGenConfig.RowContext;
import cn.reid.springbatchdemo.datagen.FieldValueGenerator;

import java.util.List;

public class ValuesGenerator implements FieldValueGenerator {
    private final List<String> values;

    public ValuesGenerator(List<String> values) {
        this.values = values;
    }

    @Override
    public String generate(RowContext ctx) {
        return values.get(ctx.getRowIndex() % values.size());
    }
}
