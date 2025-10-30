package com.example.coda.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiError
{
   @JsonFormat(shape = JsonFormat.Shape.STRING)
   private final OffsetDateTime timestamp = OffsetDateTime.now();
   private final int status;
   private final String error;
   private final String message;
   private final String path;
}
