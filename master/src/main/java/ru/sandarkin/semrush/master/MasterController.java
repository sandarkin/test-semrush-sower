package ru.sandarkin.semrush.master;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.sandarkin.semrush.master.model.FailedProject;
import ru.sandarkin.semrush.master.model.Project;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
public class MasterController {

  @Autowired
  private DiscoveryClient discoveryClient;
  @Autowired
  MasterService masterService;

  private Map<Integer, Project> projects = Collections.synchronizedMap(new HashMap<>());
  volatile private Map<Integer, FailedProject> failedProjects = new ConcurrentHashMap<>();

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
    log.debug("Try to spread projects to slaves");
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

  @RequestMapping(value = "/monitor", method = RequestMethod.GET)
  public void monitor(HttpServletResponse response) throws IOException {
    response.sendRedirect("http://localhost:8080/hystrix/monitor?stream=http%3A%2F%2Flocalhost%3A8080%2Fturbine.stream");
  }

}
