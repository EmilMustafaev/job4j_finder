package ru.job4j.finder;


import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.io.FileWriter;

public class Finder {

    private Path directory;
    private String pattern;
    private String searchType;
    private Path outputFile;

    public Finder(String directory, String pattern, String searchType, String outputFile) {
        this.directory = Paths.get(directory);
        this.pattern = pattern;
        this.searchType = searchType;
        this.outputFile = Paths.get(outputFile);
    }

    public void search() throws IOException {
        List<Path> result = new ArrayList<>();
        Files.walkFileTree(directory, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (matches(file.getFileName().toString())) {
                    result.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        writeResult(result);
    }

    private boolean matches(String fileName) {
        switch (searchType) {
            case "mask":
                return fileName.matches(pattern.replace("*", ".*").replace("?", "."));
            case "name":
                return fileName.equals(pattern);
            case "regex":
                return Pattern.compile(pattern).matcher(fileName).matches();
            default:
                throw new IllegalArgumentException("Unknown search type: " + searchType);
        }
    }

    private void writeResult(List<Path> result) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile.toFile())) {
            for (Path path : result) {
                writer.write(path.toString() + System.lineSeparator());
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: -d=directory -n=pattern -t=searchType -o=outputFile");
            return;
        }
        String directory = null;
        String pattern = null;
        String searchType = null;
        String outputFile = null;

        for (String arg : args) {
            if (arg.startsWith("-d=")) {
                directory = arg.substring(3);
            } else if (arg.startsWith("-n=")) {
                pattern = arg.substring(3);
            } else if (arg.startsWith("-t=")) {
                searchType = arg.substring(3);
            } else if (arg.startsWith("-o=")) {
                outputFile = arg.substring(3);
            }
        }

        if (directory == null || pattern == null || searchType == null || outputFile == null) {
            System.out.println("Usage: -d=directory -n=pattern -t=searchType -o=outputFile");
            return;
        }

        try {
            new Finder(directory, pattern, searchType, outputFile).search();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
