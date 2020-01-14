package ru.javaops.masterjava.service.mail.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.dao.AbstractDao;
import ru.javaops.masterjava.service.model.MailDescriptor;

import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class MailDescriptorDao implements AbstractDao {
    @SqlUpdate("TRUNCATE mail_descriptor CASCADE")
    @Override
    public abstract void clean();

    @SqlUpdate("INSERT INTO mail_descriptor (subject, to_addresses, cc_addresses, sent_date, sent_result) " +
            "VALUES (:subject, :toAddresses, :ccAddresses, :sentDate, CAST(:sentResult AS MAIL_RESULT)) ")
    @GetGeneratedKeys
    public abstract int insertGeneratedId(@BindBean MailDescriptor mail);

    @SqlQuery("SELECT * FROM mail_descriptor")
    public abstract List<MailDescriptor> getAll();
}
