package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import one.util.streamex.IntStreamEx;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.City;

import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class CityDao implements AbstractDao {

    public City insert(City city) {
        insertWitId(city);
        return city;
    }

    @SqlUpdate("INSERT INTO cities (id, name) VALUES (:id, :name) ")
    abstract void insertWitId(@BindBean City city);

    @SqlQuery("SELECT * FROM cities ORDER BY name LIMIT :it")
    public abstract List<City> getWithLimit(@Bind int limit);

    @SqlUpdate("TRUNCATE cities")
    @Override
    public abstract void clean();

    @SqlBatch("INSERT INTO cities (id, name) VALUES (:id, :name) ON CONFLICT DO NOTHING")
    public abstract int[] insertBatch(@BindBean List<City> cities, @BatchChunkSize int chunkSize);


    public List<String> insertAndGetConflictIds(List<City> cities) {
        int[] result = insertBatch(cities, cities.size());
        return IntStreamEx.range(0, cities.size())
                .filter(i -> result[i] == 0)
                .mapToObj(index -> cities.get(index).getId())
                .toList();
    }
}
