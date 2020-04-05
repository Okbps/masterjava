package ru.javaops.masterjava.upload;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static ru.javaops.masterjava.common.web.ThymeleafListener.engine;

@WebServlet(urlPatterns = "/", loadOnStartup = 1)
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10) //10 MB in memory limit
@Slf4j
public class UploadServlet extends HttpServlet {
    private static final int CHUNK_SIZE = 2000;

    private final PayloadProcessor payloadProcessor = new PayloadProcessor();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        out(req, resp, "", CHUNK_SIZE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message;
        int chunkSize = CHUNK_SIZE;
        try {
            chunkSize = Integer.parseInt(req.getParameter("chunkSize"));
            if (chunkSize < 1) {
                message = "Chunk Size must be > 1";
            } else {
                Part filePart = req.getPart("fileToUpload");
                try (InputStream is1 = filePart.getInputStream(); InputStream is2 = filePart.getInputStream()) {
                    List<PayloadProcessor.FailedEmails> failedUsers = payloadProcessor.processCitiesUsers(is1, chunkSize);
                    List<PayloadProcessor.FailedEmails> failedProjects = payloadProcessor.processProjectsGroups(is2, chunkSize);
                    log.info("Failed users: " + failedUsers);
                    final WebContext webContext =
                            new WebContext(req, resp, req.getServletContext(), req.getLocale(),
                                    ImmutableMap.of("users", failedUsers));
                    engine.process("result", webContext, resp.getWriter());
                    return;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            message = e.toString();
        }
        out(req, resp, message, chunkSize);
    }

    private void out(HttpServletRequest req, HttpServletResponse resp, String message, int chunkSize) throws IOException {
        resp.setCharacterEncoding("utf-8");
        final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale(),
                ImmutableMap.of("message", message, "chunkSize", chunkSize));
        engine.process("upload", webContext, resp.getWriter());
    }
}
