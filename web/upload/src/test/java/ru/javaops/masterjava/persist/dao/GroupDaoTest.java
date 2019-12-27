package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.CityTestData;
import ru.javaops.masterjava.persist.CommonTestData;
import ru.javaops.masterjava.persist.GroupTestData;
import ru.javaops.masterjava.persist.model.Group;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.javaops.masterjava.persist.GroupTestData.FIRST2_GROUPS;
import static ru.javaops.masterjava.persist.GroupTestData.TOPJAVA08;

public class GroupDaoTest extends AbstractDaoTest<GroupDao> {

    public GroupDaoTest() {
        super(GroupDao.class);
    }

    @BeforeClass
    public static void init() {
        GroupTestData.init();
    }

    @Before
    public void setUp() {
        GroupTestData.setUp();
    }

    @Test
    public void getWithLimit() {
        List<Group> users = dao.getWithLimit(2)
                .stream()
                .sorted(Comparator.comparing(Group::getId))
                .limit(3)
                .collect(Collectors.toList());

        Assert.assertEquals(FIRST2_GROUPS, users);
    }

    @Test
    public void insertBatch() {
        CommonTestData.clean();
        dao.insertBatch(FIRST2_GROUPS, 3);
        dao.insert(TOPJAVA08);
        Assert.assertEquals(3, dao.getWithLimit(100).size());
    }
}