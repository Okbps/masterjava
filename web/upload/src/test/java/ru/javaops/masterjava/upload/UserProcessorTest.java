package ru.javaops.masterjava.upload;

import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.CommonTestData;
import ru.javaops.masterjava.persist.DBITestProvider;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class UserProcessorTest {
    private static final int CHUNK_SIZE = 10;

    static {
        DBITestProvider.initDBI();
    }

    @BeforeClass
    public static void init() {
        CommonTestData.clean();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    public void processNormal() throws IOException, JAXBException, XMLStreamException {
        final UserProcessor userProcessor = new UserProcessor();
        List<UserProcessor.FailedEmails> failed;

        try (InputStream is = Resources.getResource("payload.xml").openStream()) {
            failed = userProcessor.process(is, CHUNK_SIZE);
        }

        Assert.assertTrue(failed.isEmpty());
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    public void processInvalid() throws IOException, JAXBException, XMLStreamException {
        final UserProcessor userProcessor = new UserProcessor();
        List<UserProcessor.FailedEmails> failed;

        try (InputStream is = Resources.getResource("payload-invalid.xml").openStream()) {
            failed = userProcessor.process(is, CHUNK_SIZE);
        }

        Assert.assertEquals(1, failed.size());
    }
}
