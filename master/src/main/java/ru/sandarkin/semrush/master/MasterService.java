package ru.sandarkin.semrush.master;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class MasterService {

  @Autowired
  private RestTemplate restTemplate;

  private Map<Integer, FailedProject> failedProjects;

  @HystrixCommand
  @Retryable(value = MasterSpreadException.class, maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 2000, multiplier = 1.2))
  public void spreadProject(ServiceInstance slave, Project project, Map<Integer, FailedProject> failedProjects)
      throws Exception {
    this.failedProjects = failedProjects;
    try {
      this.restTemplate.exchange(slave.getUri() + "/projects/" + project.getId(),
          HttpMethod.POST, new HttpEntity<>((ShortProject) project), ShortProject.class);
      project.setStatusCode(HttpStatus.OK.value());
      this.failedProjects.remove(project.getId());
    } catch (HttpStatusCodeException e) {
      project.setStatusCode(e.getStatusCode().value());
      MasterSpreadException mse = new MasterSpreadException(e);
      mse.setProject(project);
      FailedProject failedProject = new FailedProject(project.getId(), project.getVersion(), project.getData(),
          slave.getPort(), project.getStatusCode());
      this.failedProjects.put(project.getId(), failedProject);
      throw mse;
    }
  }

  @Recover
  public void recover(MasterSpreadException exception) {
    if (exception.getProject().getStatusCode() == HttpStatus.OK.value()) {
      this.failedProjects.remove(exception.getProject().getId());
    }
  }

}
