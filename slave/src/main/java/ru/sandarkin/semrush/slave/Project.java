package ru.sandarkin.semrush.slave;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project extends ShortProject {

  private Integer id;

  public Project(Integer version, String data) {
    super(version, data);
  }

  public Project(Integer id, Integer version, String data) {
    super(version, data);
    this.id = id;
  }

}
