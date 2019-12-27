package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapper;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements ResultSetMapper<User> {
    @Override
    public User map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        EntityMapper<City> cityMapper = new EntityMapper<>(City.class);
        UserFlag flag = UserFlag.valueOf(r.getString("flag"));
        return new User(r.getInt("id"), r.getString("full_name"), r.getString("email"), cityMapper.map(index, r, ctx), flag);
    }
}
