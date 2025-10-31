package com.example.coda.service;

import com.example.coda.model.*;
import com.example.coda.util.IbanUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for Belgian CODA bank statement format
 * Converts CODA text format to Java data structures
 */
public class CodaParser
{
   private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyy");
   private static final DateTimeFormatter DATE_FORMAT_FULL = DateTimeFormatter.ofPattern("yyMMdd");

   /**
    * Parse a complete CODA statement from text
    */
   public CodaStatement parse(String codaContent) throws IOException
   {
      CodaStatement.CodaStatementBuilder builder = CodaStatement.builder();
      List<MovementRecord> movements = new ArrayList<>();
      MovementRecord.MovementRecordBuilder currentMovement = null;

      try (BufferedReader reader = new BufferedReader(new StringReader(codaContent)))
      {
         String line;
         while ((line = reader.readLine()) != null)
         {
            if (line.trim().isEmpty())
            {
               continue;
            }

            String recordType = line.substring(0, 1);

            switch (recordType)
            {
               case "0":
                  builder.header(parseHeaderRecord(line));
                  break;

               case "1":
                  builder.oldBalance(parseOldBalanceRecord(line));
                  break;

               case "2":
                  String subType = line.substring(1, 2);
                  if ("1".equals(subType))
                  {
                     // Save previous movement if exists
                     if (currentMovement != null)
                     {
                        movements.add(currentMovement.build());
                     }
                     // Start new movement
                     currentMovement = parseMovementRecord21(line);
                  }
                  else if ("2".equals(subType) && currentMovement != null)
                  {
                     parseMovementRecord22(line, currentMovement);
                  }
                  else if ("3".equals(subType) && currentMovement != null)
                  {
                     parseMovementRecord23(line, currentMovement);
                  }
                  break;

               case "3":
                  if (currentMovement != null)
                  {
                     String subType3 = line.substring(1, 2);
                     if ("1".equals(subType3))
                     {
                        parseMovementRecord31(line, currentMovement);
                     }
                     else if ("2".equals(subType3))
                     {
                        parseMovementRecord32(line, currentMovement);
                     }
                  }
                  break;

               case "8":
                  // Save last movement before new balance
                  if (currentMovement != null)
                  {
                     movements.add(currentMovement.build());
                     currentMovement = null;
                  }
                  builder.newBalance(parseNewBalanceRecord(line));
                  break;

               case "9":
                  builder.trailer(parseTrailerRecord(line));
                  break;
            }
         }
      }

      builder.movements(movements);
      return builder.build();
   }

   /**
    * Parse Record Type 0 - Header (Identification Record)
    * Positions based on CODA specification (1-indexed, converted to 0-indexed)
    */
   private HeaderRecord parseHeaderRecord(String line)
   {
      String accountNumber = extract(line, 61, 76).trim();
      String completedAccount = IbanUtil.extractAndCompleteIban(accountNumber);
      
      return HeaderRecord.builder()
            .sequenceNumber(parseInt(line, 1, 4))           // Pos 2-4
            .versionCode(extract(line, 4, 5))               // Pos 5
            .creationDate(parseDate6(line, 5, 11))          // Pos 6-11
            .bankIdentificationNumber(extract(line, 11, 14)) // Pos 12-14
            .applicationCode(extract(line, 14, 16))         // Pos 15-16
            .recipientName(extract(line, 23, 49).trim())    // Pos 24-49
            .bic(extract(line, 49, 61).trim())              // Pos 50-61
            .accountNumber(completedAccount)                 // Pos 62-76
            .accountDescription(extract(line, 76, 90).trim()) // Pos 77-90
            .oldBalanceSign(extract(line, 90, 91))          // Pos 91
            .duplicateCode(extract(line, 127, 128))         // Pos 128
            .build();
   }

   /**
    * Parse Record Type 1 - Old Balance
    * Positions based on CODA specification (1-indexed, converted to 0-indexed)
    */
   private OldBalanceRecord parseOldBalanceRecord(String line)
   {
      String accountNumber = extract(line, 4, 16).trim();
      String completedAccount = IbanUtil.extractAndCompleteIban(accountNumber);
      
      return OldBalanceRecord.builder()
            .sequenceNumber(parseInt(line, 1, 4))            // Pos 2-4
            .accountNumber(completedAccount)                  // Pos 5-16
            .accountNumberType(extract(line, 16, 17))        // Pos 17
            .currencyCode(extract(line, 17, 20))             // Pos 18-20
            .countryCode(extract(line, 20, 22))              // Pos 21-22
            .oldBalance(parseAmount(line, 24, 39))           // Pos 25-39
            .balanceDate(parseDate6(line, 39, 45))           // Pos 40-45
            .accountHolderName(extract(line, 45, 71).trim()) // Pos 46-71
            .accountDescription(extract(line, 71, 105).trim()) // Pos 72-105
            .sequenceNumberDetail(parseInt(line, 105, 108))  // Pos 106-108
            .build();
   }

