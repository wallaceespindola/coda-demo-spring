package com.example.coda.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.example.coda.model.CodaStatement;
import com.example.coda.model.HeaderRecord;
import com.example.coda.model.MovementRecord;
import com.example.coda.model.NewBalanceRecord;
import com.example.coda.model.OldBalanceRecord;
import com.example.coda.model.TrailerRecord;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/**
 * Unit test for parsing coda_test.txt file into Java data structures
 */
class CodaFileParserTest
{
   private final CodaParser parser = new CodaParser();

   @Test
   void parseCodaTestFileIntoJavaDataStructure() throws IOException
   {
      // Read the coda_test.txt file from test resources
      String codaContent = new String(Files.readAllBytes(Paths.get("src/test/java/resources/coda_test.txt")));

      // Parse CODA text into Java data structure
      CodaStatement statement = parser.parse(codaContent);

      // Verify the complete data structure was created
      assertNotNull(statement, "CodaStatement should be created");

      // 1. Verify Header Record (Record Type 0)
      assertNotNull(statement.getHeader(), "Header record should exist");
      HeaderRecord header = statement.getHeader();
      assertNotNull(header.getRecipientName(), "Recipient name should be extracted");
      assertTrue(header.getRecipientName().contains("AZA BELGIUM SA"), "Recipient should be AZA BELGIUM SA");
      assertNotNull(header.getBic(), "BIC should be extracted");
      assertNotNull(header.getAccountNumber(), "Account number should be extracted");

      // 2. Verify Old Balance Record (Record Type 1)
      assertNotNull(statement.getOldBalance(), "Old balance record should exist");
      OldBalanceRecord oldBalance = statement.getOldBalance();
      assertNotNull(oldBalance.getAccountNumber(), "Account number should be extracted");
      assertNotNull(oldBalance.getCurrencyCode(), "Currency code should be extracted");
      assertNotNull(oldBalance.getOldBalance(), "Old balance amount should be extracted");
      assertTrue(oldBalance.getOldBalance().compareTo(BigDecimal.ZERO) >= 0, "Old balance should be non-negative");

      // 3. Verify Movement Records (Record Types 21, 22, 23, 31, 32)
      assertNotNull(statement.getMovements(), "Movements list should exist");
      assertFalse(statement.getMovements().isEmpty(), "Should have at least one movement");

      // Verify we have 7 movements (based on 7 record 21s in the file)
      assertTrue(statement.getMovements().size() >= 6,
            "Should have at least 6 movements, got: " + statement.getMovements().size());

      // Verify first movement structure
      MovementRecord firstMovement = statement.getMovements().get(0);
      assertNotNull(firstMovement, "First movement should exist");
      assertNotNull(firstMovement.getAmount(), "Movement amount should be extracted");
      assertTrue(firstMovement.getAmount().compareTo(BigDecimal.ZERO) > 0, "Movement amount should be positive");

      // Find a movement with counterparty details (from record 23)
      MovementRecord movementWithCounterparty = statement.getMovements().stream().filter(
            m -> m.getCounterpartyAccount() != null).findFirst().orElse(null);

      assertNotNull(movementWithCounterparty, "Should have at least one movement with counterparty");
      assertTrue(movementWithCounterparty.getCounterpartyAccount().contains("BE"), "Should be Belgian IBAN");
      assertNotNull(movementWithCounterparty.getCounterpartyAccountName(), "Counterparty name should be extracted");

      // Verify address details (from record 32)
      assertNotNull(movementWithCounterparty.getCounterpartyAddress(), "Counterparty address should be extracted");
      assertNotNull(movementWithCounterparty.getCounterpartyCity(), "Counterparty city should be extracted");

      // 4. Verify specific movements with known data
      // Movement 1: UCAR from WALESCHELT
      MovementRecord ucarMovement = statement.getMovements().stream().filter(
            m -> m.getCounterpartyAccountName() != null && m.getCounterpartyAccountName().contains(
                  "UCAR")).findFirst().orElse(null);
      assertNotNull(ucarMovement, "Should find UCAR movement");
      assertNotNull(ucarMovement.getCounterpartyCity(), "UCAR movement should have city");
      assertTrue(ucarMovement.getCounterpartyCity().contains("WALESCHELT"), "UCAR should be from WALESCHELT");
      assertNotNull(ucarMovement.getCounterpartyPostalCode(), "UCAR movement should have postal code");
      assertEquals("9950", ucarMovement.getCounterpartyPostalCode().trim(), "Postal code should be 9950");

      // Movement with JOE JOHN from ETEBELE
      MovementRecord doeJohnMovement = statement.getMovements().stream().filter(
            m -> m.getCounterpartyAccountName() != null && m.getCounterpartyAccountName().contains(
                  "JOE JOHN")).findFirst().orElse(null);
      assertNotNull(doeJohnMovement, "Should find JOE JOHN movement");
      assertNotNull(doeJohnMovement.getCounterpartyCity(), "JOE JOHN movement should have city");
      assertTrue(doeJohnMovement.getCounterpartyCity().contains("ETEBELE"), "JOE JOHN should be from ETEBELE");

      // 5. Verify New Balance Record (Record Type 8)
      assertNotNull(statement.getNewBalance(), "New balance record should exist");
      NewBalanceRecord newBalance = statement.getNewBalance();
      assertNotNull(newBalance.getNewBalance(), "New balance amount should be extracted");
      assertNotNull(newBalance.getCurrencyCode(), "Currency code should be extracted");

      // 6. Verify Trailer Record (Record Type 9)
      assertNotNull(statement.getTrailer(), "Trailer record should exist");
      TrailerRecord trailer = statement.getTrailer();
      assertEquals(33, trailer.getNumberOfRecords(), "Should have 33 records total");
      assertNotNull(trailer.getTotalCredit(), "Total credit should be extracted");
      assertTrue(trailer.getTotalCredit().compareTo(BigDecimal.ZERO) > 0, "Total credit should be positive");

      // 7. Verify data structure completeness
      System.out.println("=== CODA Test File Parsed Successfully ===");
      System.out.println("Header: " + header.getRecipientName());
      System.out.println("Old Balance: " + oldBalance.getOldBalance());
      System.out.println("Movements: " + statement.getMovements().size());
      System.out.println("New Balance: " + newBalance.getNewBalance());
      System.out.println("Total Records: " + trailer.getNumberOfRecords());
      System.out.println("Total Credit: " + trailer.getTotalCredit());
   }

