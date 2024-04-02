package org.folio.readingroom.exception;

public class BadRequestException extends RuntimeException {

  public BadRequestException(String errorMsg) {
    super(errorMsg);
  }
}
