package ru.javaops.masterjava.upload;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.Project;
import ru.javaops.masterjava.persist.model.type.GroupType;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
public class ProjectProcessor {
    private static final ProjectDao projectDao = DBIProvider.getDao(ProjectDao.class);
    private static final GroupDao groupDao = DBIProvider.getDao(GroupDao.class);

    public Map<String, Project> process(StaxStreamProcessor processor) throws XMLStreamException {
        val storedProjects = projectDao.getAsMap();
        val storedGroups = groupDao.getAsMap();
        val newGroups = new ArrayList<Group>();

        while (processor.startElement("Project", "Projects")) {
            val projectName = processor.getAttribute("name");
            int projectId;

            if (storedProjects.containsKey(projectName)) {
                projectId = storedProjects.get(projectName).getId();
            } else {
                projectId = projectDao.insertGeneratedId(new Project(projectName, processor.getElementValue("description")));
            }

            while (processor.startElement("Group", "Project")) {
                val groupName = processor.getAttribute("name");

                if (storedGroups.containsKey(groupName)) {

                } else {
                    val type = GroupType.valueOf(processor.getAttribute("type"));
                    newGroups.add(new Group(processor.getAttribute("name"), type, projectId));
                }
            }

            if (!newGroups.isEmpty()) {
                int id = groupDao.getSeqAndSkip(newGroups.size());
                for (Group newGroup : newGroups) {
                    newGroup.setId(id++);
                }

                groupDao.insertBatchWithId(newGroups, newGroups.size());
            }
        }

        return projectDao.getAsMap();
    }
}
