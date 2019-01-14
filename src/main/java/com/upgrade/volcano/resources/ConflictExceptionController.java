package com.upgrade.volcano.resources;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ConflictExceptionController {
   @ExceptionHandler(value = ConflictException.class)
   public ResponseEntity<BookingResourceResponse> exception(ConflictException exception) {
      return new ResponseEntity<>(new BookingResourceResponse("the campsite is occupied for the days you have selected."), HttpStatus.CONFLICT);
   }
}