package ru.sandarkin.semrush.master;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.sandarkin.semrush.master.model.FailedProject;
import ru.sandarkin.semrush.master.model.Project;
import ru.sandarkin.semrush.master.model.ShortProject;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Service
public class MasterService {

  private RestTemplate restTemplate;
  private RetryTemplate retryTemplate;
  private RetryExecutor executor;

  @Autowired
  public MasterService(RestTemplate restTemplate, RetryTemplate retryTemplate) {
    this.restTemplate = restTemplate;
    this.retryTemplate = retryTemplate;

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    this.executor = new AsyncRetryExecutor(scheduler).
        retryOn(RestClientException.class).
        withExponentialBackoff(2000, 1.2).
        //withMaxDelay(300_000).
        withMaxDelay(10_000).
        //withMaxRetries(10).
        withUniformJitter();
  }

  //private Map<Integer, FailedProject> failedProjects;

  @HystrixCommand(groupKey = "master-service", fallbackMethod = "spreadProjectFailed")
  //@Retryable(value = MasterSpreadException.class, maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 2000, multiplier = 1.2))
  public void spreadProject(ServiceInstance slave, Project project, Map<Integer, FailedProject> failedProjects)
      throws Exception {
    //this.failedProjects = failedProjects;
    try {
      log.debug(MessageFormat.format("Try to spread project {0} to slave {1,number,#}",
          project.getId(), slave.getPort()));
      this.restTemplate.exchange(slave.getUri() + "/projects/" + project.getId(),
          HttpMethod.POST, new HttpEntity<>((ShortProject) project), ShortProject.class);
      project.setStatusCode(HttpStatus.OK.value());
      failedProjects.remove(project.getId());
      log.debug(MessageFormat.format("Spread project {0} to slave {1,number,#} success",
          project.getId(), slave.getPort()));
    } catch (HttpStatusCodeException e) {
      project.setStatusCode(e.getStatusCode().value());
      throw e;
    }
  }

  public void spreadProjectFailed(ServiceInstance slave, Project project, Map<Integer, FailedProject> failedProjects) {
    FailedProject failedProject = new FailedProject(project.getId(), project.getVersion(), project.getData(),
        slave.getPort(), project.getStatusCode());
    failedProjects.put(project.getId(), failedProject);

    log.error(MessageFormat.format("Spread project {0} to slave {1,number,#} Failed!",
        project.getId(), slave.getPort()));

    executor.doWithRetry(ctx -> {
      try {
        log.debug(MessageFormat.format("Retry to spread project {0} to slave {1,number,#}",
            project.getId(), slave.getPort()));
        this.restTemplate.exchange(slave.getUri() + "/projects/" + project.getId(),
            HttpMethod.POST, new HttpEntity<>((ShortProject) project), ShortProject.class);
        failedProjects.remove(project.getId());
        log.debug(MessageFormat.format("Spread project {0} to slave {1,number,#} success",
            project.getId(), slave.getPort()));
      } catch (HttpStatusCodeException e) {
        project.setStatusCode(e.getStatusCode().value());
        failedProjects.put(project.getId(), failedProject);
        throw e;
      } catch (RestClientException e) {
        failedProjects.put(project.getId(), failedProject);
        throw e;
      }
    });

  }

}
