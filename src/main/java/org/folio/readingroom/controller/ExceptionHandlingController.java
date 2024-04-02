package org.folio.readingroom.controller;

import static org.folio.readingroom.utils.ErrorHelper.ErrorCode.BAD_GATEWAY;
import static org.folio.readingroom.utils.ErrorHelper.ErrorCode.DUPLICATE_ERROR;
import static org.folio.readingroom.utils.ErrorHelper.ErrorCode.INTERNAL_SERVER_ERROR;
import static org.folio.readingroom.utils.ErrorHelper.ErrorCode.NOT_FOUND_ERROR;
import static org.folio.readingroom.utils.ErrorHelper.ErrorCode.VALIDATION_ERROR;
import static org.folio.readingroom.utils.ErrorHelper.createExternalError;
import static org.folio.readingroom.utils.ErrorHelper.createInternalError;

import feign.FeignException;
import lombok.extern.log4j.Log4j2;
import org.folio.readingroom.domain.dto.Error;
import org.folio.readingroom.domain.dto.Errors;
import org.folio.readingroom.exception.ResourceAlreadyExistException;
import org.folio.readingroom.utils.ErrorHelper;
import org.folio.spring.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler({
    MissingServletRequestParameterException.class,
    MethodArgumentTypeMismatchException.class,
    HttpMessageNotReadableException.class,
    IllegalArgumentException.class
  })
  public Errors handleValidationErrors(Exception e) {
    log.error("Handle validation errors", e);
    return createExternalError(e.getMessage(), VALIDATION_ERROR);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public Errors handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    logExceptionMessage(ex);
    var errorList = ex.getBindingResult().getFieldErrors()
      .stream()
      .map(error -> new Error()
        // Extract the error message and validation errors from the MethodArgumentNotValidException
        .message(String.format("'%s' validation failed. %s", error.getField(), error.getDefaultMessage()))
        .type(ErrorHelper.ErrorType.EXTERNAL.getTypeCode())
        .code(VALIDATION_ERROR.name()))
      .toList();

    return new Errors().errors(errorList);
  }

  private void logExceptionMessage(Exception ex) {
    log.warn("Exception occurred ", ex);
  }

}
