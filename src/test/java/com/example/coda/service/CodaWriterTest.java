package com.example.coda.service;

import com.example.coda.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CODA Writer (Java objects → CODA text)
 */
class CodaWriterTest
{
   private final CodaWriter writer = new CodaWriter();

   @Test
   void writeHeaderRecordCorrectly()
   {
      HeaderRecord header = HeaderRecord.builder()
            .sequenceNumber(3)
            .versionCode("0")
            .creationDate(LocalDate.of(2025, 3, 3))
            .bankIdentificationNumber("300")
            .applicationCode("05")
            .recipientName("AXA BELGIUM SA")
            .bic("BBRUBEBB")
            .accountNumber("00404483367")
            .accountDescription("00000")
            .oldBalanceSign(" ")
            .duplicateCode("2")
            .build();

      CodaStatement statement = CodaStatement.builder()
            .header(header)
            .build();

      String result = writer.write(statement);

      assertTrue(result.startsWith("0"));
      assertTrue(result.contains("AXA BELGIUM SA"));
      assertTrue(result.contains("BBRUBEBB"));
   }

   @Test
   void writeOldBalanceRecordCorrectly()
   {
      OldBalanceRecord oldBalance = OldBalanceRecord.builder()
            .sequenceNumber(2)
            .accountNumber("4310000017062")
            .accountNumberType("2")
            .currencyCode("EUR")
            .countryCode("0B")
            .oldBalance(new BigDecimal("1700.22"))
            .balanceDate(LocalDate.of(2025, 2, 27))
            .accountHolderName("AXA BELGIUM SA")
            .accountDescription("Compte à vue")
            .sequenceNumberDetail(24)
            .build();

      CodaStatement statement = CodaStatement.builder()
            .oldBalance(oldBalance)
            .build();

      String result = writer.write(statement);

      assertTrue(result.contains("1"));
      assertTrue(result.contains("EUR"));
            assertTrue(result.contains("00000000170022")); // 1700.22 in cents
   }

   @Test
   void writeMovementRecordCorrectly()
   {
      MovementRecord movement = MovementRecord.builder()
            .sequenceNumber(1)
            .accountNumber("0003010383")
            .transactionCode("03291000")
            .amount(new BigDecimal("2441.20"))
            .valueDate(LocalDate.of(2025, 3, 25))
            .transactionReference("201500000REGROU")
            .communicationStructured("PEMENT DE      6 VCS")
            .transactionDate(LocalDate.of(2025, 3, 25))
            .statementNumber("024")
            .globalSequence("10 ")
            .statementSequence("0")
            .counterpartyName("UCAR")
            .counterpartyBic("BBRUBEBB")
            .counterpartyAccount("BE84390060159859")
            .counterpartyAccountName("UCAR")
            .structuredCommunication("03291000028  601500001001UCAR")
            .counterpartyAddress("BEKE TUINWIJK 35")
            .counterpartyPostalCode("9950")
            .counterpartyCity("WAARSCHOOT")
            .build();

      List<MovementRecord> movements = new ArrayList<>();
      movements.add(movement);

      CodaStatement statement = CodaStatement.builder()
            .movements(movements)
            .build();

      String result = writer.write(statement);

      assertTrue(result.contains("21")); // Record type 21
      assertTrue(result.contains("22")); // Record type 22
      assertTrue(result.contains("23")); // Record type 23
      assertTrue(result.contains("31")); // Record type 31
      assertTrue(result.contains("32")); // Record type 32
      assertTrue(result.contains("UCAR"));
      assertTrue(result.contains("WAARSCHOOT"));
   }

