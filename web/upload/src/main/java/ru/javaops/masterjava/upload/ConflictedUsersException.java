package ru.javaops.masterjava.upload;

import ru.javaops.masterjava.persist.model.User;

public class ConflictedUsersException extends Exception {
    public ConflictedUsersException(Iterable<User> users) {
        super("Duplicated users found in database");
    }
}
