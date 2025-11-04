package com.example.coda.service;

import com.example.coda.model.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
    * Positions: 1(1) + 2-4(3) + 5(1) + 6-11(6) + 12-14(3) + 15-16(2) + 17-24(8) + 25-50(26) + 51-61(11) + 62-76(15) + 77-90(14) + 91(1) + 92-127(36) + 128(1)
    */
   private String writeHeaderRecord(HeaderRecord header)
   {
      StringBuilder line = new StringBuilder();
      line.append("0");                                                    // Pos 1
      line.append(formatNumber(header.getSequenceNumber(), 3));            // Pos 2-4
      line.append(formatString(header.getVersionCode(), 1));               // Pos 5
      line.append(formatDate(header.getCreationDate()));                   // Pos 6-11 (DDMMYY)
      line.append(formatString(header.getBankIdentificationNumber(), 3));  // Pos 12-14
      line.append(formatString(header.getApplicationCode(), 2));           // Pos 15-16
      line.append(formatString("", 8));                                    // Pos 17-24 Reserved
      line.append(formatString(header.getRecipientName(), 26));            // Pos 25-50
      line.append(formatString(header.getBic(), 11));                      // Pos 51-61
      line.append(formatString(header.getAccountNumber(), 15));            // Pos 62-76
      line.append(formatString(header.getAccountDescription(), 14));       // Pos 77-90
      line.append(formatString(header.getOldBalanceSign(), 1));            // Pos 91
      line.append(formatString("", 36));                                   // Pos 92-127 Reserved
      line.append(formatString(header.getDuplicateCode(), 1));             // Pos 128
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 1 - Old Balance
    * Positions: 1(1) + 2-4(3) + 5-16(12) + 17(1) + 18-20(3) + 21-22(2) + 23-24(2) + 25-39(15) + 40-45(6) + 46-71(26) + 72-105(34) + 106-108(3) + 109-128(20)
    */
   private String writeOldBalanceRecord(OldBalanceRecord balance)
   {
      StringBuilder line = new StringBuilder();
      line.append("1");                                                     // Pos 1
      line.append(formatNumber(balance.getSequenceNumber(), 3));            // Pos 2-4
      line.append(formatString(balance.getAccountNumber(), 12));            // Pos 5-16
      line.append(formatString(balance.getAccountNumberType(), 1));         // Pos 17
      line.append(formatString(balance.getCurrencyCode(), 3));              // Pos 18-20
      line.append(formatString(balance.getCountryCode(), 2));               // Pos 21-22
      line.append(formatString("", 2));                                     // Pos 23-24 Reserved
      line.append(formatAmount(balance.getOldBalance(), 15));               // Pos 25-39
      line.append(formatDate(balance.getBalanceDate()));                    // Pos 40-45 (DDMMYY)
      line.append(formatString(balance.getAccountHolderName(), 26));        // Pos 46-71
      line.append(formatString(balance.getAccountDescription(), 34));       // Pos 72-105
      line.append(formatNumber(balance.getSequenceNumberDetail(), 3));      // Pos 106-108
      line.append(formatString("", 20));                                    // Pos 109-128 Reserved
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
    * Positions: 1-2(2) + 3-6(4) + 7-18(12) + 19-26(8) + 27-41(15) + 42-47(6) + 48-68(21) + 69-115(47) + 116-121(6) + 122-124(3) + 125-127(3) + 128(1)
    */
   private String writeMovementRecord21(MovementRecord movement)
   {
      StringBuilder line = new StringBuilder();
      line.append("21");                                                    // Pos 1-2
      line.append(formatNumber(movement.getSequenceNumber(), 4));           // Pos 3-6
      line.append(formatString(movement.getAccountNumber(), 12));           // Pos 7-18
      line.append(formatString(movement.getTransactionCode(), 8));          // Pos 19-26
      line.append(formatAmount(movement.getAmount(), 15));                  // Pos 27-41
      line.append(formatDate(movement.getValueDate()));                     // Pos 42-47 (DDMMYY)
      line.append(formatString(movement.getTransactionReference(), 21));    // Pos 48-68
      line.append(formatString(movement.getCommunicationStructured(), 47)); // Pos 69-115
      line.append(formatDate(movement.getTransactionDate()));               // Pos 116-121 (DDMMYY)
      line.append(formatString(movement.getStatementNumber(), 3));          // Pos 122-124
      line.append(formatString(movement.getGlobalSequence(), 3));           // Pos 125-127
      line.append(formatString(movement.getStatementSequence(), 1));        // Pos 128
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
      line.append(formatString("", 27)); // Reserved (reduced from 30 to make room for trailing field)
      line.append("1 0"); // CODA trailing field for record 22 (positions 126-128)
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
      line.append(formatString("", 43)); // Reserved (reduced from 46 to make room for trailing field)
      line.append("0 1"); // CODA trailing field for record 23 (positions 126-128)
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 31 - Structured Communication
    * Positions: 1-2(2) + 3-6(4) + 7-18(12) + 19-20(2) + 21-100(80) + 101-125(25) + 126(1) + 127(1) + 128(1)
    */
   private String writeMovementRecord31(MovementRecord movement)
   {
      StringBuilder line = new StringBuilder();
      line.append("31");                                                    // Pos 1-2
      line.append(formatNumber(movement.getSequenceNumber(), 4));           // Pos 3-6
      line.append(formatString(movement.getAccountNumber(), 12));           // Pos 7-18
      line.append(formatString("", 2));                                     // Pos 19-20 Reserved
      line.append(formatString(movement.getStructuredCommunication(), 80)); // Pos 21-100
      line.append(formatString("", 25));                                    // Pos 101-125 Reserved
      line.append("1");                                                     // Pos 126
      line.append(" ");                                                     // Pos 127
      line.append("0");                                                     // Pos 128
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
      line.append(formatString("", 41)); // Reserved (reduced from 44 to make room for trailing field)
      line.append("0 0"); // CODA trailing field for record 32 (positions 126-128)
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 8 - New Balance
    * Positions: 1(1) + 2-4(3) + 5-16(12) + 17(1) + 18-20(3) + 21-22(2) + 23-24(2) + 25-39(15) + 40-45(6) + 46-127(82) + 128(1)
    */
   private String writeNewBalanceRecord(NewBalanceRecord balance)
   {
      StringBuilder line = new StringBuilder();
      line.append("8");                                                     // Pos 1
      line.append(formatNumber(balance.getSequenceNumber(), 3));            // Pos 2-4
      line.append(formatString(balance.getAccountNumber(), 12));            // Pos 5-16
      line.append(formatString(balance.getAccountNumberType(), 1));         // Pos 17
      line.append(formatString(balance.getCurrencyCode(), 3));              // Pos 18-20
      line.append(formatString(balance.getCountryCode(), 2));               // Pos 21-22
      line.append(formatString("", 2));                                     // Pos 23-24 Reserved
      line.append(formatAmount(balance.getNewBalance(), 15));               // Pos 25-39
      line.append(formatDate(balance.getBalanceDate()));                    // Pos 40-45 (DDMMYY)
      line.append(formatString("", 80));                                    // Pos 46-125 Reserved
      line.append("  0");                                                   // Pos 126-128
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Write Record Type 9 - Trailer
    * Positions: 1(1) + 2-16(15) + 17-22(6) + 23-37(15) + 38-52(15) + 53-127(75) + 128(1)
    */
   private String writeTrailerRecord(TrailerRecord trailer)
   {
      StringBuilder line = new StringBuilder();
      line.append("9");                                                     // Pos 1
      line.append(formatString("", 15));                                    // Pos 2-16 Reserved
      line.append(formatNumber(trailer.getNumberOfRecords(), 6));           // Pos 17-22
      line.append(formatAmount(trailer.getTotalDebit(), 15));               // Pos 23-37
      line.append(formatAmount(trailer.getTotalCredit(), 15));              // Pos 38-52
      line.append(formatString("", 73));                                    // Pos 53-125 Reserved
      line.append("  1");                                                   // Pos 126-128
      return padRight(line.toString(), LINE_LENGTH);
   }

   /**
    * Utility: Detect pure VCS reference (format +++nnn/nnnn/nnnnn+++, modulo 97)
    */
   private boolean isPureVcs(String ref) {
      if (ref == null) return false;
      String cleaned = ref.replace("+","").replace("/","").trim();
      if (cleaned.length() != 12 || !cleaned.matches("\\d{12}")) return false;
      int num = Integer.parseInt(cleaned.substring(0, 10));
      int mod = Integer.parseInt(cleaned.substring(10));
      return num % 97 == mod || (num % 97 == 0 && mod == 97);
   }

   /**
    * Write ART Grouping: Globalized amount + individual VCS payments
    * Matches reference file structure: Header, OldBalance, Global21, Detail21+22+23+31+32 (per VCS), NewBalance, Trailer
    * First movement is the global record, remaining movements are detail VCS records
    */
   public String writeArtGrouping(CodaStatement statement) {
      StringBuilder sb = new StringBuilder();
      // Header
      if (statement.getHeader() != null) {
         sb.append(writeHeaderRecord(statement.getHeader())).append("\n");
      }
      // Old Balance
      if (statement.getOldBalance() != null) {
         sb.append(writeOldBalanceRecord(statement.getOldBalance())).append("\n");
      }

      // Process movements: first is global, but all (including first) are output as details
      List<MovementRecord> movements = statement.getMovements();
      if (movements != null && !movements.isEmpty()) {
         // Global record (first movement contains global data, seq 0001)
         MovementRecord global = movements.get(0);
         sb.append(writeMovementRecord21(global)).append("\n");

         // Individual VCS payments - output ALL movements (including first) as detail records
         // Each has: 21, 22, 23, 31, 32
         // Sequence numbering: VCS n uses seq (2n-1) for 21/22/23 and seq (2n) for 31/32
         for (int i = 0; i < movements.size(); i++) {
            MovementRecord m = movements.get(i);
            int vcsIndex = i + 1;  // 1-based index for VCS
            int baseSeq = 2 * vcsIndex - 1;  // 1, 3, 5, 7, 9, 11 for VCS 1-6
            int commSeq = 2 * vcsIndex;       // 2, 4, 6, 8, 10, 12 for VCS 1-6

            // Record 21 - Detail movement
            MovementRecord m21 = m.toBuilder().sequenceNumber(baseSeq).build();
            sb.append(writeMovementRecord21(m21)).append("\n");

            // Record 22 - Counterparty information
            MovementRecord m22 = m.toBuilder().sequenceNumber(baseSeq).build();
            sb.append(writeMovementRecord22(m22)).append("\n");

            // Record 23 - Counterparty account
            MovementRecord m23 = m.toBuilder().sequenceNumber(baseSeq).build();
            sb.append(writeMovementRecord23(m23)).append("\n");

            // Record 31 - Structured communication
            MovementRecord m31 = m.toBuilder().sequenceNumber(commSeq).build();
            sb.append(writeMovementRecord31(m31)).append("\n");

            // Record 32 - Address
            MovementRecord m32 = m.toBuilder().sequenceNumber(commSeq).build();
            sb.append(writeMovementRecord32(m32)).append("\n");
         }
      }

      // New Balance
      if (statement.getNewBalance() != null) {
         sb.append(writeNewBalanceRecord(statement.getNewBalance())).append("\n");
      }
      // Trailer
      if (statement.getTrailer() != null) {
         sb.append(writeTrailerRecord(statement.getTrailer())).append("\n");
      }
      return sb.toString();
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
