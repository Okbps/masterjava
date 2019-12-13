package ru.javaops.masterjava.page;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@MultipartConfig
public class WelcomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private void setHeaders(HttpServletResponse res){
        res.setContentType("text/html;charset=UTF-8");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Cache-Control", "no-cache");
        res.setDateHeader("Expires", 0);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        setHeaders(res);
        WelcomeApplication application = new WelcomeApplication();
        application.processWelcome(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        setHeaders(res);
        WelcomeApplication application = new WelcomeApplication();
        application.processUsersXml(req, res);
    }
}
