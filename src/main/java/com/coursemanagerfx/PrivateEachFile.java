/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

public class PrivateEachFile {

    private static final String ROOT = "D:\\Projects\\IntelliJ_IDEA\\CourseManagerFX\\src\\main\\java\\com\\coursemanagerfx";
    private static final String HEADER_MARKER = "THIS FILE CREATED FOR \"CourseManagerFX\"";
    private static final String HEADER = String.join(System.lineSeparator(),
            "/*",
            "========================================",
            "THIS FILE CREATED FOR \"CourseManagerFX\"",
            "            Author: Nazuha26",
            "========================================",
            "*/",
            "",
            ""
    );

    public static void main(String[] args) throws IOException {
        int[] modifiedCount = {0};

        try (Stream<Path> paths = Files.walk(Paths.get(ROOT))) {
            paths.filter(p -> p.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            List<String> lines = Files.readAllLines(path);
                            if (lines.stream().anyMatch(line -> line.contains(HEADER_MARKER))) {
                                System.out.println("Skipped (already has header): " + path);
                            } else {
                                System.out.println("Processing: " + path);
                                Path tempFile = Files.createTempFile("header_", ".java");
                                try (BufferedWriter writer = Files.newBufferedWriter(tempFile);
                                     BufferedReader reader = Files.newBufferedReader(path)) {

                                    writer.write(HEADER);
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        writer.write(line);
                                        writer.newLine();
                                    }
                                }
                                Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
                                modifiedCount[0]++;
                            }
                        } catch (IOException e) {
                            System.err.println("Error processing file: " + path);
                            e.printStackTrace();
                        }
                    });
        }

        System.out.println();
        System.out.println("Done. Modified " + modifiedCount[0] + " .java file(s).");
    }
}
