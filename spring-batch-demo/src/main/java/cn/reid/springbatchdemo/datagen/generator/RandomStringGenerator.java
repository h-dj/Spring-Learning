package cn.reid.springbatchdemo.datagen.generator;

import cn.reid.springbatchdemo.datagen.DataGenConfig.RowContext;
import cn.reid.springbatchdemo.datagen.FieldValueGenerator;

public class RandomStringGenerator implements FieldValueGenerator {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final int length;

    public RandomStringGenerator(int length) {
        this.length = length;
    }

    @Override
    public String generate(RowContext ctx) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(ctx.getRandom().nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
