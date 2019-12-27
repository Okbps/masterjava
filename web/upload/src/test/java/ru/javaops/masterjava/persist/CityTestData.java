package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.CityDao;
import ru.javaops.masterjava.persist.model.City;

import java.util.List;

public class CityTestData {
    public static City KIV = new City("kiv", "Киев");
    public static City MNSK = new City("mnsk", "Минск");
    public static City MOW = new City("mow", "Москва");
    public static City SPB = new City("spb", "Санкт-Петербург");
    public static List<City> FIRST3_CITITES = ImmutableList.of(KIV, MNSK, MOW);

    public static void init() {
    }

    public static void clean() {
        DBIProvider.getDBI().useHandle(h -> h.execute("TRUNCATE TABLE cities CASCADE;"));
    }

    public static void setUp() {
        CityTestData.clean();
        CityDao dao = DBIProvider.getDao(CityDao.class);
        DBIProvider.getDBI().useTransaction((conn, status) -> {
            FIRST3_CITITES.forEach(dao::insert);
            dao.insert(SPB);
        });
    }
}
