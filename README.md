How to make it work properly?

Your test class should have the following look:
`

    class SomeTest {

        @Test
        void shouldFetchSomeTestData() {
          Some request setup here...
    
          MvcResult result = executeSomeRequest();
    
          // You can choose whatever method for writing response to file you like.
          // File should be named in camelCase
          Files.write(Paths.get("someName.json", getStringResult(result))
    
          // Expected response file should be named in UPPER_CASE 
          // and may contain dashes, underscores or numbers
          someCustomeAssertMethod(result, "path-to-expected-response-file/SOME_TEST-FILE.json");
        }
    }
`