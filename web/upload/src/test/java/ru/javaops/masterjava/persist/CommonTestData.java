package ru.javaops.masterjava.persist;

import org.skife.jdbi.v2.Handle;

public class CommonTestData {
    public static void clean(){
        Handle handle = DBIProvider.getDBI().open();
        handle.execute("TRUNCATE TABLE cities, project_groups, user_groups, users, projects, groups;");
        handle.close();
    }
}
