
package ru.javaops.masterjava.xml.schema;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "name",
        "type",
        "users"
})
@XmlRootElement(name = "Group", namespace = "http://javaops.ru")
public class Group {

    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "type", required = true)
    protected GroupType type;
    @XmlAttribute(name = "users", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<User> users;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public GroupType getGroupType() {
        return type;
    }

    public void setGroupType(GroupType value) {
        this.type = value;
    }

    public List<User> getUsers() {
        if (users == null) {
            users = new ArrayList<User>();
        }
        return this.users;
    }

}
