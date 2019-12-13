package ru.javaops.masterjava.page;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.Collection;

@MultipartConfig
public class WelcomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        WelcomeApplication application = new WelcomeApplication();
        application.process(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Collection<Part> parts = req.getParts();
        for(Part part : parts) {
            System.out.println("Name:");
            System.out.println(part.getName());
            System.out.println("Header: ");
            for(String headerName : part.getHeaderNames()) {
                System.out.println(headerName);
                System.out.println(part.getHeader(headerName));
            }
            System.out.println("Size: ");
            System.out.println(part.getSize());
            part.write(part.getName() + "-down");
        }
        res.sendRedirect("/welcome");
    }
}
