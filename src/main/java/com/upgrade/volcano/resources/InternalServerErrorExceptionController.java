package com.upgrade.volcano.resources;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class InternalServerErrorExceptionController {
   @ExceptionHandler(value = InternalServerErrorException.class)
   public ResponseEntity<BookingResourceResponse> exception(InternalServerErrorException exception) {
      return new ResponseEntity<>(new BookingResourceResponse("an internal server error has occurred. Please, try again in a few minutes."), HttpStatus.INTERNAL_SERVER_ERROR);
   }
}