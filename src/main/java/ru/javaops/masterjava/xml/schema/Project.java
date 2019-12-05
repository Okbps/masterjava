
package ru.javaops.masterjava.xml.schema;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "name",
        "description",
        "groups"
})
@XmlRootElement(name = "Project", namespace = "http://javaops.ru")
public class Project {

    @XmlAttribute(name = "name", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String name;
    @XmlAttribute(name = "description", required = true)
    protected String description;
    @XmlElement(name = "Groups", namespace = "http://javaops.ru", required = true)
    protected Project.Groups groups;

    public Project.Groups getGroups() {
        return groups;
    }

    public void setGroups(Project.Groups value) {
        this.groups = value;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "group"
    })
    public static class Groups {

        @XmlElement(name = "Group", namespace = "http://javaops.ru")
        protected List<Group> group;

        public List<Group> getGroup() {
            if (group == null) {
                group = new ArrayList<Group>();
            }
            return this.group;
        }

    }

}
