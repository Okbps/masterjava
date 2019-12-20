package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.UserTestData;
import ru.javaops.masterjava.persist.model.User;

import java.util.List;

import static ru.javaops.masterjava.persist.UserTestData.FIRST6_USERS;
import static ru.javaops.masterjava.persist.UserTestData.SECOND6_USERS;

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
        Assert.assertEquals(FIRST6_USERS, users);
    }

    @Test
    public void getWithLimitBatch() {
        UserTestData.setUpBatch();
        List<User> users = dao.getWithLimit(5);
        users.forEach(u -> u.setId(null));
        Assert.assertEquals(FIRST6_USERS, users);
        List<User>newUsers = (List<User>) dao.insert(SECOND6_USERS);
        List<User>conflicted = UserDao.subtractUsersByEmail(SECOND6_USERS, newUsers);
        System.out.println(conflicted);
    }
}