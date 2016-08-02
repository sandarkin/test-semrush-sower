package ru.sandarkin.semrush.master;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class MasterController {

  @Autowired
  private DiscoveryClient discoveryClient;
  @Autowired
  MasterService masterService;

  private Map<Integer, Project> projects = Collections.synchronizedMap(new HashMap<>());
  private Map<Integer, FailedProject> failedProjects = Collections.synchronizedMap(new HashMap<>());

  @RequestMapping(value = "/projects/{id}/data/{data:.+}", method = RequestMethod.POST)
  public ResponseEntity<Project> postProjectInfo(@PathVariable Integer id, @PathVariable String data) throws Exception {
    Project project;
    if (this.projects.get(id) == null) {
      project = new Project(id, new AtomicInteger(1), data);
      this.projects.put(id, project);
    } else {
      project = this.projects.get(id);
      project.incrementVersion();
      project.setData(data);
    }
    for (ServiceInstance slave : discoveryClient.getInstances("slave")) {
      masterService.spreadProject(slave, project, this.failedProjects);
    }
    return ResponseEntity.ok(project);
  }

  @RequestMapping(value = "/projects", method = RequestMethod.GET)
  public Collection<Project> getProjects() {
    return this.projects.values();
  }

  @RequestMapping(value = "/failed", method = RequestMethod.GET)
  public Collection<FailedProject> getFailedProjects() {
    return this.failedProjects.values();
  }



}
