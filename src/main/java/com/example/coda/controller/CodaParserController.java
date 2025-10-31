package com.example.coda.controller;

import com.example.coda.model.CodaStatement;
import com.example.coda.service.CodaParser;
import com.example.coda.service.CodaWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * REST API for parsing and writing CODA files
 */
@RestController
@RequestMapping("/api/coda/parser")
@Tag(name = "CODA Parser", description = "Parse and convert CODA bank statements")
public class CodaParserController
{
   private final CodaParser parser = new CodaParser();
   private final CodaWriter writer = new CodaWriter();

   @PostMapping(value = "/parse", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   @Operation(summary = "Parse CODA text to JSON", description = "Converts CODA format text to structured JSON")
   public ResponseEntity<CodaStatement> parseCoda(@RequestBody String codaContent)
   {
      try
      {
         CodaStatement statement = parser.parse(codaContent);
         return ResponseEntity.ok(statement);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Failed to parse CODA content: " + e.getMessage(), e);
      }
   }

   @PostMapping(value = "/parse/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   @Operation(summary = "Parse CODA file to JSON", description = "Upload a CODA file and get structured JSON")
   public ResponseEntity<CodaStatement> parseCodaFile(@RequestParam("file") MultipartFile file)
   {
      try
      {
         String content = new String(file.getBytes());
         CodaStatement statement = parser.parse(content);
         return ResponseEntity.ok(statement);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Failed to parse CODA file: " + e.getMessage(), e);
      }
   }

   @PostMapping(value = "/write", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
   @Operation(summary = "Write JSON to CODA format", description = "Converts structured JSON to CODA format text")
   public ResponseEntity<String> writeCoda(@RequestBody CodaStatement statement)
   {
      String codaContent = writer.write(statement);
      return ResponseEntity.ok(codaContent);
   }

   @PostMapping(value = "/convert", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
   @Operation(summary = "Parse and rewrite CODA", description = "Parse CODA text and write it back (validation/normalization)")
   public ResponseEntity<String> convertCoda(@RequestBody String codaContent)
   {
      try
      {
         CodaStatement statement = parser.parse(codaContent);
         String regenerated = writer.write(statement);
         return ResponseEntity.ok(regenerated);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Failed to convert CODA content: " + e.getMessage(), e);
      }
   }
}
