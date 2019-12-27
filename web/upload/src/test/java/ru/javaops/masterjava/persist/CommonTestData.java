package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import org.skife.jdbi.v2.Handle;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;

import java.util.List;

import static ru.javaops.masterjava.persist.CityTestData.KIV;
import static ru.javaops.masterjava.persist.CityTestData.SPB;

public class CommonTestData {
    public static User ADMIN;
    public static User DELETED;
    public static User FULL_NAME;
    public static User USER1;
    public static User USER2;
    public static User USER3;
    public static List<User> FIRST5_USERS;

    public static void initUsers() {
        ADMIN = new User("Admin", "admin@javaops.ru", KIV, UserFlag.superuser);
        DELETED = new User("Deleted", "deleted@yandex.ru", KIV, UserFlag.deleted);
        FULL_NAME = new User("Full Name", "gmail@gmail.com", KIV, UserFlag.active);
        USER1 = new User("User1", "user1@gmail.com", SPB, UserFlag.active);
        USER2 = new User("User2", "user2@yandex.ru", SPB, UserFlag.active);
        USER3 = new User("User3", "user3@yandex.ru", SPB, UserFlag.active);
        FIRST5_USERS = ImmutableList.of(ADMIN, DELETED, FULL_NAME, USER1, USER2);
    }

    public static void clean() {
        DBIProvider.getDBI().useHandle(h -> h.execute("TRUNCATE TABLE cities, project_groups, user_groups, users, projects, groups;"));
    }

    public static void cleanUsers() {
        DBIProvider.getDBI().useHandle(h -> h.execute("TRUNCATE TABLE users CASCADE;"));
    }

    public static void insertUsers() {
        UserDao dao = DBIProvider.getDao(UserDao.class);
        DBIProvider.getDBI().useTransaction((conn, status) -> {
            FIRST5_USERS.forEach(dao::insert);
            dao.insert(USER3);
        });

    }
}
