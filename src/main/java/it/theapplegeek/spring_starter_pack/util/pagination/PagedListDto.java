package it.theapplegeek.spring_starter_pack.util.pagination;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedListDto<T> {
  private long total;
  private int totalPages;
  private int perPage;
  private int page;
  private List<T> data;

  public static class PagedListDtoBuilder<T> {
    public PagedListDtoBuilder<T> fromPage(Page<?> e) {
      this.perPage = e.getSize();
      this.totalPages = e.getTotalPages();
      this.page = e.getNumber();
      this.total = e.getTotalElements();
      return this;
    }
  }
}
