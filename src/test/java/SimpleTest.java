import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleTest {

    private TestDataProducer testDataProducer;

    @BeforeEach
    public void initTestDataProducer() {
        testDataProducer = new TestDataProducer();
    }


    @Test
    void shouldFetchSomeData() throws IOException {
        // when
        String someString = testDataProducer.getSomeString();
        Files.write(Paths.get("shouldFetchSomeData.txt"), Collections.singleton(someString));

        //then
        assertEquals(Files.readString(Paths.get("src/test/resources/responses/cmt/patients/pd/SOME-STRING-TEST-DATA-8987.txt")),
                someString);
    }
}
