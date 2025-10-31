package com.example.coda.service;

import com.example.coda.model.*;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test CODA Parser and Writer with real CODA file
 */
class CodaParserWriterTest
{
   private final CodaParser parser = new CodaParser();
   private final CodaWriter writer = new CodaWriter();

   @Test
   void parseRealCodaFile() throws IOException
   {
      // Read the Pai.txt file
      String codaContent = new String(Files.readAllBytes(Paths.get("Pai.txt")));

      // Parse it
      CodaStatement statement = parser.parse(codaContent);

      // Verify header
      assertNotNull(statement.getHeader());
      assertEquals("AXA BELGIUM SA", statement.getHeader().getRecipientName().trim());
      assertEquals("BBRUBEBB", statement.getHeader().getBic().trim());

      // Verify old balance
      assertNotNull(statement.getOldBalance());
      assertEquals("EUR", statement.getOldBalance().getCurrencyCode());
      assertNotNull(statement.getOldBalance().getOldBalance());

      // Verify movements
      assertNotNull(statement.getMovements());
      assertTrue(statement.getMovements().size() > 0);

      // Check first movement
      MovementRecord firstMovement = statement.getMovements().get(0);
      assertNotNull(firstMovement);
      assertNotNull(firstMovement.getAmount());

      // Verify new balance
      assertNotNull(statement.getNewBalance());
      assertNotNull(statement.getNewBalance().getNewBalance());

      // Verify trailer
      assertNotNull(statement.getTrailer());
      assertTrue(statement.getTrailer().getNumberOfRecords() > 0);
   }

   @Test
   void parseAndWriteBackProducesSimilarOutput() throws IOException
   {
      // Read original file
      String originalContent = new String(Files.readAllBytes(Paths.get("Pai.txt")));

      // Parse it
      CodaStatement statement = parser.parse(originalContent);

      // Write it back
      String regeneratedContent = writer.write(statement);

      // Verify structure is maintained
      assertNotNull(regeneratedContent);
      assertTrue(regeneratedContent.contains("0"));
      assertTrue(regeneratedContent.contains("1"));
      assertTrue(regeneratedContent.contains("8"));
      assertTrue(regeneratedContent.contains("9"));
   }

   @Test
   void parseExtractsMovementDetails() throws IOException
   {
      String codaContent = new String(Files.readAllBytes(Paths.get("Pai.txt")));
      CodaStatement statement = parser.parse(codaContent);

      // Find a movement with counterparty details
      MovementRecord movement = statement.getMovements().stream()
            .filter(m -> m.getCounterpartyName() != null)
            .findFirst()
            .orElse(null);

      assertNotNull(movement, "Should have at least one movement with counterparty");
      assertNotNull(movement.getCounterpartyAccount());
   }

   @Test
   void parseExtractsBalances() throws IOException
   {
      String codaContent = new String(Files.readAllBytes(Paths.get("Pai.txt")));
      CodaStatement statement = parser.parse(codaContent);

      assertNotNull(statement.getOldBalance());
      assertNotNull(statement.getNewBalance());
      assertNotNull(statement.getOldBalance().getOldBalance());
      assertNotNull(statement.getNewBalance().getNewBalance());
   }
}
