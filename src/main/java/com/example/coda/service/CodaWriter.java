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
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Writer for Belgian CODA bank statement format
 * Converts Java data structures to CODA text format
 */
@Service
public class CodaWriter
{
   private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyy");
   private static final int LINE_LENGTH = 128;

   /**
    * Generate CODA format from statement
    */
   public String write(CodaStatement statement)
   {
      StringBuilder sb = new StringBuilder();

      // Record 0 - Header
      if (statement.getHeader() != null)
      {
         sb.append(writeHeaderRecord(statement.getHeader())).append("\n");
      }

      // Record 1 - Old Balance
      if (statement.getOldBalance() != null)
      {
         sb.append(writeOldBalanceRecord(statement.getOldBalance())).append("\n");
      }

      // Global Record 2.1
      if (statement.getGlobal() != null)
      {
         sb.append(writeGlobal(statement.getGlobal()));
      }

      // Records 2x and 3x - Transactions
      if (statement.getIndividualTransactions() != null)
      {
         for (CodaIndividualTransactionRecord transactionRecord : statement.getIndividualTransactions())
         {
            sb.append(writeTransactionRecords(transactionRecord));
         }
      }

      // Record 8 - New Balance
      if (statement.getNewBalance() != null)
      {
         sb.append(writeNewBalanceRecord(statement.getNewBalance())).append("\n");
      }

      // Record 9 - Trailer
      if (statement.getTrailer() != null)
      {
         sb.append(writeTrailerRecord(statement.getTrailer())).append("\n");
      }

      return sb.toString();
   }

