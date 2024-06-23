package it.theapplegeek.spring_starter_pack.util.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagedRequestParams {
  @Builder.Default private int page = 0;
  @Builder.Default private int size = 10;
  private String sort;
  @Builder.Default private String direction = Sort.DEFAULT_DIRECTION.toString();

  public Pageable asPageable() {
    if (getSort() == null) {
      return PageRequest.of(getPage(), getSize());
    }
    return PageRequest.of(
        getPage(),
        getSize(),
        Sort.Direction.fromOptionalString(getDirection()).orElse(Sort.DEFAULT_DIRECTION),
        getSort());
  }
}
