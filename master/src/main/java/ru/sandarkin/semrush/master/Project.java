package ru.sandarkin.semrush.master;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class Project extends ShortProject {

  private Integer id;
  @JsonIgnore
  private int statusCode;

  public Project(Integer id, AtomicInteger version, String data) {
    super(version, data);
    this.id = id;
  }

}
