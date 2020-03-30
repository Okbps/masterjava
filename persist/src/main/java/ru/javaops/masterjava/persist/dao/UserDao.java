package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.IntStreamEx;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.model.User;

import java.util.List;
import java.util.concurrent.Callable;

@RegisterMapperFactory(EntityMapperFactory.class)
@Slf4j
public abstract class UserDao implements AbstractDao {

    public User insert(User user) {
        if (user.isNew()) {
            int id = insertGeneratedId(user);
            user.setId(id);
        } else {
            insertWitId(user);
        }
        return user;
    }

    @SqlQuery("SELECT nextval('user_seq')")
    abstract long getNextVal();

    //    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public int getSeqAndSkip(int step) {
        log.info("getSeqAndSkip...");
        return DBIProvider.getDBI().inTransaction(TransactionIsolationLevel.SERIALIZABLE, (conn, status) ->
                {
                    long id = (long)conn.select("SELECT nextval('user_seq')").iterator().next().get("nextval");
                    conn.execute("ALTER SEQUENCE user_seq RESTART WITH " + (id + step));
                    return Math.toIntExact(id);
                }
        );
    }

    @SqlUpdate("INSERT INTO users (full_name, email, flag, city_ref) VALUES (:fullName, :email, CAST(:flag AS USER_FLAG), :cityRef) ")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean User user);

    @SqlUpdate("INSERT INTO users (id, full_name, email, flag, city_ref) VALUES (:id, :fullName, :email, CAST(:flag AS USER_FLAG), :cityRef) ")
    abstract void insertWitId(@BindBean User user);

    @SqlQuery("SELECT * FROM users ORDER BY full_name, email LIMIT :it")
    public abstract List<User> getWithLimit(@Bind int limit);

    //   http://stackoverflow.com/questions/13223820/postgresql-delete-all-content
    @SqlUpdate("TRUNCATE users CASCADE")
    @Override
    public abstract void clean();

    //    https://habrahabr.ru/post/264281/
    @SqlBatch("INSERT INTO users (id, full_name, email, flag, city_ref) VALUES (:id, :fullName, :email, CAST(:flag AS USER_FLAG), :cityRef)" +
            "ON CONFLICT DO NOTHING")
//            "ON CONFLICT (email) DO UPDATE SET full_name=:fullName, flag=CAST(:flag AS USER_FLAG)")
    public abstract int[] insertBatch(@BindBean List<User> users, @BatchChunkSize int chunkSize);


    public List<String> insertAndGetConflictEmails(List<User> users) {
        int[] result = insertBatch(users, users.size());
        return IntStreamEx.range(0, users.size())
                .filter(i -> result[i] == 0)
                .mapToObj(index -> users.get(index).getEmail())
                .toList();
    }
}
