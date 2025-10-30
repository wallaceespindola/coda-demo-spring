package com.example.coda.service;

import com.example.coda.model.BankTransaction;
import com.example.coda.model.TransactionType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CodaGenerator
{
   private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

   public String generate(String bankName, String accountNumber, String currency, LocalDate statementDate,
         BigDecimal openingBalance, List<BankTransaction> inputTxs)
   {
      List<BankTransaction> txs = (inputTxs == null) ? List.of() : inputTxs;
      StringBuilder sb = new StringBuilder();
      int lineNo = 1;

      sb.append(record("000", lineNo++))
            .append(padRight(safe(bankName), 12))
            .append(padRight(safe(accountNumber), 18))
            .append(padRight(safe(currency), 3))
            .append("\n");

      sb.append(record("100", lineNo++))
            .append(statementDate.format(YYYYMMDD))
            .append(padLeft(amountToCents(openingBalance), 15, '0'))
            .append("\n");

      BigDecimal totalCredits = BigDecimal.ZERO;
      BigDecimal totalDebits = BigDecimal.ZERO;

      for (BankTransaction tx : txs)
      {
         if (tx == null)
         {
            continue;
         }

         boolean credit = tx.getType() == TransactionType.CREDIT;
         if (credit)
         {
            totalCredits = totalCredits.add(tx.getAmount());
         }
         else
         {
            totalDebits = totalDebits.add(tx.getAmount());
         }

         sb.append(record("200", lineNo++))
               .append(tx.getBookingDate().format(YYYYMMDD))
               .append(credit ? "CR" : "DB")
               .append(padLeft(amountToCents(tx.getAmount()), 15, '0'))
               .append(padRight(safe(tx.getCounterpartyAccount()), 18))
               .append(padRight(safe(tx.getCounterpartyName()), 30))
               .append(padRight(safe(tx.getDescription()), 25))
               .append(padRight(safe(tx.getReference()), 25))
               .append("\n");
      }

      BigDecimal closingBalance = openingBalance.add(totalCredits).subtract(totalDebits);

      sb.append(record("800", lineNo++))
            .append("CR")
            .append(padLeft(amountToCents(totalCredits), 15, '0'))
            .append("DB")
            .append(padLeft(amountToCents(totalDebits), 15, '0'))
            .append("\n");

      sb.append(record("900", lineNo++))
            .append("CL")
            .append(padLeft(amountToCents(closingBalance), 15, '0'))
            .append("\n");

      return sb.toString();
   }

   private String record(String code, int lineNo)
   {
      return code + String.format("%06d", lineNo);
   }

   private String padRight(String s, int n)
   {
      return String.format("%-" + n + "s", s);
   }

   private String padLeft(String s, int n, char c)
   {
      return String.format("%" + n + "s", s).replace(' ', c);
   }

   private String amountToCents(BigDecimal amt)
   {
      return amt.setScale(2, RoundingMode.HALF_UP).movePointRight(2).toBigInteger().toString();
   }

   private String safe(String s)
   {
      return (s == null) ? "" : s;
   }
}
