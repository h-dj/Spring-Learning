package cn.reid.springbatchdemo.datagen.generator;

import cn.reid.springbatchdemo.datagen.DataGenConfig.RowContext;
import cn.reid.springbatchdemo.datagen.FieldValueGenerator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateGenerator implements FieldValueGenerator {
    private static final Pattern FIELD_REF_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    private final String template;

    public TemplateGenerator(String template) {
        this.template = template;
    }

    @Override
    public String generate(RowContext ctx) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = FIELD_REF_PATTERN.matcher(template);
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            String value = ctx.getFieldValue(fieldName);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
