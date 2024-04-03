package org.folio.readingroom.utils;

import java.util.List;
import lombok.experimental.UtilityClass;
import org.folio.readingroom.domain.dto.Error;
import org.folio.readingroom.domain.dto.Errors;

@UtilityClass
public class ErrorHelper {

  public static Error createError(String message, ErrorType type, ErrorCode errorCode) {
    var error = new Error();
    error.setMessage(message);
    error.setCode(errorCode == null ? null : errorCode.name());
    error.setType(type.getTypeCode());
    return error;
  }

  public static Errors createErrors(Error error) {
    var e = new Errors();
    e.setErrors(List.of(error));
    e.setTotalRecords(1);
    return e;
  }


  public static Errors createInternalError(String message, ErrorCode errorCode) {
    return createErrors(createError(message, ErrorType.INTERNAL, errorCode));
  }

  public static Errors createExternalError(String message, ErrorCode errorCode) {
    return createErrors(createError(message, ErrorType.EXTERNAL, errorCode));
  }

  public enum ErrorType {
    EXTERNAL("-1"), // bad request or client error
    INTERNAL("-2"); // bad gateway or internal error (db error)

    private final String typeCode;

    ErrorType(String typeCode) {
      this.typeCode = typeCode;
    }

    public String getTypeCode() {
      return typeCode;
    }

  }

  public enum ErrorCode {
    VALIDATION_ERROR,
    NOT_FOUND_ERROR,
    DUPLICATE_ERROR,
    BAD_GATEWAY,
    INTERNAL_SERVER_ERROR
  }

}
