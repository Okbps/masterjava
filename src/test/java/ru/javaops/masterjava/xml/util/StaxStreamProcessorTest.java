package ru.javaops.masterjava.xml.util;

import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;
import ru.javaops.masterjava.xml.schema.User;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

public class StaxStreamProcessorTest {
    @Test
    public void readCities() throws Exception {
        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(Resources.getResource("payload.xml").openStream())) {
            XMLStreamReader reader = processor.getReader();
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    if ("City".equals(reader.getLocalName())) {
                        System.out.println(reader.getElementText());
                    }
                }
            }
        }
    }

    @Test
    public void readCities2() throws Exception {
        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(Resources.getResource("payload.xml").openStream())) {
            String city;
            while ((city = processor.getElementValue("City")) != null) {
                System.out.println(city);
            }
        }
    }

    @Test
    public void testUsersByProject() throws Exception {
        String projectName = "masterjava";
        Set<User> users = new TreeSet<>(Comparator.comparing(User::getFullName));
        Set<String> usersIds = new HashSet<>();

        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(Resources.getResource("payload.xml").openStream())) {
            XMLStreamReader reader = processor.getReader();
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLEvent.START_ELEMENT && "Project".equals(reader.getLocalName())
                        && projectName.equals(reader.getAttributeValue("", "name"))) {
                    extractGroups(reader, usersIds);
                } else if (event == XMLEvent.END_ELEMENT && "Projects".equals(reader.getLocalName())) {
                    break;
                }
            }
        }

        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(Resources.getResource("payload.xml").openStream())) {
            XMLStreamReader reader = processor.getReader();

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLEvent.START_ELEMENT && "User".equals(reader.getLocalName())
                        && usersIds.contains(reader.getAttributeValue("", "id"))) {
                    User user = new User();
                    user.setEmail(reader.getAttributeValue("", "email"));
                    user.setFullName(extractFullName(reader));
                    users.add(user);

                } else if (event == XMLEvent.END_ELEMENT && "Users".equals(reader.getLocalName())) {
                    break;
                }
            }
        }

        Assert.assertEquals(2, users.size());

        users.forEach(System.out::println);
    }

    private void extractGroups(XMLStreamReader reader, Set<String> destination) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLEvent.START_ELEMENT && "Group".equals(reader.getLocalName())) {
                String usersAttrValue = reader.getAttributeValue("", "users");
                destination.addAll(Arrays.asList(usersAttrValue.split("\\s")));
            } else if (event == XMLEvent.END_ELEMENT && "Groups".equals(reader.getLocalName())) {
                break;
            }
        }
    }

    private String extractFullName(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLEvent.START_ELEMENT && "fullName".equals(reader.getLocalName())) {
                return reader.getElementText();
            } else if (event == XMLEvent.END_ELEMENT && "fullName".equals(reader.getLocalName())) {
                break;
            }
        }
        return "";
    }
}