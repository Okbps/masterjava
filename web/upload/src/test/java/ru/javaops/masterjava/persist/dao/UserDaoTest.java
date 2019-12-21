package ru.javaops.masterjava.persist.dao;

import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.UserTestData;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.upload.UserProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class UserDaoTest extends AbstractDaoTest<UserDao> {

    public UserDaoTest() {
        super(UserDao.class);
    }

    @BeforeClass
    public static void init() {
        UserTestData.init();
    }

    @Test
    public void getWithLimit() {
        UserTestData.setUp();
        List<User> users = dao.getWithLimit(5);
        Assert.assertEquals(UserTestData.FIRST5_USERS, users);
    }

    @Test
    public void insertBatchWithConflictFluent() {
        UserTestData.setUpBatch();
        List<User> users = dao.getWithLimit(5);
        users.forEach(u -> u.setId(null));
        Assert.assertEquals(UserTestData.FIRST5_USERS, users);

        List<User> newUsers = UserTestData.insertBatchNew(UserTestData.SECOND6_USERS);
        List<User> conflicted = UserDao.subtractUsersByEmail(UserTestData.SECOND6_USERS, newUsers);

        Assert.assertTrue(conflicted.contains(UserTestData.USER1));
        Assert.assertEquals(1, conflicted.size());
    }

    @Test
    public void insertBatchWithConflictFluent2() {
        UserTestData.setUpBatch();
        List<User> users = dao.getWithLimit(5);
        users.forEach(u -> u.setId(null));
        Assert.assertEquals(UserTestData.FIRST5_USERS, users);

        List<User> conflicted = UserTestData.insertBatchConflicted(UserTestData.SECOND6_USERS);

        Assert.assertTrue(conflicted.contains(UserTestData.USER1));
        Assert.assertEquals(1, conflicted.size());
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    public void insertBatchConcurrent() {
        UserDao.setBatchChunkSize(UserDao.class, 10);
        UserDao dao = DBIProvider.getDao(UserDao.class);
//        dao.clean();
        List<User> conflicted = new ArrayList<>();

        try (InputStream is = Resources.getResource("payload.xml").openStream()) {
            int count = 0;
            UserProcessor processor = UserProcessor.ofInputStream(is);
            ExecutorService executor = Executors.newFixedThreadPool(4);
            CompletionService<List<User>> service = new ExecutorCompletionService<>(executor);

            while (true) {
                List<User> users = processor.process(5);
                if(users.isEmpty()){
                    break;
                }

                count++;

                service.submit(() -> UserTestData.insertBatchConflicted(users));
            }

            for (int i = 0; i < count; i++) {
                conflicted.addAll(service.take().get());
            }

        } catch (JAXBException | XMLStreamException | IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        conflicted.forEach(System.out::println);
    }
}