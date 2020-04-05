package ru.javaops.masterjava.upload;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.dao.UserGroupDao;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserGroup;
import ru.javaops.masterjava.persist.model.type.UserFlag;
import ru.javaops.masterjava.upload.PayloadProcessor.FailedEmails;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;

@Slf4j
public class UserProcessor {
    private static final int NUMBER_THREADS = 4;

    private static final JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
    private static UserDao userDao = DBIProvider.getDao(UserDao.class);
    private ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);
    private static UserGroupDao userGroupDao = DBIProvider.getDao(UserGroupDao.class);
    private static GroupDao groupDao = DBIProvider.getDao(GroupDao.class);

    class UserGroupPair {
        Set<Integer> userGroup;
        User user;

        public UserGroupPair(User user, Set<Integer> userGroup) {
            this.userGroup = userGroup;
            this.user = user;
        }
    }

    /*
     * return failed users chunks
     */
    public List<FailedEmails> process(final StaxStreamProcessor processor, Map<String, City> cities, int chunkSize) throws XMLStreamException, JAXBException {
        log.info("Start processing with chunkSize=" + chunkSize);

        Map<String, Future<List<String>>> chunkFutures = new LinkedHashMap<>();  // ordered map (emailRange -> chunk future)

        Map<String, Group> storedGroups = groupDao.getAsMap();
        int id = userDao.getSeqAndSkip(chunkSize);
        List<UserGroupPair> userChunk = new ArrayList<>(chunkSize);
        val unmarshaller = jaxbParser.createUnmarshaller();
        List<FailedEmails> failed = new ArrayList<>();

        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            String cityRef = processor.getAttribute("city");  // unmarshal doesn't get city ref
            String groupRefsRaw = processor.getAttribute("groupRefs");
            Set<String> groupRefs = new HashSet<>();

            if (groupRefsRaw != null) {
                groupRefs.addAll(Splitter.on(' ').splitToList(nullToEmpty(groupRefsRaw))); // unmarshal doesn't get groupRefs
            }

            ru.javaops.masterjava.xml.schema.User xmlUser = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.User.class);

            if (cities.get(cityRef) == null) {
                failed.add(new FailedEmails(xmlUser.getEmail(), "City '" + cityRef + "' is not present in DB"));

            } else if (Sets.intersection(groupRefs, storedGroups.keySet()).size() != groupRefs.size()) {
                failed.add(new FailedEmails(xmlUser.getEmail(), "Not all groups of '" + groupRefs + "' are present in DB"));

            } else {
                final User user = new User(id++, xmlUser.getValue(), xmlUser.getEmail(), UserFlag.valueOf(xmlUser.getFlag().value()), cityRef);
                userChunk.add(new UserGroupPair(user,
                        groupRefs.stream().map(s -> storedGroups.get(s).getId()).collect(Collectors.toSet())
                ));

                if (userChunk.size() == chunkSize) {
                    addChunkFutures(chunkFutures, userChunk);
                    userChunk = new ArrayList<>(chunkSize);
                    id = userDao.getSeqAndSkip(chunkSize);
                }
            }
        }

        if (!userChunk.isEmpty()) {
            addChunkFutures(chunkFutures, userChunk);
        }

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

    private void addChunkFutures(Map<String, Future<List<String>>> chunkFutures, List<UserGroupPair> chunk) {
        String emailRange = String.format("[%s-%s]", chunk.get(0).user.getEmail(), chunk.get(chunk.size() - 1).user.getEmail());

        Future<List<String>> future = executorService.submit(() -> {
            List<String> conflictEmails = userDao.insertAndGetConflictEmails(
                    chunk.stream().map(pair -> pair.user).collect(Collectors.toList())
            );

            if (conflictEmails.isEmpty()) {
                userGroupDao.insertBatch(
                        chunk.stream()
                                .flatMap(pair -> pair.userGroup.stream()
                                        .map(group -> new UserGroup(pair.user.getId(), group)))
                                .collect(Collectors.toList())
                );
            }
            return conflictEmails;
        });

        chunkFutures.put(emailRange, future);
        log.info("Submit chunk: " + emailRange);
    }
}
