package ru.sandarkin.semrush.master.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@AllArgsConstructor
public class ShortProject {

  private AtomicInteger version;
  private String data;

  public ShortProject() {
  }

  public void incrementVersion() {
    this.version.incrementAndGet();
  }

}
