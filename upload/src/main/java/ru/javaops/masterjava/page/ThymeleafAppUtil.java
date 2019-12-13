package ru.javaops.masterjava.page;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;

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
}
