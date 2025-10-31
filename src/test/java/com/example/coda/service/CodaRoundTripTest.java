package com.example.coda.service;

import com.example.coda.model.*;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip tests: Parse → Write → Parse
 * Ensures data integrity through bidirectional conversion
 */
class CodaRoundTripTest
{
   private final CodaParser parser = new CodaParser();
   private final CodaWriter writer = new CodaWriter();

   @Test
   void roundTripSimpleStatement() throws IOException
   {
      // Create original statement
      CodaStatement original = createSimpleStatement();

      // Write to CODA format
      String codaText = writer.write(original);

      // Parse back
      CodaStatement parsed = parser.parse(codaText);

      // Verify data integrity
      assertNotNull(parsed.getHeader());
      assertNotNull(parsed.getOldBalance());
      assertEquals(0, original.getOldBalance().getOldBalance().compareTo(parsed.getOldBalance().getOldBalance()));
   }

   @Test
   void roundTripWithMovements() throws IOException
   {
      // Create statement with movements
      CodaStatement original = createStatementWithMovements();

      // Write to CODA
      String codaText = writer.write(original);

      // Parse back
      CodaStatement parsed = parser.parse(codaText);

      // Verify movements
      assertNotNull(parsed.getMovements());
      assertEquals(original.getMovements().size(), parsed.getMovements().size());
      
      // Verify first movement amount
      assertEquals(0, 
            original.getMovements().get(0).getAmount().compareTo(
                  parsed.getMovements().get(0).getAmount())
      );
   }

   @Test
   void roundTripPreservesAmounts() throws IOException
   {
      CodaStatement original = CodaStatement.builder()
            .oldBalance(OldBalanceRecord.builder()
                  .oldBalance(new BigDecimal("1234.56"))
                  .build())
            .newBalance(NewBalanceRecord.builder()
                  .newBalance(new BigDecimal("5678.90"))
                  .build())
            .build();

      String codaText = writer.write(original);
      CodaStatement parsed = parser.parse(codaText);

      assertEquals(0, new BigDecimal("1234.56").compareTo(parsed.getOldBalance().getOldBalance()));
      assertEquals(0, new BigDecimal("5678.90").compareTo(parsed.getNewBalance().getNewBalance()));
   }

   @Test
   void roundTripPreservesDates() throws IOException
   {
      LocalDate testDate = LocalDate.of(2025, 6, 15);
      
      CodaStatement original = CodaStatement.builder()
            .header(HeaderRecord.builder()
                  .creationDate(testDate)
                  .build())
            .oldBalance(OldBalanceRecord.builder()
                  .balanceDate(testDate)
                  .oldBalance(BigDecimal.ZERO)
                  .build())
            .build();

      String codaText = writer.write(original);
      CodaStatement parsed = parser.parse(codaText);

      assertEquals(testDate, parsed.getHeader().getCreationDate());
      assertEquals(testDate, parsed.getOldBalance().getBalanceDate());
   }

   @Test
   void roundTripPreservesCounterpartyDetails() throws IOException
   {
      MovementRecord originalMovement = MovementRecord.builder()
            .sequenceNumber(1)
            .amount(new BigDecimal("100.00"))
            .counterpartyAccount("BE68539007547034")
            .counterpartyAccountName("TEST CLIENT")
            .counterpartyAddress("TEST STREET 123")
            .counterpartyPostalCode("1000")
            .counterpartyCity("BRUSSELS")
            .build();

      CodaStatement original = CodaStatement.builder()
            .movements(List.of(originalMovement))
            .build();

      String codaText = writer.write(original);
      CodaStatement parsed = parser.parse(codaText);

      MovementRecord parsedMovement = parsed.getMovements().get(0);
      assertEquals("TEST CLIENT", parsedMovement.getCounterpartyAccountName().trim());
      assertEquals("TEST STREET 123", parsedMovement.getCounterpartyAddress().trim());
      assertEquals("1000", parsedMovement.getCounterpartyPostalCode().trim());
      assertEquals("BRUSSELS", parsedMovement.getCounterpartyCity().trim());
   }

   @Test
   void roundTripPreservesTrailerTotals() throws IOException
   {
      TrailerRecord originalTrailer = TrailerRecord.builder()
            .numberOfRecords(10)
            .totalCredit(new BigDecimal("1000.00"))
            .totalDebit(new BigDecimal("500.00"))
            .build();

      CodaStatement original = CodaStatement.builder()
            .trailer(originalTrailer)
            .build();

      String codaText = writer.write(original);
      CodaStatement parsed = parser.parse(codaText);

      assertEquals(10, parsed.getTrailer().getNumberOfRecords());
      assertEquals(0, new BigDecimal("1000.00").compareTo(parsed.getTrailer().getTotalCredit()));
      assertEquals(0, new BigDecimal("500.00").compareTo(parsed.getTrailer().getTotalDebit()));
   }

