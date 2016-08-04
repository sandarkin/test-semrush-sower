package ru.sandarkin.semrush.master;

import lombok.Getter;
import lombok.Setter;
import ru.sandarkin.semrush.master.model.Project;

@Getter
@Setter
public class MasterSpreadException extends Exception {

  private Project project;

  public MasterSpreadException(Throwable cause) {
    super(cause);
  }

}
