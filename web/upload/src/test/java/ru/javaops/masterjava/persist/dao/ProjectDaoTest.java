package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.CommonTestData;
import ru.javaops.masterjava.persist.GroupTestData;
import ru.javaops.masterjava.persist.ProjectTestData;
import ru.javaops.masterjava.persist.model.Project;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.javaops.masterjava.persist.ProjectTestData.*;

public class ProjectDaoTest extends AbstractDaoTest<ProjectDao> {

    public ProjectDaoTest() {
        super(ProjectDao.class);
    }

    @Before
    public void setUp() {
        ProjectTestData.setUp();
    }

    @Test
    public void getWithLimit() {
        List<Project> users = dao.getWithLimit(2)
                .stream()
                .sorted(Comparator.comparing(Project::getId))
                .limit(3)
                .collect(Collectors.toList());

        Assert.assertEquals(FIRST2_PROJECTS, users);
    }

    @Test
    public void insertBatch() {
        CommonTestData.clean();
        dao.insertBatch(FIRST2_PROJECTS, 3);
        dao.insert(TOPJAVA);
        Assert.assertEquals(3, dao.getWithLimit(100).size());
    }
}