package it.theapplegeek.spring_starter_pack.email.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleEmail {
  @NotNull private String to;
  private List<String> cc;
  private List<String> bcc;
  @NotNull private String subject;
  @NotNull private String text;
}
