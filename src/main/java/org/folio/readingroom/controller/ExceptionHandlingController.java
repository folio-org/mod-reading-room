package org.folio.readingroom.controller;

import static org.folio.readingroom.utils.ErrorHelper.ErrorCode.BAD_GATEWAY;
import static org.folio.readingroom.utils.ErrorHelper.ErrorCode.DUPLICATE_ERROR;
import static org.folio.readingroom.utils.ErrorHelper.ErrorCode.INTERNAL_SERVER_ERROR;
import static org.folio.readingroom.utils.ErrorHelper.ErrorCode.NOT_FOUND_ERROR;
import static org.folio.readingroom.utils.ErrorHelper.createExternalError;
import static org.folio.readingroom.utils.ErrorHelper.createInternalError;

import feign.FeignException;
import lombok.extern.log4j.Log4j2;
import org.folio.readingroom.domain.dto.Errors;
import org.folio.readingroom.exception.ResourceAlreadyExistException;
import org.folio.spring.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Log4j2
public class ExceptionHandlingController {

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public Errors handleGlobalException(Exception ex) {
    logExceptionMessage(ex);
    return createExternalError(ex.getMessage(), INTERNAL_SERVER_ERROR);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler({
    NotFoundException.class
  })
  public Errors handleNotFoundException(Exception ex) {
    logExceptionMessage(ex);
    return createExternalError(ex.getMessage(), NOT_FOUND_ERROR);
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler({
    ResourceAlreadyExistException.class
  })
  public Errors handleAlreadyExistException(Exception ex) {
    logExceptionMessage(ex);
    return createExternalError(ex.getMessage(), DUPLICATE_ERROR);
  }

  @ResponseStatus(HttpStatus.BAD_GATEWAY)
  @ExceptionHandler(FeignException.BadGateway.class)
  public Errors handleBadGatewayException(FeignException.BadGateway ex) {
    logExceptionMessage(ex);
    return createInternalError(ex.getMessage(), BAD_GATEWAY);
  }

  private void logExceptionMessage(Exception ex) {
    log.warn("Exception occurred ", ex);
  }

}
