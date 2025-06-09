package com.mrkhokh.toutf8plugin;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static com.mrkhokh.toutf8plugin.ModeUtils.detectEncoding;
import static com.mrkhokh.toutf8plugin.ModeUtils.splitBytesByLines;
import static com.mrkhokh.toutf8plugin.ProcessConvert2Utf8.WINDOWS_1251;

public class EncodingConverter {

    public static void convertToWindows1251(String path) {
        String sourceEncoding = StandardCharsets.UTF_8.name();
        String targetEncoding = Charset.forName(WINDOWS_1251).name();

        process(path, sourceEncoding, targetEncoding);
    }

    public static void convertToUtf8(String path) {
        String sourceEncoding = Charset.forName(WINDOWS_1251).name();
        String targetEncoding = StandardCharsets.UTF_8.name();

        process(path, sourceEncoding, targetEncoding);
    }

    private static void process(String path, final String sourceEncoding, final String targetEncoding) {
        final String directoryPath = path != null ? path : ".";

        try {
            Files.walk(Paths.get(directoryPath))
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> processFileAndFix(p, sourceEncoding, targetEncoding));
        } catch (IOException e) {
            System.err.println("Error due to file processing: " + e.getMessage());
        }
    }

    private static void processFileAndFix(Path filepath, String sourceEncoding, String targetEncoding) {
        try {
            final byte[] fileBytes = Files.readAllBytes(filepath);
            /*String detectedFileEncoding = detectEncoding(fileBytes);
            if (targetEncoding.equalsIgnoreCase(detectedFileEncoding)) {
                return;
            }*/

            List<byte[]> lineByBytesList = splitBytesByLines(fileBytes);
            List<String> processedLines = new ArrayList<>();

            for (byte[] linesBytes : lineByBytesList) {
                String encoding = detectEncoding(linesBytes);

                if (!targetEncoding.equalsIgnoreCase(encoding)) {
                    encoding = sourceEncoding;
                }
                String processedLine = new String(linesBytes, encoding);
                processedLines.add(processedLine);
            }
            if (!processedLines.isEmpty()) {
                try (BufferedWriter writer = Files.newBufferedWriter(
                        filepath,
                        Charset.forName(targetEncoding),
                        StandardOpenOption.CREATE
                )) {
                    for (int i = 0; i < processedLines.size(); i++) {
                        String processedLine = processedLines.get(i);
                        writer.write(processedLine);

                        if (i < processedLines.size() - 1) {
                            writer.newLine();
                        } else if (fileBytes[fileBytes.length - 1] == '\n') {
                            writer.newLine();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.printf("Error due to file processing: %s: %s%n ", filepath, e.getMessage());
            e.printStackTrace();
        }
    }
}