   @Test
   void writeNewBalanceRecordCorrectly()
   {
      NewBalanceRecord newBalance = NewBalanceRecord.builder()
            .sequenceNumber(24)
            .accountNumber("310000017062")
            .accountNumberType(" ")
            .currencyCode("EUR")
            .countryCode("0B")
            .newBalance(new BigDecimal("1702.66"))
            .balanceDate(LocalDate.of(2025, 3, 25))
            .build();

      CodaStatement statement = CodaStatement.builder()
            .newBalance(newBalance)
            .build();

      String result = writer.write(statement);

      assertTrue(result.startsWith("8"));
      assertTrue(result.contains("EUR"));
      assertTrue(result.contains("00000000170266")); // 1702.66 in cents
   }

   @Test
   void writeTrailerRecordCorrectly()
   {
      TrailerRecord trailer = TrailerRecord.builder()
            .sequenceNumber(0)
            .numberOfRecords(33)
            .totalDebit(BigDecimal.ZERO)
            .totalCredit(new BigDecimal("2441.20"))
            .build();

      CodaStatement statement = CodaStatement.builder()
            .trailer(trailer)
            .build();

      String result = writer.write(statement);

      assertTrue(result.startsWith("9"));
      assertTrue(result.contains("000033")); // 33 records
      assertTrue(result.contains("000000000244120")); // 2441.20 in cents
   }

   @Test
   void writeCompleteStatement()
   {
      HeaderRecord header = HeaderRecord.builder()
            .sequenceNumber(3)
            .versionCode("0")
            .creationDate(LocalDate.of(2025, 3, 3))
            .recipientName("AXA BELGIUM SA")
            .bic("BBRUBEBB")
            .build();

      OldBalanceRecord oldBalance = OldBalanceRecord.builder()
            .sequenceNumber(2)
            .accountNumber("4310000017062")
            .currencyCode("EUR")
            .oldBalance(new BigDecimal("1700.22"))
            .build();

      MovementRecord movement = MovementRecord.builder()
            .sequenceNumber(1)
            .amount(new BigDecimal("125.00"))
            .counterpartyAccount("BE84390060159859")
            .counterpartyAccountName("CLIENT X")
            .build();

      NewBalanceRecord newBalance = NewBalanceRecord.builder()
            .sequenceNumber(24)
            .currencyCode("EUR")
            .newBalance(new BigDecimal("1825.22"))
            .build();

      TrailerRecord trailer = TrailerRecord.builder()
            .numberOfRecords(5)
            .totalCredit(new BigDecimal("125.00"))
            .totalDebit(BigDecimal.ZERO)
            .build();

      CodaStatement statement = CodaStatement.builder()
            .header(header)
            .oldBalance(oldBalance)
            .movements(List.of(movement))
            .newBalance(newBalance)
            .trailer(trailer)
            .build();

      String result = writer.write(statement);

      // Verify all record types are present
      assertTrue(result.contains("0")); // Header
      assertTrue(result.contains("1")); // Old balance
      assertTrue(result.contains("21")); // Movement
      assertTrue(result.contains("8")); // New balance
      assertTrue(result.contains("9")); // Trailer

      // Verify line breaks
      String[] lines = result.split("\n");
      assertTrue(lines.length >= 5);
   }

   @Test
   void writeAmountsInCentsCorrectly()
   {
      OldBalanceRecord oldBalance = OldBalanceRecord.builder()
            .sequenceNumber(2)
            .oldBalance(new BigDecimal("125000.50"))
            .build();

      CodaStatement statement = CodaStatement.builder()
            .oldBalance(oldBalance)
            .build();

      String result = writer.write(statement);

      // 125000.50 EUR = 12500050 cents
      assertTrue(result.contains("000012500050"));
   }

   @Test
   void writeZeroAmountsCorrectly()
   {
      OldBalanceRecord oldBalance = OldBalanceRecord.builder()
            .sequenceNumber(2)
            .oldBalance(BigDecimal.ZERO)
            .build();

      CodaStatement statement = CodaStatement.builder()
            .oldBalance(oldBalance)
            .build();

      String result = writer.write(statement);

      assertTrue(result.contains("000000000000000"));
   }

