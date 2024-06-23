package it.theapplegeek.spring_starter_pack.util.pagination;

public interface IMapper<DTO, ENTITY> {

  DTO toDto(ENTITY entity);
}
