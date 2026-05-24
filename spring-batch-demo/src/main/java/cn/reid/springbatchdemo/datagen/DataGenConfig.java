package cn.reid.springbatchdemo.datagen;

import cn.reid.springbatchdemo.datagen.generator.*;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class DataGenConfig {
    private static final String DEFAULT_DELIMITER = "|";

    private final String table;
    private final String delimiter;
    private final boolean header;
    private final int rowCount;
    private final List<FieldConfig> fields;

    public DataGenConfig(String table, String delimiter, boolean header, int rowCount, List<FieldConfig> fields) {
        this.table = table;
        this.delimiter = delimiter;
        this.header = header;
        this.rowCount = rowCount;
        this.fields = fields;
    }

    @SuppressWarnings("unchecked")
    public static DataGenConfig fromYaml(Map<String, Object> yaml) {
        String table = (String) yaml.get("table");
        String delimiter = yaml.containsKey("delimiter") ? (String) yaml.get("delimiter") : DEFAULT_DELIMITER;
        boolean header = yaml.containsKey("header") ? (Boolean) yaml.get("header") : true;
        int rowCount = yaml.containsKey("rowCount") ? (Integer) yaml.get("rowCount") : 10;

        List<Map<String, Object>> fieldMaps = (List<Map<String, Object>>) yaml.get("fields");
        List<FieldConfig> fields = new ArrayList<>();
        for (Map<String, Object> fm : fieldMaps) {
            fields.add(FieldConfig.fromMap(fm));
        }
        return new DataGenConfig(table, delimiter, header, rowCount, fields);
    }

    @Getter
    public static class FieldConfig {
        private final String name;
        private final String type;
        private final double nullProbability;
        private final FieldValueGenerator generator;

        public FieldConfig(String name, String type, double nullProbability, FieldValueGenerator generator) {
            this.name = name;
            this.type = type;
            this.nullProbability = nullProbability;
            this.generator = generator;
        }

        @SuppressWarnings("unchecked")
        public static FieldConfig fromMap(Map<String, Object> map) {
            String name = (String) map.get("name");
            String type = (String) map.get("type");
            double nullProbability = map.containsKey("null_probability")
                    ? ((Number) map.get("null_probability")).doubleValue()
                    : 0.0;

            Map<String, Object> genMap = (Map<String, Object>) map.get("generator");
            FieldValueGenerator generator = createGenerator(genMap, type);
            return new FieldConfig(name, type, nullProbability, generator);
        }

        private static FieldValueGenerator createGenerator(Map<String, Object> genMap, String fieldType) {
            if (genMap.containsKey("values")) {
                @SuppressWarnings("unchecked")
                List<String> values = (List<String>) genMap.get("values");
                return new ValuesGenerator(values);
            }
            if (genMap.containsKey("range")) {
                @SuppressWarnings("unchecked")
                List<Object> range = (List<Object>) genMap.get("range");
                return new RangeGenerator(range, fieldType);
            }
            if (genMap.containsKey("pattern")) {
                return new PatternGenerator((String) genMap.get("pattern"));
            }
            if (genMap.containsKey("template")) {
                return new TemplateGenerator((String) genMap.get("template"));
            }
            if (genMap.containsKey("fixed")) {
                return new FixedGenerator((String) genMap.get("fixed"));
            }
            if (genMap.containsKey("randomString")) {
                return new RandomStringGenerator((Integer) genMap.get("randomString"));
            }
            throw new IllegalArgumentException("Unknown generator type for field, keys: " + genMap.keySet());
        }
    }

    public static class RowContext {
        private final int rowIndex;
        private final Random random;
        private final Map<String, String> fieldValues = new LinkedHashMap<>();

        public RowContext(int rowIndex, Random random) {
            this.rowIndex = rowIndex;
            this.random = random;
        }

        public int getRowIndex() {
            return rowIndex;
        }

        public Random getRandom() {
            return random;
        }

        public void setFieldValue(String name, String value) {
            fieldValues.put(name, value);
        }

        public String getFieldValue(String name) {
            return fieldValues.getOrDefault(name, "");
        }
    }

    public String[] generateHeaderLine() {
        return fields.stream()
                .map(FieldConfig::getName)
                .collect(Collectors.toList())
                .toArray(new String[0]);
    }
}
