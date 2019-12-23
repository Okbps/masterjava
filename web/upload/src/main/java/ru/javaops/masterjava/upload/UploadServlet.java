package ru.javaops.masterjava.upload;

import org.thymeleaf.context.WebContext;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static ru.javaops.masterjava.common.web.ThymeleafListener.engine;

@WebServlet(urlPatterns = "/", loadOnStartup = 1)
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10) //10 MB in memory limit
public class UploadServlet extends HttpServlet {

    private final UserProcessor userProcessor = new UserProcessor();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale());

        UserDao dao = DBIProvider.getDao(UserDao.class);
        List<User> users = dao.getWithLimit(Constants.USERS_DISPLAY_LIMIT);
        webContext.setVariable("users", users);

        engine.process("upload", webContext, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale());

        try {
            Part filePart = req.getPart("fileToUpload");
            int xmlChunkSize = Integer.parseInt(req.getParameter("batchChunkSize"));
            if (filePart.getSize() == 0) {
                throw new IllegalStateException("Upload file have not been selected");
            }
            try (InputStream is = filePart.getInputStream()) {
//                UserDao.setBatchChunkSize(UserDao.class, xmlChunkSize); // used for @SqlBatch only

                List<User> conflicted = UserProcessor.insertUsersConcurrent(is, xmlChunkSize);
                List<User> usersDisplayed = DBIProvider.getDao(UserDao.class).getWithLimit(Constants.USERS_DISPLAY_LIMIT);

                webContext.setVariable("users", usersDisplayed.stream().limit(Constants.USERS_DISPLAY_LIMIT).collect(Collectors.toList()));
                webContext.setVariable("conflicted", conflicted);

                engine.process("result", webContext, resp.getWriter());
            }

        } catch (ExecutionException|InterruptedException e) {
            e.printStackTrace();
        } catch (ServletException|XMLStreamException|JAXBException e) {
            processException(webContext, e, resp.getWriter());
        }
    }

    private void processException(WebContext webContext, Exception e, Writer writer){
        webContext.setVariable("exception", e);
        engine.process("exception", webContext, writer);
    }
}
