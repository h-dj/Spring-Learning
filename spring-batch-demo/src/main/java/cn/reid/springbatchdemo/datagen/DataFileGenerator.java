package cn.reid.springbatchdemo.datagen;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DataFileGenerator {

    private final Random random;
    private final Path outputDir;

    public DataFileGenerator(long seed, Path outputDir) {
        this.random = new Random(seed);
        this.outputDir = outputDir;
    }

    public static void main(String[] args) throws IOException {
        long seed = 42;
        for (int i = 0; i < args.length - 1; i++) {
            if ("--seed".equals(args[i])) {
                seed = Long.parseLong(args[i + 1]);
                break;
            }
        }

        Path outputDir = Paths.get("data");
        DataFileGenerator generator = new DataFileGenerator(seed, outputDir);

        System.out.println("=== Spring Batch Data File Generator ===");
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                System.out.print("\nEnter fileType (or 'q' to quit): ");
                String fileType = scanner.nextLine().trim();
                if ("q".equalsIgnoreCase(fileType)) {
                    System.out.println("Bye.");
                    break;
                }
                if (fileType.isEmpty()) {
                    continue;
                }
                generator.generate(fileType);
            }
        }
    }

    public void generate(String fileType) throws IOException {
        String configPath = "/data-config/" + fileType + ".yaml";
        System.out.println("Loading " + configPath + "...");

        InputStream is = getClass().getResourceAsStream(configPath);
        if (is == null) {
            System.out.println("Error: data-config/" + fileType + ".yaml not found on classpath.");
            return;
        }

        Yaml yaml = new Yaml();
        @SuppressWarnings("unchecked")
        Map<String, Object> yamlMap = yaml.load(is);
        DataGenConfig config = DataGenConfig.fromYaml(yamlMap);

        System.out.println("Generating " + config.getRowCount() + " rows...");

        Files.createDirectories(outputDir);
        Path outputFile = outputDir.resolve(fileType + ".dat");

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            if (config.isHeader()) {
                String[] headerFields = config.generateHeaderLine();
                writer.write(String.join(config.getDelimiter(), headerFields));
                writer.newLine();
            }

            for (int i = 0; i < config.getRowCount(); i++) {
                DataGenConfig.RowContext ctx = new DataGenConfig.RowContext(i, random);
                List<String> rowValues = new ArrayList<>();

                for (DataGenConfig.FieldConfig field : config.getFields()) {
                    String value = field.getGenerator().generate(ctx);

                    if (field.getNullProbability() > 0 && random.nextDouble() < field.getNullProbability()) {
                        value = "";
                    }

                    ctx.setFieldValue(field.getName(), value);
                    rowValues.add(value);
                }

                writer.write(String.join(config.getDelimiter(), rowValues));
                writer.newLine();
            }
        }

        int totalLines = config.getRowCount() + (config.isHeader() ? 1 : 0);
        System.out.println("Done. data/" + fileType + ".dat created (" + totalLines + " lines including header).");
    }
}
