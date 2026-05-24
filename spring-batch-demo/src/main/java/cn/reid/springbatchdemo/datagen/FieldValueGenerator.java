package cn.reid.springbatchdemo.datagen;

import cn.reid.springbatchdemo.datagen.DataGenConfig.RowContext;

public interface FieldValueGenerator {
    String generate(RowContext ctx);
}
