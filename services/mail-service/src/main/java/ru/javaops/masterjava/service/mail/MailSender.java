package ru.javaops.masterjava.service.mail;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import ru.javaops.masterjava.config.Configs;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.service.mail.dao.MailDescriptorDao;
import ru.javaops.masterjava.service.model.MailDescriptor;
import ru.javaops.masterjava.service.model.type.MailResult;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class MailSender {
    private final static Config mailConfig = Configs.getConfig("mail.conf","yandex");

    static {
        Config dbConfig = Configs.getConfig("persist.conf", "db");

        DBIProvider.init(() -> {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("PostgreSQL driver not found", e);
            }
            return DriverManager.getConnection(dbConfig.getString("url"), dbConfig.getString("user"), dbConfig.getString("password"));
        });
    }

    public static void main(String[] args) {
        List<Addressee> to = new ArrayList<>();
        to.add(new Addressee("eric@cartman.com", "Eric Cartman"));
        to.add(new Addressee("stan@marsh.com", "Stan Marsh"));

        System.out.println(Addressee.addresseesToSting(to));
    }

    static void sendMail(List<Addressee> to, List<Addressee> cc, String subject, String body) {
        MailDescriptor mailDescriptor = new MailDescriptor();
        mailDescriptor.setSubject(subject);
        mailDescriptor.setToAddresses(Addressee.addresseesToSting(to));
        mailDescriptor.setCcAddresses(Addressee.addresseesToSting(cc));
        mailDescriptor.setSentDate(new Date());

        String mailDescription = "to '" + to + "' cc '" + cc + "' subject '" + subject + (log.isDebugEnabled() ? "\nbody=" + body : "");

        Email email = new SimpleEmail();
        email.setHostName(mailConfig.getString("host"));
        email.setSmtpPort(mailConfig.getInt("port"));
        email.setAuthenticator(new DefaultAuthenticator(mailConfig.getString("username"), mailConfig.getString("password")));
        email.setSSLOnConnect(true);
        email.setSubject(subject);

        try {
            for(Addressee addressee: to){
                email.addTo(addressee.getEmail());
            }
            email.setFrom(mailConfig.getString("fromName"));
            email.setMsg(body);
            email.send();
            mailDescriptor.setSentResult(MailResult.SUCCESS);
            log.info("Sent mail " + mailDescription);
        } catch (EmailException e) {
            mailDescriptor.setSentResult(MailResult.FAILED);
            log.error("Failed to send mail " + mailDescription);
            log.error(e.getMessage());
        }

        storeResult(mailDescriptor);
    }

    static void storeResult(MailDescriptor mailDescriptor){
        MailDescriptorDao dao = DBIProvider.getDao(MailDescriptorDao.class);
        dao.insertGeneratedId(mailDescriptor);
    }
}
