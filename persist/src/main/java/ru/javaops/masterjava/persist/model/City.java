package ru.javaops.masterjava.persist.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.*;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class City {
    @Getter
    @Setter
    private String id;

    @Column("name")
    private @NonNull String name;
}