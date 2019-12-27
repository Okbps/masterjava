package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.GroupType;

import java.util.List;

public class GroupTestData {
    public static Group TOPJAVA06 = new Group("topjava06", GroupType.FINISHED);
    public static Group TOPJAVA07 = new Group("topjava07", GroupType.FINISHED);
    public static Group TOPJAVA08 = new Group("topjava08", GroupType.CURRENT);
    public static List<Group> FIRST2_GROUPS = ImmutableList.of(TOPJAVA06, TOPJAVA07);

    public static void init() {
    }

    public static void clean() {
        DBIProvider.getDBI().useHandle(h -> h.execute("TRUNCATE TABLE groups CASCADE;"));
    }


    public static void setUp() {
        GroupDao dao = DBIProvider.getDao(GroupDao.class);
        GroupTestData.clean();
        DBIProvider.getDBI().useTransaction((conn, status) -> {
            FIRST2_GROUPS.forEach(dao::insert);
            dao.insert(TOPJAVA08);
        });
    }
}
