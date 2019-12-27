package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.Project;

import java.util.List;

public class ProjectTestData {
    public static Project TOPJAVA = new Project("topjava", "Topjava");
    public static Project MASTERJAVA = new Project("masterjava", "Masterjava");
    public static Project BASEJAVA = new Project("basejava", "Basejava");
    public static List<Project> FIRST2_PROJECTS = ImmutableList.of(BASEJAVA, MASTERJAVA);

    public static void clean() {
        DBIProvider.getDBI().useHandle(h -> h.execute("TRUNCATE TABLE projects CASCADE;"));
    }

    public static void setUp() {
        ProjectDao dao = DBIProvider.getDao(ProjectDao.class);
        ProjectTestData.clean();
        DBIProvider.getDBI().useTransaction((conn, status) -> {
            FIRST2_PROJECTS.forEach(dao::insert);
            dao.insert(TOPJAVA);
        });
    }
}
