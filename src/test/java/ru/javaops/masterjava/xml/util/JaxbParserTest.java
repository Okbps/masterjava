package ru.javaops.masterjava.xml.util;

import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;
import ru.javaops.masterjava.xml.schema.*;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class JaxbParserTest {
    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

    static {
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));
    }

    @Test
    public void testPayload() throws Exception {
//        JaxbParserTest.class.getResourceAsStream("/city.xml")
        Payload payload = JAXB_PARSER.unmarshal(
                Resources.getResource("payload.xml").openStream());
        String strPayload = JAXB_PARSER.marshal(payload);
        JAXB_PARSER.validate(strPayload);
        System.out.println(strPayload);
    }

    @Test
    public void testCity() throws Exception {
        JAXBElement<CityType> cityElement = JAXB_PARSER.unmarshal(
                Resources.getResource("city.xml").openStream());
        CityType city = cityElement.getValue();
        JAXBElement<CityType> cityElement2 =
                new JAXBElement<>(new QName("http://javaops.ru", "City"), CityType.class, city);
        String strCity = JAXB_PARSER.marshal(cityElement2);
        JAXB_PARSER.validate(strCity);
        System.out.println(strCity);
    }

    @Test
    public void testUsersByProject() throws Exception {
        String projectName = "masterjava";

        List<User> users = getUsersByProject(projectName);

        Assert.assertEquals(2, users.size());
        Assert.assertEquals("user02", users.get(0).getId());
        Assert.assertEquals("user04", users.get(1).getId());

        users.forEach(System.out::println);
    }

    @Test
    public void testUsersAsHtml() throws Exception {
        String projectName = "masterjava";

        StringBuilder stringBuilder = new StringBuilder("<html><header></header><body><table><tr><th>Full Name</th><th>Email</th></tr>");

        List<User> users = getUsersByProject(projectName);
        users.forEach(u -> stringBuilder.append(String.format("<tr><td>%s</td><td>%s</td></tr>", u.getFullName(), u.getEmail())));

        stringBuilder.append("</table></body></html>");

        System.out.println(stringBuilder);
    }

    private List<User> getUsersByProject(String projectName) throws IOException, JAXBException {
        Payload payload = JAXB_PARSER.unmarshal(
                Resources.getResource("payload.xml").openStream());

        return payload.getProjects().getProject().stream()
                .filter(x -> projectName.equals(x.getName()))
                .map(x -> x.getGroups().getGroup())
                .flatMap(Collection::stream)
                .map(Group::getUsers)
                .flatMap(Collection::stream)
                .distinct()
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toList());
    }
}