   /**
    * Write Record Type 0 - Header
    */
   private String writeHeaderRecord(CodaHeaderRecord header)
   {
      StringBuilder line = new StringBuilder();
      line.append(formatString(header.getRecordIdentification(), 1));      // Pos 1
      line.append(formatString(header.getZeros(), 4));                     // Pos 2-5
      line.append(formatDate(header.getCreationDate()));                   // Pos 6-11
      line.append(formatString(header.getBankIdentificationNumber(), 3));  // Pos 12-14
      line.append(formatString(header.getApplicationCode(), 2));           // Pos 15-16
      line.append(formatString(header.getDuplicateCode(), 1));             // Pos 17
      line.append(formatString(header.getFiller1(), 7));                   // Pos 18-24
      line.append(formatString(header.getFileReference(), 10));            // Pos 25-34
      line.append(formatString(header.getNameAddressee(), 26));            // Pos 35-60
      line.append(formatString(header.getBic(), 11));                      // Pos 61-71
      line.append(formatString(header.getVatNumber(), 11));                // Pos 72-82
      line.append(formatString(header.getFiller2(), 1));                   // Pos 83
      line.append(formatString(header.getCodeSeparateApplication(), 5));   // Pos 84-88
      line.append(formatString(header.getTransactionReference(), 16));     // Pos 89-104
      line.append(formatString(header.getRelatedReference(), 16));         // Pos 105-120
      line.append(formatString(header.getFiller3(), 7));                   // Pos 121-127
      line.append(formatString(header.getVersionCode(), 1));               // Pos 128
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 1 - Old Balance
    */
   private String writeOldBalanceRecord(CodaOldBalanceRecord balance)
   {
      StringBuilder line = new StringBuilder();
      line.append(formatString(balance.getRecordIdentification(), 1));     // Pos 1
      line.append(formatString(balance.getAccountStructure(), 1));         // Pos 2
      line.append(formatString(balance.getStatementNumber(), 3));          // Pos 3-5
      line.append(formatString(balance.getAccountNumber(), 37));           // Pos 6-42
      line.append(formatString(balance.getOldBalanceSign(), 1));           // Pos 43
      line.append(formatAmount(balance.getOldBalance(), 15));              // Pos 44-58
      line.append(formatDate(balance.getBalanceDate()));                   // Pos 59-64
      line.append(formatString(balance.getAccountHolderName(), 26));       // Pos 65-90
      line.append(formatString(balance.getAccountDescription(), 35));      // Pos 91-125
      line.append(formatString(balance.getStatementNumberDetail(), 3));    // Pos 126-128
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Global Record 2.1
    */
   private String writeGlobal(CodaGlobalRecord global)
   {
      StringBuilder line = new StringBuilder();
      line.append(formatString(global.getRecordIdentification(), 1));      // Pos 1
      line.append(formatString(global.getArticleCode(), 1));               // Pos 2
      line.append(formatString(global.getContinuousSequenceNumber(), 4));  // Pos 3-6
      line.append(formatString(global.getDetailNumber(), 4));              // Pos 7-10
      line.append(formatString(global.getReferenceNumber(), 21));          // Pos 11-31
      line.append(formatString(global.getMovementSign(), 1));              // Pos 32
      line.append(formatAmount(global.getAmount(), 15));                   // Pos 33-47
      line.append(formatDate(global.getValueDate()));                             // Pos 48-53
      line.append(formatString(global.getTransactionCode(), 8));           // Pos 54-61
      line.append(formatString(global.getCommunicationType(), 1));         // Pos 62
      line.append(formatString(global.getCommunicationZone(), 53));        // Pos 63-115
      line.append(formatDate(global.getEntryDate()));                             // Pos 116-121
      line.append(formatString(global.getStatementNumber(), 3));           // Pos 122-124
      line.append(formatString(global.getGlobalisationCode(), 1));         // Pos 125: "1"
      line.append(formatString(global.getNextCode(), 1));                  // Pos 126
      line.append(formatString(global.getFiller(), 1));                    // Pos 127
      line.append(formatString(global.getLinkCode(), 1));                  // Pos 128
      return padRight(line.toString(), LINE_LENGTH) + "\n";
   }

   /**
    * Write Transaction Records (21, 22, 23, 31, 32)
    */
   private String writeTransactionRecords(CodaIndividualTransactionRecord transaction)
   {
      StringBuilder sb = new StringBuilder();

      // Record 21 - Main transaction data
      if (transaction.getRecord21() != null)
      {
         sb.append(writeRecord21(transaction.getRecord21())).append("\n");
      }

      // Record 22 - Counterparty information (if present)
      if (transaction.getRecord22() != null)
      {
         sb.append(writeRecord22(transaction.getRecord22())).append("\n");
      }

      // Record 23 - Counterparty account (if present)
      if (transaction.getRecord23() != null)
      {
         sb.append(writeRecord23(transaction.getRecord23())).append("\n");
      }

      // Record 31 - Structured communication (if present)
      if (transaction.getRecord31() != null)
      {
         sb.append(writeRecord31(transaction.getRecord31())).append("\n");
      }

      // Record 32 - Counterparty address (if present)
      if (transaction.getRecord32() != null)
      {
         sb.append(writeRecord32(transaction.getRecord32())).append("\n");
      }

      return sb.toString();
   }

   /**
    * Write Record Type 2.1 - Transaction Main Data
    */
   private String writeRecord21(CodaRecord21 record)
   {
      StringBuilder line = new StringBuilder();
      line.append(formatString(record.getRecordIdentification(), 1));      // Pos 1
      line.append(formatString(record.getArticleCode(), 1));               // Pos 2
      line.append(formatString(record.getContinuousSequenceNumber(), 4));  // Pos 3-6
      line.append(formatString(record.getDetailNumber(), 4));              // Pos 7-10
      line.append(formatString(record.getReferenceNumber(), 21));          // Pos 11-31
      line.append(formatString(record.getMovementSign(), 1));              // Pos 32
      line.append(formatAmount(record.getAmount(), 15));                   // Pos 33-47
      line.append(formatDate(record.getValueDate()));                      // Pos 48-53
      line.append(formatString(record.getTransactionCode(), 8));           // Pos 54-61
      line.append(formatString(record.getCommunicationType(), 1));         // Pos 62
      line.append(formatString(record.getCommunicationZone(), 53));        // Pos 63-115
      line.append(formatDate(record.getEntryDate()));                      // Pos 116-121
      line.append(formatString(record.getStatementNumber(), 3));           // Pos 122-124
      line.append(formatString(record.getGlobalisationCode(), 1));         // Pos 125
      line.append(formatString(record.getNextCode(), 1));                  // Pos 126
      line.append(formatString(record.getFiller(), 1));                    // Pos 127
      line.append(formatString(record.getLinkCode(), 1));                  // Pos 128
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 2.2 - Counterparty Information
    */
   private String writeRecord22(CodaRecord22 record)
   {
      StringBuilder line = new StringBuilder();
      line.append(formatString(record.getRecordIdentification(), 1));      // Pos 1
      line.append(formatString(record.getArticleCode(), 1));               // Pos 2
      line.append(formatString(record.getContinuousSequenceNumber(), 4));  // Pos 3-6
      line.append(formatString(record.getDetailNumber(), 4));              // Pos 7-10
      line.append(formatString(record.getClientReference(), 53));          // Pos 11-63
      line.append(formatString(record.getCounterpartyName(), 27));         // Pos 64-90
      line.append(formatString(record.getCounterpartyBic(), 11));          // Pos 91-101
      line.append(formatString(record.getFiller1(), 24));                  // Pos 102-125
      line.append(formatString(record.getTransactionCategory(), 1));       // Pos 126
      line.append(formatString(record.getFiller2(), 1));                   // Pos 127
      line.append(formatString(record.getNextCode(), 1));                  // Pos 128
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 2.3 - Counterparty Account
    */
   private String writeRecord23(CodaRecord23 record)
   {
      StringBuilder line = new StringBuilder();
      line.append(formatString(record.getRecordIdentification(), 1));      // Pos 1
      line.append(formatString(record.getArticleCode(), 1));               // Pos 2
      line.append(formatString(record.getContinuousSequenceNumber(), 4));  // Pos 3-6
      line.append(formatString(record.getDetailNumber(), 4));              // Pos 7-10
      line.append(formatString(record.getCounterpartyAccount(), 37));      // Pos 11-47
      line.append(formatString(record.getCounterpartyAccountName(), 35));  // Pos 48-82
      line.append(formatString(record.getFiller1(), 43));                  // Pos 83-125
      line.append(formatString(record.getPurposeCategory(), 1));           // Pos 126
      line.append(formatString(record.getFiller2(), 1));                   // Pos 127
      line.append(formatString(record.getNextCode(), 1));                  // Pos 128
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 3.1 - Structured Communication
    */
   private String writeRecord31(CodaRecord31 record)
   {
      StringBuilder line = new StringBuilder();
      line.append(formatString(record.getRecordIdentification(), 1));      // Pos 1
      line.append(formatString(record.getArticleCode(), 1));               // Pos 2
      line.append(formatString(record.getContinuousSequenceNumber(), 4));  // Pos 3-6
      line.append(formatString(record.getDetailNumber(), 4));              // Pos 7-10
      line.append(formatString(record.getReferenceNumber(), 21));          // Pos 11-31
      line.append(formatString(record.getTransactionCode(), 8));           // Pos 32-39
      line.append(formatString(record.getStructuredCommunication(), 73));  // Pos 40-112
      line.append(formatString(record.getFiller1(), 13));                  // Pos 113-125
      line.append(formatString(record.getNextCode1(), 1));                 // Pos 126
      line.append(formatString(record.getFiller2(), 1));                   // Pos 127
      line.append(formatString(record.getNextCode2(), 1));                 // Pos 128
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 3.2 - Counterparty Address
    */
   private String writeRecord32(CodaRecord32 record)
   {
      StringBuilder line = new StringBuilder();
      line.append(formatString(record.getRecordIdentification(), 1));      // Pos 1
      line.append(formatString(record.getArticleCode(), 1));               // Pos 2
      line.append(formatString(record.getContinuousSequenceNumber(), 4));  // Pos 3-6
      line.append(formatString(record.getDetailNumber(), 4));              // Pos 7-10
      line.append(formatString(record.getCounterpartyAddress(), 36));      // Pos 11-46 (36 chars)
      line.append(formatString(record.getCounterpartyPostalCode(), 12));   // Pos 47-58 (12 chars)
      line.append(formatString(record.getCounterpartyCity(), 35));         // Pos 59-93 (35 chars)
      line.append(formatString(record.getFiller1(), 32));                  // Pos 94-125 (32 chars)
      line.append(formatString(record.getNextCode1(), 1));                 // Pos 126
      line.append(formatString(record.getFiller2(), 1));                   // Pos 127
      line.append(formatString(record.getNextCode2(), 1));                 // Pos 128
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 8 - New Balance
    */
   private String writeNewBalanceRecord(CodaNewBalanceRecord balance)
   {
      StringBuilder line = new StringBuilder();
      line.append(formatString(balance.getRecordIdentification(), 1));     // Pos 1
      line.append(formatString(balance.getAccountStructure(), 1));         // Pos 2
      line.append(formatString(balance.getStatementNumber(), 3));          // Pos 3-5
      line.append(formatString(balance.getAccountNumber(), 37));           // Pos 6-42
      line.append(formatString(balance.getNewBalanceSign(), 1));           // Pos 43
      line.append(formatAmount(balance.getNewBalance(), 15));              // Pos 44-58
      line.append(formatDate(balance.getBalanceDate()));                   // Pos 59-64
      line.append(formatString(balance.getFiller(), 64));                  // Pos 65-128
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 9 - Trailer
    */
   private String writeTrailerRecord(CodaTrailerRecord trailer)
   {
      StringBuilder line = new StringBuilder();
      line.append(formatString(trailer.getRecordIdentification(), 1));     // Pos 1
      line.append(formatString(trailer.getFiller1(), 15));                 // Pos 2-16
      line.append(formatNumber(trailer.getNumberOfRecords(), 6));          // Pos 17-22
      line.append(formatAmount(trailer.getTotalDebit(), 15));              // Pos 23-37
      line.append(formatAmount(trailer.getTotalCredit(), 15));             // Pos 38-52
      line.append(formatString(trailer.getFiller2(), 75));                 // Pos 53-127
      line.append(formatString(trailer.getTrailerMarker(), 1));            // Pos 128
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write ART Grouping: Globalized amount + individual VCS payments
    * Matches reference file structure: Header, OldBalance, Global21, Detail21+22+23+31+32 (per VCS), NewBalance, Trailer
    * First transaction of record type 2 is the global record, remaining transactions of record type 2 are detail VCS records
    */
   public String writeArtGrouping(CodaStatement statement)
   {
      StringBuilder sb = new StringBuilder();

      // Header
      if (statement.getHeader() != null)
      {
         sb.append(writeHeaderRecord(statement.getHeader())).append("\n");
      }

      // Old Balance
      if (statement.getOldBalance() != null)
      {
         sb.append(writeOldBalanceRecord(statement.getOldBalance())).append("\n");
      }

      // Process transactionRecords: first is global, rest are detail VCS records
      List<CodaIndividualTransactionRecord> transactionRecords = statement.getIndividualTransactions();
      if (transactionRecords != null && !transactionRecords.isEmpty())
      {
         // Global record (first transaction)
         CodaIndividualTransactionRecord global = transactionRecords.get(0);
         if (global.getRecord21() != null)
         {
            sb.append(writeRecord21(global.getRecord21())).append("\n");
         }

         // Individual VCS payments - output all transactionRecords (including first) as detail records
         // Each has: 21, 22, 23, 31, 32
         for (int i = 0; i < transactionRecords.size(); i++)
         {
            CodaIndividualTransactionRecord tx = transactionRecords.get(i);
            // Just write all records for each transaction
            sb.append(writeTransactionRecords(tx));
         }
      }

      // New Balance
      if (statement.getNewBalance() != null)
      {
         sb.append(writeNewBalanceRecord(statement.getNewBalance())).append("\n");
      }

      // Trailer
      if (statement.getTrailer() != null)
      {
         sb.append(writeTrailerRecord(statement.getTrailer())).append("\n");
      }

      return sb.toString();
   }

   // Utility methods

   private String formatString(String value, int length)
   {
      if (value == null)
      {
         value = "";
      }
      if (value.length() > length)
      {
         return value.substring(0, length);
      }
      return padRight(value, length);
   }

   private String formatNumber(int value, int length)
   {
      String str = String.valueOf(value);
      if (str.length() > length)
      {
         return str.substring(0, length);
      }
      return padLeft(str, length, '0');
   }

   private String formatAmount(BigDecimal amount, int length)
   {
      if (amount == null)
      {
         amount = BigDecimal.ZERO;
      }
      // Convert to cents (multiply by 1000 for 3 decimal places)
      long cents = amount.multiply(new BigDecimal("1000")).setScale(0, RoundingMode.HALF_UP).longValue();
      String str = String.valueOf(cents);
      return padLeft(str, length, '0');
   }

   private String formatDate(LocalDate date)
   {
      if (date == null)
      {
         return "000000";
      }
      return date.format(DATE_FORMAT);
   }

   private String padRight(String str, int length)
   {
      if (str.length() >= length)
      {
         return str.substring(0, length);
      }
      return String.format("%-" + length + "s", str);
   }

   private String padLeft(String str, int length, char padChar)
   {
      if (str.length() >= length)
      {
         return str;
      }
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < length - str.length(); i++)
      {
         sb.append(padChar);
      }
      sb.append(str);
      return sb.toString();
   }
}

