package ru.sandarkin.semrush.slave;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix="sower")
public class SlaveConfig {

  private boolean blink;
  private boolean snail;

}
