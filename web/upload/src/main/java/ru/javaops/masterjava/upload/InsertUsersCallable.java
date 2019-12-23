package ru.javaops.masterjava.upload;

import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class InsertUsersCallable implements Callable<InsertUsersCallable.Result> {
    Result result;

    public InsertUsersCallable(List<User> users) {
        this.result = new Result(users);
    }

    @Override
    public Result call() {
        result.conflicted = UserDao.insertBatchConflicted(result.input);

        return result;
    }

    public static class Result {
        List<User> input;
        List<User> conflicted;

        public Result(List<User> input) {
            this.input = input;
            this.conflicted = Collections.emptyList();
        }
    }
}
