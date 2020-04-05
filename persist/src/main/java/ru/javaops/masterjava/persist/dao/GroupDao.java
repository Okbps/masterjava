package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import one.util.streamex.StreamEx;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.model.Group;

import java.util.List;
import java.util.Map;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class GroupDao implements AbstractDao {

    @SqlUpdate("TRUNCATE groups CASCADE ")
    @Override
    public abstract void clean();

    @SqlQuery("SELECT * FROM groups ORDER BY name")
    public abstract List<Group> getAll();

    public Map<String, Group> getAsMap() {
        return StreamEx.of(getAll()).toMap(Group::getName, g -> g);
    }

    @SqlUpdate("INSERT INTO groups (name, type, project_id)  VALUES (:name, CAST(:type AS group_type), :projectId)")
    @GetGeneratedKeys
    public abstract int insertGeneratedId(@BindBean Group groups);

    public void insert(Group groups) {
        int id = insertGeneratedId(groups);
        groups.setId(id);
    }

    public int getSeqAndSkip(int step) {
        return DBIProvider.getDBI().inTransaction(TransactionIsolationLevel.SERIALIZABLE, (conn, status) ->
                {
                    long id = (long)conn.select("SELECT nextval('common_seq')").iterator().next().get("nextval");
                    conn.execute("ALTER SEQUENCE common_seq RESTART WITH " + (id + step));
                    return Math.toIntExact(id);
                }
        );
    }

    @SqlBatch("INSERT INTO groups (name, type, project_id) VALUES (:name, CAST(:type AS group_type), :projectId)")
    @GetGeneratedKeys
    public abstract int[] insertBatch(@BindBean List<Group> groups, @BatchChunkSize int chunkSize);

    @SqlBatch("INSERT INTO groups (id, name, type, project_id) VALUES (:id, :name, CAST(:type AS group_type), :projectId) ON CONFLICT DO NOTHING")
    public abstract void insertBatchWithId(@BindBean List<Group> groups, @BatchChunkSize int chunkSize);
}
