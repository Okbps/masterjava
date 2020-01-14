package ru.javaops.masterjava.service.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.*;
import ru.javaops.masterjava.persist.model.RefEntity;
import ru.javaops.masterjava.service.model.type.MailResult;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MailDescriptor extends RefEntity {
    private @NonNull String subject;
    @Column("to_addresses")
    private @NonNull String toAddresses;
    @Column("cc_addresses")
    private @NonNull String ccAddresses;
    @Column("sent_date")
    private @NonNull Date sentDate;
    @Column("sent_result")
    private @NonNull MailResult sentResult;
}
