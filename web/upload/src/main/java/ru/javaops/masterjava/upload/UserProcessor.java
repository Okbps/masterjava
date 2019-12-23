package ru.javaops.masterjava.upload;

import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.JaxbUnmarshaller;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class UserProcessor {
    private static final JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
    private StaxStreamProcessor processor;

    public static UserProcessor ofInputStream(InputStream is) {
        UserProcessor userProcessor = new UserProcessor();
        try {
            userProcessor.processor = new StaxStreamProcessor(is);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        return userProcessor;
    }

    public List<User> process(int chunkSize) throws XMLStreamException, JAXBException {
        List<User> users = new ArrayList<>();
        JaxbUnmarshaller unmarshaller = jaxbParser.createUnmarshaller();

        for (int i = 0; i < chunkSize && processor.doUntil(XMLEvent.START_ELEMENT, "User"); i++) {
            ru.javaops.masterjava.xml.schema.User xmlUser = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.User.class);
            final User user = new User(xmlUser.getValue(), xmlUser.getEmail(), UserFlag.valueOf(xmlUser.getFlag().value()));
            users.add(user);
        }
        return users;
    }

    public static List<User> insertUsersConcurrent(InputStream is, int xmlChunkSize) throws JAXBException, XMLStreamException, InterruptedException, ExecutionException {
        List<User> conflicted = new ArrayList<>();
        int nCallables = 0;
        UserProcessor processor = UserProcessor.ofInputStream(is);
        ExecutorService executor = Executors.newFixedThreadPool(Constants.N_THREADS);
        CompletionService<InsertUsersCallable.Result> service = new ExecutorCompletionService<>(executor);

        while (true) {
            List<User> users = processor.process(xmlChunkSize);
            if(users.isEmpty()){
                break;
            }

            nCallables++;

            service.submit(new InsertUsersCallable(users));
        }

        for (int i = 0; i < nCallables; i++) {
            InsertUsersCallable.Result result = service.take().get();
            conflicted.addAll(result.conflicted);
        }

        executor.shutdown();

        return conflicted;
    }
}