   /**
    * Parse Record Type 21 - Movement Main Data
    * Positions based on CODA specification (1-indexed, converted to 0-indexed)
    */
   private MovementRecord.MovementRecordBuilder parseMovementRecord21(String line)
   {
      return MovementRecord.builder()
            .sequenceNumber(parseInt(line, 2, 6))              // Pos 3-6
            .accountNumber(extract(line, 6, 18).trim())        // Pos 7-18
            .transactionCode(extract(line, 18, 26))            // Pos 19-26
            .amount(parseAmount(line, 26, 41))                 // Pos 27-41
            .valueDate(parseDate6(line, 41, 47))               // Pos 42-47
            .transactionReference(extract(line, 47, 68).trim()) // Pos 48-68
            .communicationStructured(extract(line, 68, 115).trim()) // Pos 69-115
            .transactionDate(parseDate6(line, 115, 121))       // Pos 116-121
            .statementNumber(extract(line, 121, 124))          // Pos 122-124
            .globalSequence(extract(line, 124, 127))           // Pos 125-127
            .statementSequence(extract(line, 127, 128));       // Pos 128
   }

   /**
    * Parse Record Type 22 - Communication
    * Positions based on CODA specification (1-indexed, converted to 0-indexed)
    */
   private void parseMovementRecord22(String line, MovementRecord.MovementRecordBuilder builder)
   {
      builder.counterpartyName(extract(line, 10, 63).trim())   // Pos 11-63
            .counterpartyBic(extract(line, 63, 74).trim())      // Pos 64-74
            .transactionCategory(extract(line, 96, 97))         // Pos 97
            .purposeCategory(extract(line, 97, 98));            // Pos 98
   }

   /**
    * Parse Record Type 23 - Counterparty Account
    * Positions based on CODA specification (1-indexed, converted to 0-indexed)
    */
   private void parseMovementRecord23(String line, MovementRecord.MovementRecordBuilder builder)
   {
      String account = extract(line, 10, 47).trim();           // Pos 11-47
      // Auto-complete Belgian IBAN if applicable
      String completedAccount = IbanUtil.extractAndCompleteIban(account);
      
      builder.counterpartyAccount(completedAccount)
            .counterpartyAccountName(extract(line, 47, 82).trim()); // Pos 48-82
   }

   /**
    * Parse Record Type 31 - Structured Communication
    * Positions based on CODA specification (1-indexed, converted to 0-indexed)
    */
   private void parseMovementRecord31(String line, MovementRecord.MovementRecordBuilder builder)
   {
      builder.structuredCommunication(extract(line, 20, 100).trim()); // Pos 21-100
   }

   /**
    * Parse Record Type 32 - Counterparty Address
    * Positions based on CODA specification (1-indexed, converted to 0-indexed)
    */
   private void parseMovementRecord32(String line, MovementRecord.MovementRecordBuilder builder)
   {
      builder.counterpartyAddress(extract(line, 10, 45).trim())    // Pos 11-45
            .counterpartyPostalCode(extract(line, 45, 52).trim())  // Pos 46-52
            .counterpartyCity(extract(line, 52, 84).trim());       // Pos 53-84
   }

   /**
    * Parse Record Type 8 - New Balance
    * Positions based on CODA specification (1-indexed, converted to 0-indexed)
    */
   private NewBalanceRecord parseNewBalanceRecord(String line)
   {
      return NewBalanceRecord.builder()
            .sequenceNumber(parseInt(line, 1, 4))           // Pos 2-4
            .accountNumber(extract(line, 4, 16).trim())     // Pos 5-16
            .accountNumberType(extract(line, 16, 17))       // Pos 17
            .currencyCode(extract(line, 17, 20))            // Pos 18-20
            .countryCode(extract(line, 20, 22))             // Pos 21-22
            .newBalance(parseAmount(line, 24, 39))          // Pos 25-39
            .balanceDate(parseDate6(line, 39, 45))          // Pos 40-45
            .build();
   }

   /**
    * Parse Record Type 9 - Trailer
    * Positions based on CODA specification (1-indexed, converted to 0-indexed)
    */
   private TrailerRecord parseTrailerRecord(String line)
   {
      return TrailerRecord.builder()
            .sequenceNumber(parseInt(line, 1, 4))        // Pos 2-4 (not used, reserved)
            .numberOfRecords(parseInt(line, 16, 22))     // Pos 17-22
            .totalDebit(parseAmount(line, 22, 37))       // Pos 23-37
            .totalCredit(parseAmount(line, 37, 52))      // Pos 38-52
            .build();
   }

   // Helper methods

   private String extract(String line, int start, int end)
   {
      if (line.length() < end)
      {
         return line.substring(start).trim();
      }
      return line.substring(start, end);
   }

   private int parseInt(String line, int start, int end)
   {
      String value = extract(line, start, end).trim();
      return value.isEmpty() ? 0 : Integer.parseInt(value);
   }

   private BigDecimal parseAmount(String line, int start, int end)
   {
      String value = extract(line, start, end).trim().replaceAll("\\s+", "");
      if (value.isEmpty() || value.equals("0"))
      {
         return BigDecimal.ZERO;
      }
      try
      {
         // Amount is in cents, divide by 100
         return new BigDecimal(value).divide(new BigDecimal("100"));
      }
      catch (NumberFormatException e)
      {
         return BigDecimal.ZERO;
      }
   }

   private LocalDate parseDate6(String line, int start, int end)
   {
      String value = extract(line, start, end).trim();
      if (value.isEmpty() || value.equals("000000"))
      {
         return null;
      }
      try
      {
         return LocalDate.parse(value, DATE_FORMAT);
      }
      catch (Exception e)
      {
         // Try alternative format
         try
         {
            return LocalDate.parse(value, DATE_FORMAT_FULL);
         }
         catch (Exception ex)
         {
            return null;
         }
      }
   }
}
