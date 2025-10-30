package com.example.coda.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler
{
   @ExceptionHandler(IllegalArgumentException.class)
   public ResponseEntity<ApiError> badReq(IllegalArgumentException ex, HttpServletRequest req)
   {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(), req.getRequestURI()));
   }

   @ExceptionHandler(MethodArgumentNotValidException.class)
   public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req)
   {
      String msg = "Validation failed";
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request", msg, req.getRequestURI()));
   }
}
