package ru.javaops.masterjava.persist.dao;

import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.UserTestData;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.upload.UserProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import static ru.javaops.masterjava.persist.UserTestData.*;

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

        List<User> conflicted = UserDao.insertBatchConflicted(UserTestData.SECOND6_USERS);

        Assert.assertTrue(conflicted.contains(UserTestData.USER1));
        Assert.assertEquals(1, conflicted.size());
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    public void insertBatchConcurrentPool() {
        UserDao.setBatchChunkSize(UserDao.class, DB_CHUNK_SIZE);
        List<User> conflicted = new ArrayList<>();

        try (InputStream is = Resources.getResource("payload.xml").openStream()) {
            conflicted = UserProcessor.insertUsersConcurrent(is, XML_CHUNK_SIZE);
        } catch (JAXBException | XMLStreamException | IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        conflicted.forEach(System.out::println);
    }

    @Test
    public void testThreads() {
        Semaphore lock = new Semaphore(3);
        int nThreads = 10;
        Thread[] threads = new Thread[nThreads];

        class R implements Runnable {
            Semaphore lock;
            String name;

            public R(Semaphore lock, String name) {
                this.lock = lock;
                this.name = name;
            }

            @Override
            public void run() {
                try {
                    lock.acquire();
                    System.out.println(name + " acquired lock");
                    Thread.sleep(500);
                    lock.release();
                    System.out.println(name + " released lock");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 0; i < nThreads; i++) {
            Thread t = new Thread(new R(lock, "Thread #"+i));
            t.start();
            threads[i] = t;
        }

        for (int i = 0; i < nThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}