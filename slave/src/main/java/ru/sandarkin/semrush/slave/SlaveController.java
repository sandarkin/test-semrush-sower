package ru.sandarkin.semrush.slave;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SlaveController {

  Map<Integer, Project> projects = Collections.synchronizedMap(new HashMap<>());

  @RequestMapping(value = "/projects/{id}", method = RequestMethod.POST)
  public ShortProject takeProject(@RequestBody ShortProject prj, @PathVariable Integer id) throws Exception {
    if (Math.random() > 0.2) {
      throw new Exception();
    }
    Project project = new Project(id, prj.getVersion(), prj.getData());
    this.projects.put(id, project);
    return prj;
  }

  @RequestMapping(value = "/projects", method = RequestMethod.GET)
  public Collection<Project> getProjects() {
    return this.projects.values();
  }

}
