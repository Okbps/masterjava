package ru.javaops.masterjava.page;

import org.thymeleaf.context.WebContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class WelcomeApplication {
    public void process(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        WebContext ctx = new WebContext(request, response, request.getServletContext(),
                request.getLocale());
        ctx.setVariable("currentDate", new Date());
        ThymeleafAppUtil.getTemplateEngine(ctx.getServletContext()).process("welcome", ctx, response.getWriter());
    }
}
