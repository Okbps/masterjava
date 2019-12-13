package ru.javaops.masterjava.page;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import ru.javaops.masterjava.xml.schema.User;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ThymeleafAppUtil {
    private static TemplateEngine templateEngine;

    public static TemplateEngine getTemplateEngine(ServletContext servletContext) {
        if(templateEngine==null) {
            ServletContextTemplateResolver templateResolver =
                    new ServletContextTemplateResolver(servletContext);
            templateResolver.setTemplateMode("XHTML");
            templateResolver.setPrefix("/WEB-INF/templates/");
            templateResolver.setSuffix(".html");
            templateResolver.setCacheTTLMs(3600000L);
            templateEngine = new TemplateEngine();
            templateEngine.setTemplateResolver(templateResolver);
        }

        return templateEngine;
    }

    public static List<User> getAlUsersStax(InputStream is) throws XMLStreamException, JAXBException {
        List<User> users = new ArrayList<>();

        StaxStreamProcessor processor = new StaxStreamProcessor(is);
        JaxbParser parser = new JaxbParser(User.class);

        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            User user = parser.unmarshal(processor.getReader(), User.class);
            users.add(user);
        }

        return users;
    }

}
