import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
  private static final Path CUSTOM_DIRECTORY_PATH = Paths.get(
      "your-custom-path");
  private static final String PATH_TO_TEST_CLASS = "your-path-to-test-class";
  private static final String CLASS_AS_STRING_NAME = "classAsString.txt";

  public static void main(String[] args) throws IOException {
    Path pathToTestClass = Paths.get(PATH_TO_TEST_CLASS);

    String classAsString = Files.readAllLines(pathToTestClass).stream()
        .map(String::trim)
        .map(string -> string.replace(" ", ""))
        .reduce((prev, curr) -> prev + "\n" + curr)
        .orElseThrow(
            () -> new RuntimeException("The file is empty! Path to file " + pathToTestClass));

    Files.write(Paths.get(CLASS_AS_STRING_NAME), Collections.singleton(classAsString));

    Pattern findTestRegex = Pattern.compile("@Test\\n.+(?<=\\{)[^}]+(?=})");
    Matcher findTestMatcher = findTestRegex.matcher(classAsString);

    List<String> listOfTests = findTestMatcher.results()
        .map(MatchResult::group)
        .collect(Collectors.toList());

    Pattern findNewResponseFileRegex = Pattern.compile("[a-z][A-Za-z0-9]+\\.json|txt");
    Pattern findOldResponseFileRegex = Pattern.compile("[A-Z0-9-_]+\\.json|txt");
    Map<String, String> newToOldResponseMap = new HashMap<>();
    for (String test : listOfTests) {
      Matcher findNewResponseFileMatcher = findNewResponseFileRegex.matcher(test);
      Matcher findOldResponseFileMatcher = findOldResponseFileRegex.matcher(test);

      String newResponseFile = extractFileNameFromMatcherResults(
          findNewResponseFileMatcher.results());
      String oldResponseFile = extractFileNameFromMatcherResults(
          findOldResponseFileMatcher.results());

      if (newResponseFile != null && oldResponseFile != null) {
        newToOldResponseMap.put(newResponseFile, oldResponseFile);
      }
    }

    newToOldResponseMap.forEach((k, v) -> {
      try {
        // You can use findFileByNameByCustomDirectory method instead of findFileByName
        // if you want to specify concrete path where to find
        // (Don't forget to change CUSTOM_DIRECTORY_PATH constant value ;) )
        Path oldFilePath = findFileByName(v);
        String newFileData = Files.readString(findFileByName(k)).trim();

        Files.write(oldFilePath, Collections.singleton(newFileData));
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    Files.delete(Path.of(CLASS_AS_STRING_NAME));
    newToOldResponseMap.forEach((k,v) -> {
      try {
        Files.delete(findFileByName(k));
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  private static Path findFileByName(String fileName) throws IOException {
    return Files.walk(CURRENT_DIRECTORY_PATH)
        .filter(file -> file.getFileName().endsWith(fileName))
        .findFirst()
        .orElseThrow(
            () -> new FileNotFoundException("File with name " + fileName + " was not found!"));
  }

  private static Path findFileByNameByCustomDirectory(String fileName)
      throws IOException {
    return Files.walk(CUSTOM_DIRECTORY_PATH)
        .filter(file -> file.getFileName().endsWith(fileName))
        .findFirst()
        .orElseThrow(
            () -> new FileNotFoundException("File with name " + fileName + " was not found!"));
  }

  private static String extractFileNameFromMatcherResults(Stream<MatchResult> results) {
    return results.map(MatchResult::group)
        .findFirst()
        .orElse(null);
  }
}
