package ru.javaops.masterjava.page;

import org.thymeleaf.context.WebContext;
import ru.javaops.masterjava.xml.schema.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class WelcomeApplication {
    private WebContext getWelcomeContext(HttpServletRequest request, HttpServletResponse response){
        WebContext ctx = new WebContext(request, response, request.getServletContext(),
                request.getLocale());
        ctx.setVariable("currentDate", new Date());

        return ctx;
    }

    public void processWelcome(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        WebContext ctx = getWelcomeContext(request, response);
        ThymeleafAppUtil.getTemplateEngine(ctx.getServletContext()).process("welcome", ctx, response.getWriter());
    }

    public void processUsersXml(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Collection<Part> parts = request.getParts();
        List<User> users = new ArrayList<>();
        for(Part part : parts) {
            if(part.getContentType().equals("text/xml")){
                try {
                    users = ThymeleafAppUtil.getAlUsersStax(part.getInputStream());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        WebContext ctx = getWelcomeContext(request, response);
        ctx.setVariable("users", users);
        ThymeleafAppUtil.getTemplateEngine(ctx.getServletContext()).process("welcome", ctx, response.getWriter());
    }
}
