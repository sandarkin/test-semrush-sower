package ru.sandarkin.semrush.master;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class FailedProject extends ShortProject {

  private Integer id;
  private Integer slavePort;
  private Integer responseStatus;

  public FailedProject(Integer id, AtomicInteger version, String data, Integer slavePort, Integer responseStatus) {
    super(version, data);
    this.id = id;
    this.slavePort = slavePort;
    this.responseStatus = responseStatus;
  }
}
