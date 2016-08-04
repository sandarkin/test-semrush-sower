package ru.sandarkin.semrush.slave;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sandarkin.semrush.slave.model.Project;
import ru.sandarkin.semrush.slave.model.ShortProject;

import java.text.MessageFormat;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service
public class SlaveService {

  private SlaveConfig config;

  @Value("${server.port}")
  private int port;

  @Autowired
  public SlaveService(SlaveConfig config) {
    this.config = config;
  }

  @HystrixCommand(groupKey = "slave-service", commandKey = "takeProject turbinePort", fallbackMethod = "takeProjectFailed")
  public ShortProject takeProject(Map<Integer, Project> projects, Integer id, ShortProject prj,
                                  HttpServletResponse response) throws Exception {

    if (config.isBlink() && Math.random() > 0.2) {
      log.debug(MessageFormat.format("Slave on port {0,number,#} throw exception", port));
      throw new Exception();
    }

    if (config.isSnail()) {
      long sleepPeriod = (long) (Math.random() * 10000);
      log.debug(MessageFormat.format("Slave on port {0,number,#} fall asleep for {1,number,#} ms", port, sleepPeriod));
      Thread.sleep(sleepPeriod);
    }

    Project project = new Project(id, prj.getVersion(), prj.getData());
    projects.put(id, project);
    return prj;
  }

  public ShortProject takeProjectFailed(Map<Integer, Project> projects, Integer id, ShortProject prj,
                                        HttpServletResponse response) throws Exception {
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    return new ShortProject(0, "Failed");
  }

}
