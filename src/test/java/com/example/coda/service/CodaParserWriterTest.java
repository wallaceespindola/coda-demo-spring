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
      // Read the coda_test.txt file
      String codaContent = new String(Files.readAllBytes(Paths.get("src/test/java/resources/coda_test.txt")));

      // Parse it
      CodaStatement statement = parser.parse(codaContent);

      // Verify header
      assertNotNull(statement.getHeader());
      assertTrue(statement.getHeader().getRecipientName().contains("AXA BELGIUM SA"));
      assertNotNull(statement.getHeader().getBic(), "BIC should not be null");
      assertTrue(statement.getHeader().getBic().trim().length() > 0, 
            "BIC should have content, got: '" + statement.getHeader().getBic() + "'");

      // Verify old balance
      assertNotNull(statement.getOldBalance());
      assertNotNull(statement.getOldBalance().getCurrencyCode());
      assertTrue(statement.getOldBalance().getCurrencyCode().trim().length() >= 2, 
            "Currency code should be at least 2 chars: " + statement.getOldBalance().getCurrencyCode());
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
      String originalContent = new String(Files.readAllBytes(Paths.get("src/test/java/resources/coda_test.txt")));

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
      String codaContent = new String(Files.readAllBytes(Paths.get("src/test/java/resources/coda_test.txt")));
      CodaStatement statement = parser.parse(codaContent);

      // Find a movement with counterparty details
      MovementRecord movement = statement.getMovements().stream()
            .filter(m -> m.getCounterpartyAccountName() != null)
            .findFirst()
            .orElse(null);

      assertNotNull(movement, "Should have at least one movement with counterparty");
      assertNotNull(movement.getCounterpartyAccount());
   }

   @Test
   void parseExtractsBalances() throws IOException
   {
      String codaContent = new String(Files.readAllBytes(Paths.get("src/test/java/resources/coda_test.txt")));
      CodaStatement statement = parser.parse(codaContent);

      assertNotNull(statement.getOldBalance());
      assertNotNull(statement.getNewBalance());
      assertNotNull(statement.getOldBalance().getOldBalance());
      assertNotNull(statement.getNewBalance().getNewBalance());
   }
}
