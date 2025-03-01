package it.theapplegeek.spring_starter_pack.quartz.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobInfo {
  private String name;
  private String group;
  private String cronExpression;
}
