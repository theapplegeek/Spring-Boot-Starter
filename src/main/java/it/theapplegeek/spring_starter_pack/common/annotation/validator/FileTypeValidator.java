package it.theapplegeek.spring_starter_pack.common.annotation.validator;

import it.theapplegeek.spring_starter_pack.common.annotation.ValidFileType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class FileTypeValidator implements ConstraintValidator<ValidFileType, Object> {
  private List<String> allowedTypes;

  @Override
  public void initialize(ValidFileType constraintAnnotation) {
    allowedTypes = Arrays.asList(constraintAnnotation.types());
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    if (value instanceof MultipartFile file) {
      return isValidFile(file);
    } else if (value instanceof List) {
      for (Object obj : (List<?>) value) {
        if (obj instanceof MultipartFile file) {
          if (!isValidFile(file)) {
            return false;
          }
        } else {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private boolean isValidFile(MultipartFile file) {
    String contentType = file.getContentType();
    return allowedTypes.contains(contentType);
  }
}
