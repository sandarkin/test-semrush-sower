package ru.sandarkin.semrush.slave;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.sandarkin.semrush.slave.model.Project;
import ru.sandarkin.semrush.slave.model.ShortProject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
public class SlaveController {

  Map<Integer, Project> projects = Collections.synchronizedMap(new HashMap<>());

  @Value("${server.port}")
  private int port;

  private SlaveConfig config;
  private SlaveService service;

  @Autowired
  public SlaveController(SlaveConfig config, SlaveService service) {
    this.config = config;
    this.service = service;
  }

  @RequestMapping(value = "/projects/{id}", method = RequestMethod.POST)
  public ShortProject takeProject(@RequestBody ShortProject prj, @PathVariable Integer id, HttpServletResponse response) throws Exception {
    return service.takeProject(this.projects, id, prj, response);
  }

  @RequestMapping(value = "/projects", method = RequestMethod.GET)
  public Collection<Project> getProjects() {
    return this.projects.values();
  }

  @PostConstruct
  public void infoAboutModes() {
    log.debug("Sower Slave controller is started on port " + port);
    log.debug("\tBlink mode is " + config.isBlink());
    log.debug("\tSnail mode is " + config.isSnail());
  }

}
