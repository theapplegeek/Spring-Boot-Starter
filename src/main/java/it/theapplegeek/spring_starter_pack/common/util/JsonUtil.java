package it.theapplegeek.spring_starter_pack.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JsonUtil {
  private final ObjectMapper mapper;

  public String toJson(Object object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      return object.toString();
    }
  }

  public String toPrettyJson(Object object) {
    try {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      return object.toString();
    }
  }

  public <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
    return mapper.readValue(json, clazz);
  }

  public <T> List<T> fromJsonList(String json, Class<T> clazz) throws JsonProcessingException {
    CollectionType collectionType =
        mapper.getTypeFactory().constructCollectionType(List.class, clazz);
    return mapper.readValue(json, collectionType);
  }
}
