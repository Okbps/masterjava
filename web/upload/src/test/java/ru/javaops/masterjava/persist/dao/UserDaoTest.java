package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.CityTestData;
import ru.javaops.masterjava.persist.CommonTestData;
import ru.javaops.masterjava.persist.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserDaoTest extends AbstractDaoTest<UserDao> {

    public UserDaoTest() {
        super(UserDao.class);
    }

    @BeforeClass
    public static void init() {
        CommonTestData.cleanUsers();
        CityTestData.setUp();
        CommonTestData.initUsers();
        CommonTestData.insertUsers();
    }

    @Before
    public void setUp() {
//        UserTestData.setUp();
    }

    @Test
    public void getWithLimit() {
        List<User> users = dao.getWithLimit(5);
        Assert.assertEquals(CommonTestData.FIRST5_USERS, users);
    }

    @Test
    public void insertBatch() {
        CommonTestData.cleanUsers();
        List<String> cityIds = CommonTestData.FIRST5_USERS.stream().map(u-> u.getCity().getId()).collect(Collectors.toList());
        dao.insertBatch(CommonTestData.FIRST5_USERS, cityIds,3);
        List<User> actual = dao.getWithLimit(100);
        Assert.assertEquals(5, actual.size());
    }

    @Test
    public void getSeqAndSkip() {
        int seq1 = dao.getSeqAndSkip(5);
        int seq2 = dao.getSeqAndSkip(1);
        Assert.assertEquals(5, seq2 - seq1);
    }
}