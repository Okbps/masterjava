package ru.javaops.masterjava.service.mail;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import ru.javaops.masterjava.config.Configs;

import java.util.List;

@Slf4j
public class MailSender {
    private final static Config db = Configs.getConfig("mail.conf","yandex");

    static void sendMail(List<Addressee> to, List<Addressee> cc, String subject, String body) {
        String mailDescription = "to '" + to + "' cc '" + cc + "' subject '" + subject + (log.isDebugEnabled() ? "\nbody=" + body : "");

        Email email = new SimpleEmail();
        email.setHostName(db.getString("host"));
        email.setSmtpPort(db.getInt("port"));
        email.setAuthenticator(new DefaultAuthenticator(db.getString("username"), db.getString("password")));
        email.setSSLOnConnect(true);
        email.setSubject(subject);

        try {
            for(Addressee addressee: to){
                email.addTo(addressee.getEmail());
            }
            email.setFrom(db.getString("fromName"));
            email.setMsg(body);
            email.send();
            log.info("Sent mail " + mailDescription);
        } catch (EmailException e) {
            log.error("Failed to send mail " + mailDescription);
            log.error(e.getMessage());
        }


    }
}
