package com.example.coda.service;

import com.example.coda.model.CodaGlobalRecord;
import com.example.coda.model.CodaHeaderRecord;
import com.example.coda.model.CodaIndividualTransactionRecord;
import com.example.coda.model.CodaNewBalanceRecord;
import com.example.coda.model.CodaOldBalanceRecord;
import com.example.coda.model.CodaRecord21;
import com.example.coda.model.CodaRecord22;
import com.example.coda.model.CodaRecord23;
import com.example.coda.model.CodaRecord31;
import com.example.coda.model.CodaRecord32;
import com.example.coda.model.CodaStatement;
import com.example.coda.model.CodaTrailerRecord;
import com.example.coda.util.IbanUtil;
import org.springframework.stereotype.Service;
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
@Service
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
      List<CodaIndividualTransactionRecord> transactionRecords = new ArrayList<>();
      CodaIndividualTransactionRecord.CodaIndividualTransactionRecordBuilder currentTransaction = null;

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
               case "0": // Header Record
                  builder.header(parseHeaderRecord(line));
                  break;

               case "1": // Old Balance Record
                  builder.oldBalance(parseOldBalanceRecord(line));
                  break;

               case "2": // Transaction Records
                  String subType = line.substring(1, 2);
                  if ("1".equals(subType)) // Transaction Main Data (Record 2.1)
                  {
                     // Check if this is a global record (globalisation code = "1" at position 125)
                     String globalisationCode = line.length() >= 125 ? line.substring(124, 125) : "0";

                     if ("1".equals(globalisationCode))
                     {
                        // This is the global record (line 3) - parse as CodaGlobalRecord
                        builder.global(parseGlobalRecord(line));
                     }
                     else
                     {
                        // This is an individual transaction
                        // Save previous transaction if exists
                        if (currentTransaction != null)
                        {
                           transactionRecords.add(currentTransaction.build());
                        }
                        // Start new transaction
                        currentTransaction = CodaIndividualTransactionRecord.builder()
                              .record21(parseRecord21(line));
                     }
                  }
                  else if ("2".equals(subType) && currentTransaction != null) // Communication
                  {
                     currentTransaction.record22(parseRecord22(line));
                  }
                  else if ("3".equals(subType) && currentTransaction != null) // Counterparty Account
                  {
                     currentTransaction.record23(parseRecord23(line));
                  }
                  break;

               case "3": // Transaction Detail Records
                  if (currentTransaction != null)
                  {
                     String subType3 = line.substring(1, 2);
                     if ("1".equals(subType3)) // Structured Communication
                     {
                        currentTransaction.record31(parseRecord31(line));
                     }
                     else if ("2".equals(subType3)) // Counterparty Address
                     {
                        currentTransaction.record32(parseRecord32(line));
                     }
                  }
                  break;

               case "8": // New Balance Record
                  // Save last transaction before new balance
                  if (currentTransaction != null)
                  {
                     transactionRecords.add(currentTransaction.build());
                     currentTransaction = null;
                  }
                  builder.newBalance(parseNewBalanceRecord(line));
                  break;

               case "9": // Trailer Record
                  builder.trailer(parseTrailerRecord(line));
                  break;
            }
         }
      }

      builder.individualTransactions(transactionRecords);
      return builder.build();
   }

   /**
    * Parse Record Type 0 - Header (Identification Record)
    */
   private CodaHeaderRecord parseHeaderRecord(String line)
   {
      return CodaHeaderRecord.builder()
            .recordIdentification(extract(line, 0, 1))           // Pos 1
            .zeros(extract(line, 1, 5))                          // Pos 2-5
            .creationDate(parseDate6(line, 5, 11))               // Pos 6-11
            .bankIdentificationNumber(extract(line, 11, 14))     // Pos 12-14
            .applicationCode(extract(line, 14, 16))              // Pos 15-16
            .duplicateCode(extract(line, 16, 17))                // Pos 17
            .filler1(extract(line, 17, 24))                      // Pos 18-24
            .fileReference(extract(line, 24, 34))                // Pos 25-34 (preserve whitespace)
            .nameAddressee(extract(line, 34, 60))                // Pos 35-60 (preserve whitespace)
            .bic(extract(line, 60, 71))                          // Pos 61-71 (preserve whitespace)
            .vatNumber(extract(line, 71, 82).trim())             // Pos 72-82
            .filler2(extract(line, 82, 83))                      // Pos 83
            .codeSeparateApplication(extract(line, 83, 88))      // Pos 84-88
            .transactionReference(extract(line, 88, 104))        // Pos 89-104
            .relatedReference(extract(line, 104, 120))           // Pos 105-120
            .filler3(extract(line, 120, 127))                    // Pos 121-127
            .versionCode(extract(line, 127, 128))                // Pos 128
            .build();
   }

   /**
    * Parse Record Type 1 - Old Balance
    */
   private CodaOldBalanceRecord parseOldBalanceRecord(String line)
   {
      String accountNumber = extract(line, 5, 42).trim();
      String completedAccount = IbanUtil.extractAndCompleteIban(accountNumber);

      return CodaOldBalanceRecord.builder()
            .recordIdentification(extract(line, 0, 1))           // Pos 1
            .accountStructure(extract(line, 1, 2))               // Pos 2
            .statementNumber(extract(line, 2, 5))                // Pos 3-5
            .accountNumber(completedAccount)                                // Pos 6-42
            .oldBalanceSign(extract(line, 42, 43))               // Pos 43
            .oldBalance(parseAmount(line, 43, 58))               // Pos 44-58
            .balanceDate(parseDate6(line, 58, 64))               // Pos 59-64
            .accountHolderName(extract(line, 64, 90))            // Pos 65-90 (preserve whitespace)
            .accountDescription(extract(line, 90, 125))          // Pos 91-125 (preserve whitespace)
            .statementNumberDetail(extract(line, 125, 128))      // Pos 126-128
            .build();
   }

   /**
    * Parse Record Type 2.1 - Transaction Main Data
    */
   private CodaRecord21 parseRecord21(String line)
   {
      return CodaRecord21.builder()
            .recordIdentification(extract(line, 0, 1))           // Pos 1
            .articleCode(extract(line, 1, 2))                    // Pos 2
            .continuousSequenceNumber(extract(line, 2, 6))       // Pos 3-6
            .detailNumber(extract(line, 6, 10))                  // Pos 7-10
            .referenceNumber(extract(line, 10, 31).trim())       // Pos 11-31
            .movementSign(extract(line, 31, 32))                 // Pos 32
            .amount(parseAmount(line, 32, 47))                   // Pos 33-47
            .valueDate(parseDate6(line, 47, 53))                 // Pos 48-53
            .transactionCode(extract(line, 53, 61))              // Pos 54-61
            .communicationType(extract(line, 61, 62))            // Pos 62
            .communicationZone(extract(line, 62, 115).trim())    // Pos 63-115
            .entryDate(parseDate6(line, 115, 121))               // Pos 116-121
            .statementNumber(extract(line, 121, 124))            // Pos 122-124
            .globalisationCode(extract(line, 124, 125))          // Pos 125
            .nextCode(extract(line, 125, 126))                   // Pos 126
            .filler(extract(line, 126, 127))                     // Pos 127
            .linkCode(extract(line, 127, 128))                   // Pos 128
            .build();
   }

   /**
    * Parse Record Type 2.1 - Global Record (Line 3 - Mandatory)
    * This is the global amount of all VCS transactions
    */
   private CodaGlobalRecord parseGlobalRecord(String line)
   {
      return CodaGlobalRecord.builder()
            .recordIdentification(extract(line, 0, 1))           // Pos 1
            .articleCode(extract(line, 1, 2))                    // Pos 2
            .continuousSequenceNumber(extract(line, 2, 6))       // Pos 3-6
            .detailNumber(extract(line, 6, 10))                  // Pos 7-10
            .referenceNumber(extract(line, 10, 31).trim())       // Pos 11-31
            .movementSign(extract(line, 31, 32))                 // Pos 32
            .amount(parseAmount(line, 32, 47))                   // Pos 33-47
            .valueDate(parseDate6(line, 47, 53))                 // Pos 48-53
            .transactionCode(extract(line, 53, 61))              // Pos 54-61
            .communicationType(extract(line, 61, 62))            // Pos 62
            .communicationZone(extract(line, 62, 115).trim())    // Pos 63-115
            .entryDate(parseDate6(line, 115, 121))               // Pos 116-121
            .statementNumber(extract(line, 121, 124))            // Pos 122-124
            .globalisationCode(extract(line, 124, 125))          // Pos 125
            .nextCode(extract(line, 125, 126))                   // Pos 126
            .filler(extract(line, 126, 127))                     // Pos 127
            .linkCode(extract(line, 127, 128))                   // Pos 128
            .build();
   }

   /**
    * Parse Record Type 2.2 - Communication
    */
   private CodaRecord22 parseRecord22(String line)
   {
      return CodaRecord22.builder()
            .recordIdentification(extract(line, 0, 1))           // Pos 1
            .articleCode(extract(line, 1, 2))                    // Pos 2
            .continuousSequenceNumber(extract(line, 2, 6))       // Pos 3-6
            .detailNumber(extract(line, 6, 10))                  // Pos 7-10
            .clientReference(extract(line, 10, 63).trim())       // Pos 11-63
            .counterpartyName(extract(line, 63, 90).trim())      // Pos 64-90
            .counterpartyBic(extract(line, 90, 101).trim())      // Pos 91-101
            .filler1(extract(line, 101, 125))                    // Pos 102-125
            .transactionCategory(extract(line, 125, 126))        // Pos 126
            .filler2(extract(line, 126, 127))                    // Pos 127
            .nextCode(extract(line, 127, 128))                   // Pos 128
            .build();
   }

   /**
    * Parse Record Type 2.3 - Counterparty Account
    */
   private CodaRecord23 parseRecord23(String line)
   {
      String account = extract(line, 10, 47).trim();
      String completedAccount = IbanUtil.extractAndCompleteIban(account);

      return CodaRecord23.builder()
            .recordIdentification(extract(line, 0, 1))             // Pos 1
            .articleCode(extract(line, 1, 2))                      // Pos 2
            .continuousSequenceNumber(extract(line, 2, 6))         // Pos 3-6
            .detailNumber(extract(line, 6, 10))                    // Pos 7-10
            .counterpartyAccount(completedAccount)                            // Pos 11-47
            .counterpartyAccountName(extract(line, 47, 82).trim()) // Pos 48-82
            .filler1(extract(line, 82, 125))                       // Pos 83-125
            .purposeCategory(extract(line, 125, 126))              // Pos 126
            .filler2(extract(line, 126, 127))                      // Pos 127
            .nextCode(extract(line, 127, 128))                     // Pos 128
            .build();
   }

   /**
    * Parse Record Type 3.1 - Structured Communication
    */
   private CodaRecord31 parseRecord31(String line)
   {
      return CodaRecord31.builder()
            .recordIdentification(extract(line, 0, 1))              // Pos 1
            .articleCode(extract(line, 1, 2))                       // Pos 2
            .continuousSequenceNumber(extract(line, 2, 6))          // Pos 3-6
            .detailNumber(extract(line, 6, 10))                     // Pos 7-10
            .referenceNumber(extract(line, 10, 31).trim())          // Pos 11-31
            .transactionCode(extract(line, 31, 39))                 // Pos 32-39
            .structuredCommunication(extract(line, 39, 112).trim()) // Pos 40-112
            .filler1(extract(line, 112, 125))                       // Pos 113-125
            .nextCode1(extract(line, 125, 126))                     // Pos 126
            .filler2(extract(line, 126, 127))                       // Pos 127
            .nextCode2(extract(line, 127, 128))                     // Pos 128
            .build();
   }

   /**
    * Parse Record Type 3.2 - Counterparty Address
    */
   private CodaRecord32 parseRecord32(String line)
   {
      return CodaRecord32.builder()
            .recordIdentification(extract(line, 0, 1))           // Pos 1
            .articleCode(extract(line, 1, 2))                    // Pos 2
            .continuousSequenceNumber(extract(line, 2, 6))       // Pos 3-6
            .detailNumber(extract(line, 6, 10))                  // Pos 7-10
            .counterpartyAddress(extract(line, 10, 45).trim())   // Pos 11-45 (35 chars)
            .counterpartyPostalCode(extract(line, 45, 57).trim())// Pos 46-57 (12 chars)
            .counterpartyCity(extract(line, 57, 92).trim())      // Pos 58-92 (35 chars)
            .filler1(extract(line, 92, 124))                     // Pos 93-124 (32 chars)
            .nextCode1(extract(line, 124, 125))                  // Pos 125
            .filler2(extract(line, 125, 126))                    // Pos 126
            .nextCode2(extract(line, 126, 127))                  // Pos 127
            .build();
   }

   /**
    * Parse Record Type 8 - New Balance
    */
   private CodaNewBalanceRecord parseNewBalanceRecord(String line)
   {
      String accountNumber = extract(line, 5, 42).trim();
      String completedAccount = IbanUtil.extractAndCompleteIban(accountNumber);

      return CodaNewBalanceRecord.builder()
            .recordIdentification(extract(line, 0, 1))           // Pos 1
            .accountStructure(extract(line, 1, 2))               // Pos 2
            .statementNumber(extract(line, 2, 5))                // Pos 3-5
            .accountNumber(completedAccount)                                // Pos 6-42
            .newBalanceSign(extract(line, 42, 43))               // Pos 43
            .newBalance(parseAmount(line, 43, 58))               // Pos 44-58
            .balanceDate(parseDate6(line, 58, 64))               // Pos 59-64
            .filler(extract(line, 64, 128))                      // Pos 65-128
            .build();
   }

   /**
    * Parse Record Type 9 - Trailer
    */
   private CodaTrailerRecord parseTrailerRecord(String line)
   {
      return CodaTrailerRecord.builder()
            .recordIdentification(extract(line, 0, 1))           // Pos 1
            .filler1(extract(line, 1, 16))                       // Pos 2-16
            .numberOfRecords(parseInt(line, 16, 22))             // Pos 17-22
            .totalDebit(parseAmount(line, 22, 37))               // Pos 23-37
            .totalCredit(parseAmount(line, 37, 52))              // Pos 38-52
            .filler2(extract(line, 52, 127))                     // Pos 53-127
            .trailerMarker(extract(line, 127, 128))              // Pos 128
            .build();
   }

   // Helper methods

   private String extract(String line, int start, int end)
   {
      if (line.length() < end)
      {
         return line.length() > start ? line.substring(start).trim() : "";
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
      String value = extract(line, start, end).trim();
      if (value.isEmpty())
      {
         return BigDecimal.ZERO;
      }
      // Amount is in cents (last 3 digits are decimals)
      return new BigDecimal(value).divide(new BigDecimal("1000"));
   }

   private LocalDate parseDate6(String line, int start, int end)
   {
      String dateStr = extract(line, start, end).trim();
      if (dateStr.isEmpty() || "000000".equals(dateStr) || "0000".equals(dateStr) || dateStr.matches("0+"))
      {
         return null;
      }
      try
      {
         return LocalDate.parse(dateStr, DATE_FORMAT);
      }
      catch (Exception e)
      {
         return null;
      }
   }
}
