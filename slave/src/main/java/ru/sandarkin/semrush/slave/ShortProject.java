package ru.sandarkin.semrush.slave;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShortProject {

  private Integer version;
  private String data;

  public ShortProject() {
  }

  public ShortProject(Integer version, String data) {
    this.version = version;
    this.data = data;
  }



}
