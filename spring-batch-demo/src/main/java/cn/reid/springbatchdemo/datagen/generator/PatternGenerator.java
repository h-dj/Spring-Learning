package cn.reid.springbatchdemo.datagen.generator;

import cn.reid.springbatchdemo.datagen.DataGenConfig.RowContext;
import cn.reid.springbatchdemo.datagen.FieldValueGenerator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternGenerator implements FieldValueGenerator {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    private static final Pattern AUTO_INCREMENT_PATTERN = Pattern.compile("auto_increment:(\\d+)d");
    private static final Pattern RANDOM_DIGIT_PATTERN = Pattern.compile("random:(\\d+)d");

    private final String pattern;
    private int autoIncrementCounter = 1;

    public PatternGenerator(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String generate(RowContext ctx) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(pattern);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = resolvePlaceholder(placeholder, ctx);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolvePlaceholder(String placeholder, RowContext ctx) {
        Matcher autoInc = AUTO_INCREMENT_PATTERN.matcher(placeholder);
        if (autoInc.matches()) {
            int digits = Integer.parseInt(autoInc.group(1));
            return String.format("%0" + digits + "d", autoIncrementCounter++);
        }

        Matcher randomDigit = RANDOM_DIGIT_PATTERN.matcher(placeholder);
        if (randomDigit.matches()) {
            int digits = Integer.parseInt(randomDigit.group(1));
            StringBuilder sb = new StringBuilder(digits);
            for (int i = 0; i < digits; i++) {
                sb.append(ctx.getRandom().nextInt(10));
            }
            return sb.toString();
        }

        throw new IllegalArgumentException("Unknown pattern placeholder: " + placeholder);
    }
}
