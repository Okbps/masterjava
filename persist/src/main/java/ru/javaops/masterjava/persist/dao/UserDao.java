package ru.javaops.masterjava.persist.dao;

import one.util.streamex.IntStreamEx;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.util.IntegerColumnMapper;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.model.User;

import java.util.List;
import java.util.stream.Collectors;

//@RegisterMapperFactory(EntityMapperFactory.class)
@RegisterMapper(UserMapper.class)
public abstract class UserDao implements AbstractDao {
    public User insert(User user) {
        if (user.isNew()) {
            int id = insertGeneratedId(user, user.getCity().getId());
            user.setId(id);
        } else {
            insertWitId(user, user.getCity().getId());
        }
        return user;
    }

    public int getSeqAndSkip(int step) {
        String sql = "SELECT setval('user_seq', nextval('user_seq')+" + step + ", false);";
        Handle h = DBIProvider.getDBI().open();
        int id = h.createQuery(sql)
                .map(IntegerColumnMapper.PRIMITIVE)
                .first();
        h.close();
        return id - step;
    }

    @SqlUpdate("INSERT INTO users (full_name, email, city, flag) VALUES (:fullName, :email, :cityId, CAST(:flag AS USER_FLAG)) ")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean User user, @Bind("cityId") String cityId);

    @SqlUpdate("INSERT INTO users (id, full_name, email, city, flag) VALUES (:id, :fullName, :email, :cityId, CAST(:flag AS USER_FLAG)) ")
    abstract void insertWitId(@BindBean User user, @Bind("cityId") String cityId);

    @SqlQuery("SELECT * FROM users LEFT JOIN cities ON users.city = cities.id ORDER BY full_name, email LIMIT :it")
    public abstract List<User> getWithLimit(@Bind int limit);

    //   http://stackoverflow.com/questions/13223820/postgresql-delete-all-content
    @SqlUpdate("TRUNCATE users")
    @Override
    public abstract void clean();

    //    https://habrahabr.ru/post/264281/
    @SqlBatch("INSERT INTO users (id, full_name, email, city, flag) VALUES (:id, :fullName, :email, :cityId, CAST(:flag AS USER_FLAG))" +
            "ON CONFLICT DO NOTHING")
    public abstract int[] insertBatch(@BindBean List<User> users, @Bind("cityId") List<String> cityIds, @BatchChunkSize int chunkSize);


    public List<String> insertAndGetConflictEmails(List<User> users) {
        List<String> cityIds = users.stream().map(u-> u.getCity().getId()).collect(Collectors.toList());
        int[] result = insertBatch(users, cityIds, users.size());
        return IntStreamEx.range(0, users.size())
                .filter(i -> result[i] == 0)
                .mapToObj(index -> users.get(index).getEmail())
                .toList();
    }
}
