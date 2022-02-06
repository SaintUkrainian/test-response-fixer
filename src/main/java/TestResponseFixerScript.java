import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestResponseFixerScript {

    private static final Path CURRENT_DIRECTORY_PATH = Paths.get("").toAbsolutePath();

    public static void main(String[] args) throws IOException {
        Path pathToTestClass = Paths.get("src/test/java/SimpleTest.java");

        String classAsString = Files.readAllLines(pathToTestClass).stream()
                .map(String::trim)
                .map(string -> string.replace(" ", ""))
                .reduce((prev, curr) -> prev + "\n" + curr)
                .orElseThrow(() -> new RuntimeException("The file is empty! Path to file " + pathToTestClass));

        Files.write(Paths.get("classAsString.txt"), Collections.singleton(classAsString));

        Pattern findTestRegex = Pattern.compile("@Test\\n.+[{](\\n|\\S)*[}]");
        Matcher findTestMatcher = findTestRegex.matcher(classAsString);

        List<String> listOfTests = findTestMatcher.results()
                .map(MatchResult::group)
                .collect(Collectors.toList());

        Pattern findNewResponseFileRegex = Pattern.compile("[\"][a-zA-Z0-9]+\\.\\w{3,5}[\"]");
        Pattern findOldResponseFileRegex = Pattern.compile("[A-Z0-9-]+\\.\\w{3,5}");
        Map<String, String> newToOldResponseMap = new HashMap<>();
        for (String test : listOfTests) {
            Matcher findNewResponseFileMatcher = findNewResponseFileRegex.matcher(test);
            Matcher findOldResponseFileMatcher = findOldResponseFileRegex.matcher(test);

            String newResponseFile = extractFileNameFromMatcherResults(findNewResponseFileMatcher.results());

            String oldResponseFile = extractFileNameFromMatcherResults(findOldResponseFileMatcher.results());

            newToOldResponseMap.put(newResponseFile, oldResponseFile);
        }

        System.out.println(newToOldResponseMap);

        newToOldResponseMap.forEach((k, v) -> {
            try {
                Path oldFilePath = findFileByName(v);
                String newFileData = Files.readString(findFileByName(k)).trim();
                Files.write(oldFilePath, Collections.singleton(newFileData));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private static Path findFileByName(String fileName) throws IOException {
        return Files.walk(CURRENT_DIRECTORY_PATH)
                .filter(file -> file.getFileName().endsWith(fileName))
                .findFirst()
                .orElseThrow(() -> new FileNotFoundException("File with name " + fileName + " was not found!"));
    }

    private static String extractFileNameFromMatcherResults(Stream<MatchResult> results) {
        return results.map(MatchResult::group)
                .map(fileName -> fileName.replace("\"", ""))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Response file wasn't found!"));
    }
}
