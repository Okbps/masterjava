package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.CityDao;
import ru.javaops.masterjava.persist.model.City;

import java.util.List;

public class CityTestData {
    public static City KIV;
    public static City MNSK;
    public static City MOW;
    public static City SPB;
    public static List<City> FIRST3_CITITES;

    public static void init() {
        SPB = new City("spb", "Санкт-Петербург");
        MOW = new City("mow", "Москва");
        KIV = new City("kiv", "Киев");
        MNSK = new City("mnsk", "Минск");
        FIRST3_CITITES = ImmutableList.of(KIV, MNSK, MOW);
    }

    public static void setUp() {
        CommonTestData.clean();
        CityDao dao = DBIProvider.getDao(CityDao.class);
        DBIProvider.getDBI().useTransaction((conn, status) -> {
            FIRST3_CITITES.forEach(dao::insert);
            dao.insert(SPB);
        });
    }
}