   @Test
   void writeNullFieldsGracefully()
   {
      MovementRecord movement = MovementRecord.builder()
            .sequenceNumber(1)
            .amount(new BigDecimal("100.00"))
            // Other fields are null
            .build();

      CodaStatement statement = CodaStatement.builder()
            .movements(List.of(movement))
            .build();

      String result = writer.write(statement);

      assertNotNull(result);
      assertTrue(result.contains("21")); // Should still generate record 21
   }

   @Test
   void writeMultipleMovements()
   {
      MovementRecord movement1 = MovementRecord.builder()
            .sequenceNumber(1)
            .amount(new BigDecimal("100.00"))
            .counterpartyAccountName("CLIENT A")
            .build();

      MovementRecord movement2 = MovementRecord.builder()
            .sequenceNumber(2)
            .amount(new BigDecimal("200.00"))
            .counterpartyAccountName("CLIENT B")
            .build();

      CodaStatement statement = CodaStatement.builder()
            .movements(List.of(movement1, movement2))
            .build();

      String result = writer.write(statement);

      assertTrue(result.contains("CLIENT A"));
      assertTrue(result.contains("CLIENT B"));
      
      // Count occurrences of record type 21
      long count = result.lines().filter(line -> line.startsWith("21")).count();
      assertEquals(2, count);
   }

   @Test
   void writeOnlyRequiredRecords()
   {
      // Test with only movement record (no header, balance, etc.)
      MovementRecord movement = MovementRecord.builder()
            .sequenceNumber(1)
            .amount(new BigDecimal("100.00"))
            .build();

      CodaStatement statement = CodaStatement.builder()
            .movements(List.of(movement))
            .build();

      String result = writer.write(statement);

      assertNotNull(result);
      assertFalse(result.isEmpty());
   }

   @Test
   void writeLineLength128Characters()
   {
      HeaderRecord header = HeaderRecord.builder()
            .sequenceNumber(1)
            .recipientName("TEST BANK")
            .build();

      CodaStatement statement = CodaStatement.builder()
            .header(header)
            .build();

      String result = writer.write(statement);
      String[] lines = result.split("\n");

      // Each line should be exactly 128 characters
      for (String line : lines)
      {
         if (!line.isEmpty())
         {
            assertEquals(128, line.length(), "Line should be 128 characters: " + line);
         }
      }
   }

   @Test
   void writeFormatsDateCorrectly()
   {
      OldBalanceRecord oldBalance = OldBalanceRecord.builder()
            .sequenceNumber(2)
            .balanceDate(LocalDate.of(2025, 12, 31))
            .oldBalance(BigDecimal.ZERO)
            .build();

      CodaStatement statement = CodaStatement.builder()
            .oldBalance(oldBalance)
            .build();

      String result = writer.write(statement);

      // Date should be in DDMMYY format: 31/12/25 → 311225
      assertTrue(result.contains("311225"));
   }

   @Test
   void writePadsNumericFieldsWithZeros()
   {
      TrailerRecord trailer = TrailerRecord.builder()
            .numberOfRecords(5)
            .totalCredit(new BigDecimal("1.23"))
            .totalDebit(BigDecimal.ZERO)
            .build();

      CodaStatement statement = CodaStatement.builder()
            .trailer(trailer)
            .build();

      String result = writer.write(statement);

      // 5 should be padded to 000005
      assertTrue(result.contains("000005"));
      
      // 1.23 EUR = 123 cents, should be padded to 000000000000123
      assertTrue(result.contains("000000000000123"));
   }

   @Test
   void writePadsTextFieldsWithSpaces()
   {
      HeaderRecord header = HeaderRecord.builder()
            .sequenceNumber(1)
            .recipientName("TEST")
            .build();

      CodaStatement statement = CodaStatement.builder()
            .header(header)
            .build();

      String result = writer.write(statement);

      // "TEST" should be padded with spaces to fill the field
      assertTrue(result.contains("TEST"));
      
      // Verify line is 128 chars (padded with spaces)
      String[] lines = result.split("\n");
      assertEquals(128, lines[0].length());
   }
}