   @Test
   void verifyAllMovementDetailsAreExtracted() throws IOException
   {
      String codaContent = new String(Files.readAllBytes(Paths.get("src/test/java/resources/coda_test.txt")));

      CodaStatement statement = parser.parse(codaContent);

      // Verify each movement has essential data
      for (int i = 0; i < statement.getMovements().size(); i++)
      {
         MovementRecord movement = statement.getMovements().get(i);

         assertNotNull(movement.getAmount(), "Movement " + i + " should have amount");

         // Most movements should have counterparty details
         if (movement.getCounterpartyAccount() != null)
         {
            assertTrue(movement.getCounterpartyAccount().length() > 0,
                  "Movement " + i + " counterparty account should not be empty");
         }
      }
   }

   @Test
   void verifyIbanAutoCompletionInMovements() throws IOException
   {
      String codaContent = new String(Files.readAllBytes(Paths.get("src/test/java/resources/coda_test.txt")));

      CodaStatement statement = parser.parse(codaContent);

      // Find movements with Belgian IBANs
      long belgianIbanCount = statement.getMovements().stream().filter(m -> m.getCounterpartyAccount() != null).filter(
            m -> m.getCounterpartyAccount().startsWith("BE")).count();

      assertTrue(belgianIbanCount > 0, "Should have at least one Belgian IBAN in movements");

      // Verify IBAN formatting (should have spaces)
      statement.getMovements().stream().filter(m -> m.getCounterpartyAccount() != null).filter(
            m -> m.getCounterpartyAccount().startsWith("BE")).forEach(m -> {
         String iban = m.getCounterpartyAccount();
         assertTrue(iban.contains(" "), "IBAN should be formatted with spaces: " + iban);
      });
   }

   @Test
   void verifyBalanceCalculation() throws IOException
   {
      String codaContent = new String(Files.readAllBytes(Paths.get("src/test/java/resources/coda_test.txt")));

      CodaStatement statement = parser.parse(codaContent);

      BigDecimal oldBalance = statement.getOldBalance().getOldBalance();
      BigDecimal newBalance = statement.getNewBalance().getNewBalance();
      BigDecimal totalCredit = statement.getTrailer().getTotalCredit();

      // Verify balances are valid
      assertTrue(oldBalance.compareTo(BigDecimal.ZERO) >= 0, "Old balance should be non-negative");
      assertTrue(newBalance.compareTo(BigDecimal.ZERO) >= 0, "New balance should be non-negative");
      assertTrue(totalCredit.compareTo(BigDecimal.ZERO) >= 0, "Total credit should be non-negative");
   }
}
