package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.UserTestData;
import ru.javaops.masterjava.persist.model.User;

import java.util.List;

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
        Assert.assertEquals(UserTestData.FIRST6_USERS, users);
    }

    @Test
    public void insertBatchWithConflict() {
        UserTestData.setUpBatch();
        List<User> users = dao.getWithLimit(5);
        users.forEach(u -> u.setId(null));
        Assert.assertEquals(UserTestData.FIRST6_USERS, users);

        List<User>newUsers = UserTestData.insertBatch(UserTestData.SECOND6_USERS);
        List<User>conflicted = UserDao.subtractUsersByEmail(UserTestData.SECOND6_USERS, newUsers);

        Assert.assertTrue(conflicted.contains(UserTestData.USER1));
        Assert.assertEquals(1, conflicted.size());
    }
}