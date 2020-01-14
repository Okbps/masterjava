package ru.javaops.masterjava.service.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * gkislin
 * 15.11.2016
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Addressee {
    private String email;
    private String name;

    public static String addresseesToSting(Collection<Addressee> addressees){
        return addressees.stream().map(Addressee::getEmail).reduce((s1, s2) -> s1+","+s2).orElse("");
    }
}
