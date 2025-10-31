package com.example.coda.service;

import com.example.coda.model.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Writer for Belgian CODA bank statement format
 * Converts Java data structures to CODA text format
 */
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

      // Records 2x and 3x - Movements
      if (statement.getMovements() != null)
      {
         for (MovementRecord movement : statement.getMovements())
         {
            sb.append(writeMovementRecords(movement));
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
   private String writeHeaderRecord(HeaderRecord header)
   {
      StringBuilder line = new StringBuilder();
      line.append("0");
      line.append(formatNumber(header.getSequenceNumber(), 3));
      line.append(formatString(header.getVersionCode(), 1));
      line.append(formatDate(header.getCreationDate()));
      line.append(formatString(header.getBankIdentificationNumber(), 3));
      line.append(formatString(header.getApplicationCode(), 2));
      line.append(formatString("", 8)); // Reserved
      line.append(formatString(header.getRecipientName(), 26));
      line.append(formatString(header.getBic(), 11));
      line.append(formatString(header.getAccountNumber(), 15));
      line.append(formatString(header.getAccountDescription(), 14));
      line.append(formatString(header.getOldBalanceSign(), 1));
      line.append(formatString("", 36)); // Reserved
      line.append(formatString(header.getDuplicateCode(), 1));
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 1 - Old Balance
    */
   private String writeOldBalanceRecord(OldBalanceRecord balance)
   {
      StringBuilder line = new StringBuilder();
      line.append("1");
      line.append(formatNumber(balance.getSequenceNumber(), 3));
      line.append(formatString(balance.getAccountNumber(), 12));
      line.append(formatString(balance.getAccountNumberType(), 1));
      line.append(formatString(balance.getCurrencyCode(), 3));
      line.append(formatString(balance.getCountryCode(), 2));
      line.append(formatString("", 2)); // Reserved
      line.append(formatAmount(balance.getOldBalance(), 15));
      line.append(formatDate(balance.getBalanceDate()));
      line.append(formatString(balance.getAccountHolderName(), 26));
      line.append(formatString(balance.getAccountDescription(), 34));
      line.append(formatNumber(balance.getSequenceNumberDetail(), 3));
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Movement Records (21, 22, 23, 31, 32)
    */
   private String writeMovementRecords(MovementRecord movement)
   {
      StringBuilder sb = new StringBuilder();

      // Record 21 - Main movement data
      sb.append(writeMovementRecord21(movement)).append("\n");

      // Record 22 - Counterparty information (if present)
      if (movement.getCounterpartyName() != null || movement.getCounterpartyBic() != null)
      {
         sb.append(writeMovementRecord22(movement)).append("\n");
      }

      // Record 23 - Counterparty account (if present)
      if (movement.getCounterpartyAccount() != null)
      {
         sb.append(writeMovementRecord23(movement)).append("\n");
      }

      // Record 31 - Structured communication (if present)
      if (movement.getStructuredCommunication() != null)
      {
         sb.append(writeMovementRecord31(movement)).append("\n");
      }

      // Record 32 - Counterparty address (if present)
      if (movement.getCounterpartyAddress() != null)
      {
         sb.append(writeMovementRecord32(movement)).append("\n");
      }

      return sb.toString();
   }

   /**
    * Write Record Type 21 - Movement Main Data
    */
   private String writeMovementRecord21(MovementRecord movement)
   {
      StringBuilder line = new StringBuilder();
      line.append("21");
      line.append(formatNumber(movement.getSequenceNumber(), 4));
      line.append(formatString(movement.getAccountNumber(), 12));
      line.append(formatString(movement.getTransactionCode(), 8));
      line.append(formatAmount(movement.getAmount(), 15));
      line.append(formatDate(movement.getValueDate()));
      line.append(formatString(movement.getTransactionReference(), 21));
      line.append(formatString(movement.getCommunicationStructured(), 47));
      line.append(formatDate(movement.getTransactionDate()));
      line.append(formatString(movement.getStatementNumber(), 3));
      line.append(formatString(movement.getGlobalSequence(), 3));
      line.append(formatString(movement.getStatementSequence(), 1));
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 22 - Counterparty Information
    */
   private String writeMovementRecord22(MovementRecord movement)
   {
      StringBuilder line = new StringBuilder();
      line.append("22");
      line.append(formatNumber(movement.getSequenceNumber(), 4));
      line.append(formatString("", 4)); // Reserved
      line.append(formatString(movement.getCounterpartyName(), 53));
      line.append(formatString(movement.getCounterpartyBic(), 11));
      line.append(formatString("", 22)); // Reserved
      line.append(formatString(movement.getTransactionCategory(), 1));
      line.append(formatString(movement.getPurposeCategory(), 1));
      line.append(formatString("", 30)); // Reserved
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 23 - Counterparty Account
    */
   private String writeMovementRecord23(MovementRecord movement)
   {
      StringBuilder line = new StringBuilder();
      line.append("23");
      line.append(formatNumber(movement.getSequenceNumber(), 4));
      line.append(formatString("", 4)); // Reserved
      line.append(formatString(movement.getCounterpartyAccount(), 37));
      line.append(formatString(movement.getCounterpartyAccountName(), 35));
      line.append(formatString("", 46)); // Reserved
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 31 - Structured Communication
    */
   private String writeMovementRecord31(MovementRecord movement)
   {
      StringBuilder line = new StringBuilder();
      line.append("31");
      line.append(formatNumber(movement.getSequenceNumber(), 4));
      line.append(formatString(movement.getAccountNumber(), 12));
      line.append(formatString("", 2)); // Reserved
      line.append(formatString(movement.getStructuredCommunication(), 80));
      line.append(formatString("", 28)); // Reserved
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 32 - Counterparty Address
    */
   private String writeMovementRecord32(MovementRecord movement)
   {
      StringBuilder line = new StringBuilder();
      line.append("32");
      line.append(formatNumber(movement.getSequenceNumber(), 4));
      line.append(formatString("", 4)); // Reserved
      line.append(formatString(movement.getCounterpartyAddress(), 35));
      line.append(formatString(movement.getCounterpartyPostalCode(), 7));
      line.append(formatString(movement.getCounterpartyCity(), 32));
      line.append(formatString("", 44)); // Reserved
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 8 - New Balance
    */
   private String writeNewBalanceRecord(NewBalanceRecord balance)
   {
      StringBuilder line = new StringBuilder();
      line.append("8");
      line.append(formatNumber(balance.getSequenceNumber(), 3));
      line.append(formatString(balance.getAccountNumber(), 12));
      line.append(formatString(balance.getAccountNumberType(), 1));
      line.append(formatString(balance.getCurrencyCode(), 3));
      line.append(formatString(balance.getCountryCode(), 2));
      line.append(formatString("", 2)); // Reserved
      line.append(formatAmount(balance.getNewBalance(), 15));
      line.append(formatDate(balance.getBalanceDate()));
      line.append(formatString("", 80)); // Reserved
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 9 - Trailer
    */
   private String writeTrailerRecord(TrailerRecord trailer)
   {
      StringBuilder line = new StringBuilder();
      line.append("9");
      line.append(formatString("", 15)); // Reserved
      line.append(formatNumber(trailer.getNumberOfRecords(), 6));
      line.append(formatAmount(trailer.getTotalDebit(), 15));
      line.append(formatAmount(trailer.getTotalCredit(), 15));
      line.append(formatString("", 76)); // Reserved
      return padRight(line.toString(), LINE_LENGTH);
   }

   // Helper methods

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
      return padLeft(String.valueOf(value), length, '0');
   }

   private String formatAmount(BigDecimal amount, int length)
   {
      if (amount == null)
      {
         amount = BigDecimal.ZERO;
      }
      // Convert to cents
      String cents = amount.setScale(2, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"))
            .toBigInteger()
            .toString();
      return padLeft(cents, length, '0');
   }

   private String formatDate(LocalDate date)
   {
      if (date == null)
      {
         return "000000";
      }
      return date.format(DATE_FORMAT);
   }

   private String padRight(String s, int length)
   {
      if (s.length() >= length)
      {
         return s.substring(0, length);
      }
      return String.format("%-" + length + "s", s);
   }

   private String padLeft(String s, int length, char padChar)
   {
      if (s.length() >= length)
      {
         return s.substring(0, length);
      }
      return String.format("%" + length + "s", s).replace(' ', padChar);
   }
}
