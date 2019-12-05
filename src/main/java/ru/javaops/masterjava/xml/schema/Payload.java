
package ru.javaops.masterjava.xml.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "Payload", namespace = "http://javaops.ru")
public class Payload {

    @XmlElement(name = "Cities", namespace = "http://javaops.ru", required = true)
    protected Payload.Cities cities;
    @XmlElement(name = "GroupTypes", namespace = "http://javaops.ru", required = true)
    protected Payload.GroupTypes groupTypes;
    @XmlElement(name = "Projects", namespace = "http://javaops.ru", required = true)
    protected Payload.Projects projects;
    @XmlElement(name = "Users", namespace = "http://javaops.ru", required = true)
    protected Payload.Users users;

    public Payload.Cities getCities() {
        return cities;
    }

    public void setCities(Payload.Cities value) {
        this.cities = value;
    }

    public Payload.GroupTypes getGroupTypes() {
        return groupTypes;
    }

    public void setGroupTypes(Payload.GroupTypes value) {
        this.groupTypes = value;
    }

    public Payload.Projects getProjects() {
        return projects;
    }

    public void setProjects(Payload.Projects value) {
        this.projects = value;
    }

    public Payload.Users getUsers() {
        return users;
    }

    public void setUsers(Payload.Users value) {
        this.users = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "city"
    })
    public static class Cities {

        @XmlElement(name = "City", namespace = "http://javaops.ru", required = true)
        protected List<CityType> city;

        /**
         * Gets the value of the city property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the city property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCity().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link CityType }
         * 
         * 
         */
        public List<CityType> getCity() {
            if (city == null) {
                city = new ArrayList<CityType>();
            }
            return this.city;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "groupType"
    })
    public static class GroupTypes {

        @XmlElement(name = "GroupType", namespace = "http://javaops.ru", required = true)
        protected List<GroupType> groupType;

        public List<GroupType> getGroupType() {
            if (groupType == null) {
                groupType = new ArrayList<GroupType>();
            }
            return this.groupType;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "user"
    })
    public static class Users {

        @XmlElement(name = "User", namespace = "http://javaops.ru")
        protected List<User> user;

        public List<User> getUser() {
            if (user == null) {
                user = new ArrayList<User>();
            }
            return this.user;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "project"
    })
    public static class Projects {

        @XmlElement(name = "Project", namespace = "http://javaops.ru")
        protected List<Project> project;

        public List<Project> getProject() {
            if (project == null) {
                project = new ArrayList<Project>();
            }
            return this.project;
        }
    }

}
