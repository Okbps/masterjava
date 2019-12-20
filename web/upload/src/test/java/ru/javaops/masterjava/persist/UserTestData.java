package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;

import java.util.List;

public class UserTestData {
    public static User ADMIN;
    public static User DELETED;
    public static User FULL_NAME;
    public static User USER1;
    public static User USER2;
    public static User USER3;
    public static User STAN;
    public static User KYLE;
    public static User ERIC;
    public static User KENNY;
    public static User LEOPOLD;
    public static List<User> FIRST6_USERS;
    public static List<User> SECOND6_USERS;

    public static void init() {
        ADMIN = new User("Admin", "admin@javaops.ru", UserFlag.superuser);
        DELETED = new User("Deleted", "deleted@yandex.ru", UserFlag.deleted);
        FULL_NAME = new User("Full Name", "gmail@gmail.com", UserFlag.active);
        USER1 = new User("User1", "user1@gmail.com", UserFlag.active);
        USER2 = new User("User2", "user2@yandex.ru", UserFlag.active);
        USER3 = new User("User3", "user3@yandex.ru", UserFlag.active);
        FIRST6_USERS = ImmutableList.of(ADMIN, DELETED, FULL_NAME, USER1, USER2);

        STAN = new User("Stan", "stan@javaops.ru", UserFlag.superuser);
        KYLE = new User("Kyle", "kyle@yandex.ru", UserFlag.deleted);
        ERIC = new User("Eric", "eric@gmail.com", UserFlag.active);
        KENNY = new User("Kenny", "kenny@gmail.com", UserFlag.active);
        LEOPOLD = new User("Leopold", "leopold@yandex.ru", UserFlag.active);
        SECOND6_USERS = ImmutableList.of(STAN, KYLE, ERIC, KENNY, LEOPOLD, USER1);
    }

    public static void setUp() {
        UserDao dao = DBIProvider.getDao(UserDao.class);
        dao.clean();
        DBIProvider.getDBI().useTransaction((conn, status) -> {
            FIRST6_USERS.forEach(dao::insert);
            dao.insert(USER3);
        });
    }

    public static void setUpBatch() {
        UserDao dao = DBIProvider.getDao(UserDao.class);
        dao.clean();

        DBIProvider.getDBI().useTransaction((conn, status) ->
                dao.insert(FIRST6_USERS));
    }
}
