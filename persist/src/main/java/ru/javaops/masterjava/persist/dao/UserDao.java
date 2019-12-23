package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapper;
import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.util.AnnotationUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class UserDao implements AbstractDao {
    public UserDao() {
        super();
    }

    public User insert(User user) {
        if (user.isNew()) {
            int id = insertGeneratedId(user);
            user.setId(id);
        } else {
            insertWitId(user);
        }
        return user;
    }

    public Iterable<User> insert(Iterable<User> users) {
        insertGeneratedIdBatch(users);
        return users;
    }

    @SqlBatch("insert into users (full_name, email, flag) values (:fullName, :email, CAST(:flag AS user_flag)) ON CONFLICT(email) DO NOTHING RETURNING *")
    @BatchChunkSize(1000)
    @GetGeneratedKeys
    abstract int[] insertGeneratedIdBatch(@BindBean Iterable<User> user);

    @SqlUpdate("INSERT INTO users (full_name, email, flag) VALUES (:fullName, :email, CAST(:flag AS user_flag)) ")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean User user);

    @SqlBatch("INSERT INTO users (id, full_name, email, flag) VALUES (:id, :fullName, :email, CAST(:flag AS user_flag)) ON CONFLICT(email) DO NOTHING RETURNING *")
    @BatchChunkSize(1000)
    @GetGeneratedKeys
    abstract int[] insertWitIdBatch(@BindBean Iterable<User> user);

    @SqlUpdate("INSERT INTO users (id, full_name, email, flag) VALUES (:id, :fullName, :email, CAST(:flag AS user_flag)) ")
    abstract void insertWitId(@BindBean User user);

    @SqlQuery("SELECT * FROM users ORDER BY full_name, email LIMIT :it")
    public abstract List<User> getWithLimit(@Bind int limit);

    //   http://stackoverflow.com/questions/13223820/postgresql-delete-all-content
    @SqlUpdate("TRUNCATE users")
    @Override
    public abstract void clean();

    public static <T extends Class<?>> void setBatchChunkSize(T obj, int size) {
        for (Method userDaoMethod : obj.getDeclaredMethods()) {
            if (userDaoMethod.getName().endsWith("Batch")) {
                userDaoMethod.setAccessible(true);
                Annotation annotation = userDaoMethod.getAnnotation(BatchChunkSize.class);
                if (annotation != null) {
                    AnnotationUtil.changeAnnotationValue(annotation, "value", size);
                }
            }
        }
    }

    public static List<User> subtractUsersByEmail(Collection<User> source, Iterable<User> users) {
        return source.stream()
                .filter(current -> {
                    for (User user : users) {
                        if (user.getEmail().equals(current.getEmail())) {
                            return false;
                        }
                    }
                    return true;
                }).collect(Collectors.toList());
    }

    public static String sqlValuesFrom(Iterable<User> users) {
        StringJoiner joiner = new StringJoiner(",");

        for (User user : users) {
            String str = String.format("('%s', '%s', '%s')", user.getFullName(), user.getEmail(), user.getFlag());
            joiner.add(str);
        }

        return joiner.toString();
    }

    public static String sqlTableFrom(Iterable<User> users){
        StringJoiner joiner = new StringJoiner("UNION ALL\n");

        for(User user: users){
            String str = String.format("SELECT '%s' as full_name, '%s' as email, CAST ('%s' as user_flag) as flag%n",
                    user.getFullName(), user.getEmail(), user.getFlag());
            joiner.add(str);
        }

        return "WITH u AS (\n"+joiner.toString()+")";
    }

    public static List<User> insertBatchConflicted(Iterable<User> users) {
        List<User> conflicted = new ArrayList<>();

        Handle h = DBIProvider.getDBI().open();

        String sql = UserDao.sqlTableFrom(users) +
                ", emails AS\n" +
                "(INSERT INTO public.users(full_name, email, flag)\n" +
                "SELECT full_name, email, flag FROM u\n" +
                "ON CONFLICT (email) DO NOTHING\n" +
                "RETURNING email)\n" +
                "SELECT full_name, email, flag FROM u WHERE email NOT IN (SELECT email FROM emails)";

        h.createQuery(sql)
                .map(new EntityMapper<>(User.class))
                .iterator()
                .forEachRemaining(conflicted::add);

        h.close();

        return conflicted;
    }
}
