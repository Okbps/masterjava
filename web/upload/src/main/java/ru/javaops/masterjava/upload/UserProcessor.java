package ru.javaops.masterjava.upload;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.CityDao;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.JaxbUnmarshaller;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class UserProcessor {
    private static final int NUMBER_THREADS = 4;

    private static final JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
    private static UserDao userDao = DBIProvider.getDao(UserDao.class);
    private static CityDao cityDao = DBIProvider.getDao(CityDao.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);

    @AllArgsConstructor
    public static class FailedEmails {
        public String emailsOrRange;
        public String reason;

        @Override
        public String toString() {
            return emailsOrRange + " : " + reason;
        }
    }

    /*
     * return failed users chunks
     */
    public List<FailedEmails> process(final InputStream is, int chunkSize) throws XMLStreamException, JAXBException {
        log.info("Start processing with chunkSize=" + chunkSize);

        StaxStreamProcessor processor = new StaxStreamProcessor(is);
        JaxbUnmarshaller unmarshaller = jaxbParser.createUnmarshaller();

        Map<String, Future<List<String>>> chunkCityFutures = createCityFutures(processor, unmarshaller, chunkSize);
        List<FailedEmails> failedCities = processFutures(chunkCityFutures);

        Map<String, Future<List<String>>> chunkUserFutures = createUserFutures(processor, unmarshaller, chunkSize);
        List<FailedEmails> failedUsers = processFutures(chunkUserFutures);

        return failedUsers;
    }

    private Map<String, Future<List<String>>> createUserFutures(StaxStreamProcessor processor, JaxbUnmarshaller unmarshaller, int chunkSize) throws XMLStreamException {
        Map<String, Future<List<String>>> chunkFutures = new LinkedHashMap<>();
        List<User> chunk = new ArrayList<>(chunkSize);
        int id = userDao.getSeqAndSkip(chunkSize);

        XMLStreamReader reader = processor.getReader();
        while (reader.hasNext()) {
            int event = reader.next();

            if (processor.isElementStart(event, "User")) {
                City city = new City(processor.getAttribute("city"), "");
                UserFlag flag = UserFlag.valueOf(processor.getAttribute("flag"));
                String email = processor.getAttribute("email");
                String fullName = processor.getText();

                final User user = new User(id++, fullName, email, city, flag);

                chunk.add(user);
                if (chunk.size() == chunkSize) {
                    addChunkUserFutures(chunkFutures, chunk);
                    chunk = new ArrayList<>(chunkSize);
                    id = userDao.getSeqAndSkip(chunkSize);
                }
            }

            if (processor.isElementEnd(event, "Users")) {
                break;
            }
        }

        if (!chunk.isEmpty()) {
            addChunkUserFutures(chunkFutures, chunk);
        }

        return chunkFutures;
    }

    private Map<String, Future<List<String>>> createCityFutures(StaxStreamProcessor processor, JaxbUnmarshaller unmarshaller, int chunkSize) throws XMLStreamException, JAXBException {
        Map<String, Future<List<String>>> chunkFutures = new LinkedHashMap<>();
        List<City> chunk = new ArrayList<>(chunkSize);

        XMLStreamReader reader = processor.getReader();
        while (reader.hasNext()) {
            int event = reader.next();

            if (processor.isElementStart(event, "City")) {
                ru.javaops.masterjava.xml.schema.CityType xmlCity = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.CityType.class);
                final City city = new City(xmlCity.getId(), xmlCity.getValue());
                chunk.add(city);
                if (chunk.size() == chunkSize) {
                    addChunkCityFutures(chunkFutures, chunk);
                    chunk = new ArrayList<>(chunkSize);
                }

            }
            if (processor.isElementEnd(event, "Cities")) {
                break;
            }
        }

        if (!chunk.isEmpty()) {
            addChunkCityFutures(chunkFutures, chunk);
        }
        return chunkFutures;
    }

    private List<FailedEmails> processFutures(Map<String, Future<List<String>>> chunkFutures) {
        List<FailedEmails> failed = new ArrayList<>();
        List<String> allAlreadyPresents = new ArrayList<>();
        chunkFutures.forEach((emailRange, future) -> {
            try {
                List<String> alreadyPresentsInChunk = future.get();
                log.info("{} successfully executed with already presents: {}", emailRange, alreadyPresentsInChunk);
                allAlreadyPresents.addAll(alreadyPresentsInChunk);
            } catch (InterruptedException | ExecutionException e) {
                log.error(emailRange + " failed", e);
                failed.add(new FailedEmails(emailRange, e.toString()));
            }
        });
        if (!allAlreadyPresents.isEmpty()) {
            failed.add(new FailedEmails(allAlreadyPresents.toString(), "already presents"));
        }

        return failed;
    }

    private void addChunkCityFutures(Map<String, Future<List<String>>> chunkFutures, List<City> chunk) {
        String cityRange = String.format("[%s-%s]", chunk.get(0).getId(), chunk.get(chunk.size() - 1).getId());
        Future<List<String>> future = executorService.submit(() -> cityDao.insertAndGetConflictIds(chunk));
        chunkFutures.put(cityRange, future);
        log.info("Submit chunk: " + cityRange);
    }

    private void addChunkUserFutures(Map<String, Future<List<String>>> chunkFutures, List<User> chunk) {
        String emailRange = String.format("[%s-%s]", chunk.get(0).getEmail(), chunk.get(chunk.size() - 1).getEmail());
        Future<List<String>> future = executorService.submit(() -> userDao.insertAndGetConflictEmails(chunk));
        chunkFutures.put(emailRange, future);
        log.info("Submit chunk: " + emailRange);
    }
}
