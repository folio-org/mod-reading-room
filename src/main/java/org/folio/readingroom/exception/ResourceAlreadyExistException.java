package org.folio.readingroom.exception;

public class ResourceAlreadyExistException extends RuntimeException {

  public ResourceAlreadyExistException(String errorMsg) {
    super(errorMsg);
  }

}
