package it.theapplegeek.spring_starter_pack.util.pagination;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PagedListMapper<DTO, ENTITY> {
  public PagedListDto<DTO> toDto(Page<ENTITY> e, IMapper<DTO, ENTITY> mapper) {
    return PagedListDto.<DTO>builder()
        .fromPage(e)
        .data(e.get().map(mapper::toDto).collect(Collectors.toList()))
        .build();
  }
}