   @Test
   void roundTripMultipleMovements() throws IOException
   {
      List<MovementRecord> movements = List.of(
            createMovement(1, "100.00", "CLIENT A"),
            createMovement(2, "200.00", "CLIENT B"),
            createMovement(3, "300.00", "CLIENT C")
      );

      CodaStatement original = CodaStatement.builder()
            .movements(movements)
            .build();

      String codaText = writer.write(original);
      CodaStatement parsed = parser.parse(codaText);

      assertEquals(3, parsed.getMovements().size());
      assertEquals(0, new BigDecimal("100.00").compareTo(parsed.getMovements().get(0).getAmount()));
      assertEquals(0, new BigDecimal("200.00").compareTo(parsed.getMovements().get(1).getAmount()));
      assertEquals(0, new BigDecimal("300.00").compareTo(parsed.getMovements().get(2).getAmount()));
   }

   @Test
   void roundTripCompleteStatement() throws IOException
   {
      CodaStatement original = createCompleteStatement();

      String codaText = writer.write(original);
      CodaStatement parsed = parser.parse(codaText);

      // Verify all components
      assertNotNull(parsed.getHeader());
      assertNotNull(parsed.getOldBalance());
      assertNotNull(parsed.getMovements());
      assertNotNull(parsed.getNewBalance());
      assertNotNull(parsed.getTrailer());

      // Verify specific values
      assertEquals("AXA BELGIUM SA", parsed.getHeader().getRecipientName().trim());
      assertEquals(0, new BigDecimal("1700.22").compareTo(parsed.getOldBalance().getOldBalance()));
      assertEquals(2, parsed.getMovements().size());
      assertEquals(0, new BigDecimal("1702.66").compareTo(parsed.getNewBalance().getNewBalance()));
   }

   @Test
   void roundTripZeroAmounts() throws IOException
   {
      CodaStatement original = CodaStatement.builder()
            .oldBalance(OldBalanceRecord.builder()
                  .oldBalance(BigDecimal.ZERO)
                  .build())
            .trailer(TrailerRecord.builder()
                  .totalCredit(BigDecimal.ZERO)
                  .totalDebit(BigDecimal.ZERO)
                  .build())
            .build();

      String codaText = writer.write(original);
      CodaStatement parsed = parser.parse(codaText);

      assertEquals(BigDecimal.ZERO, parsed.getOldBalance().getOldBalance());
      assertEquals(BigDecimal.ZERO, parsed.getTrailer().getTotalCredit());
      assertEquals(BigDecimal.ZERO, parsed.getTrailer().getTotalDebit());
   }

   @Test
   void roundTripLargeAmounts() throws IOException
   {
      BigDecimal largeAmount = new BigDecimal("999999999.99");
      
      CodaStatement original = CodaStatement.builder()
            .oldBalance(OldBalanceRecord.builder()
                  .oldBalance(largeAmount)
                  .build())
            .build();

      String codaText = writer.write(original);
      CodaStatement parsed = parser.parse(codaText);

      assertEquals(largeAmount, parsed.getOldBalance().getOldBalance());
   }

   // Helper methods

   private CodaStatement createSimpleStatement()
   {
      return CodaStatement.builder()
            .header(HeaderRecord.builder()
                  .sequenceNumber(1)
                  .recipientName("TEST BANK")
                  .build())
            .oldBalance(OldBalanceRecord.builder()
                  .sequenceNumber(2)
                  .oldBalance(new BigDecimal("1000.00"))
                  .build())
            .build();
   }

   private CodaStatement createStatementWithMovements()
   {
      MovementRecord movement = MovementRecord.builder()
            .sequenceNumber(1)
            .amount(new BigDecimal("125.00"))
            .counterpartyAccountName("TEST CLIENT")
            .build();

      return CodaStatement.builder()
            .oldBalance(OldBalanceRecord.builder()
                  .oldBalance(new BigDecimal("1000.00"))
                  .build())
            .movements(List.of(movement))
            .newBalance(NewBalanceRecord.builder()
                  .newBalance(new BigDecimal("1125.00"))
                  .build())
            .build();
   }

   private CodaStatement createCompleteStatement()
   {
      return CodaStatement.builder()
            .header(HeaderRecord.builder()
                  .sequenceNumber(3)
                  .versionCode("0")
                  .creationDate(LocalDate.of(2025, 3, 3))
                  .recipientName("AXA BELGIUM SA")
                  .bic("BBRUBEBB")
                  .build())
            .oldBalance(OldBalanceRecord.builder()
                  .sequenceNumber(2)
                  .oldBalance(new BigDecimal("1700.22"))
                  .currencyCode("EUR")
                  .build())
            .movements(List.of(
                  createMovement(1, "2441.20", "UCAR"),
                  createMovement(2, "724.80", "CLIENT B")
            ))
            .newBalance(NewBalanceRecord.builder()
                  .sequenceNumber(24)
                  .newBalance(new BigDecimal("1702.66"))
                  .currencyCode("EUR")
                  .build())
            .trailer(TrailerRecord.builder()
                  .numberOfRecords(33)
                  .totalCredit(new BigDecimal("2441.20"))
                  .totalDebit(BigDecimal.ZERO)
                  .build())
            .build();
   }

   private MovementRecord createMovement(int seq, String amount, String name)
   {
      return MovementRecord.builder()
            .sequenceNumber(seq)
            .amount(new BigDecimal(amount))
            .counterpartyAccountName(name)
            .build();
   }
}
