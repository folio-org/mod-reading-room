package org.folio.readingroom.exception;

public class PatronPermissionException extends RuntimeException {

  public PatronPermissionException(String errorMsg) {
    super(errorMsg);
  }
}
