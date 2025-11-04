package com.example.coda.service;

import com.example.coda.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CodaGenerator
{
   private final CodaWriter writer = new CodaWriter();

   public String generate(String bankName, String accountNumber, String currency, LocalDate statementDate,
         BigDecimal openingBalance, List<BankTransaction> inputTxs)
   {
      List<BankTransaction> txs = (inputTxs == null) ? List.of() : inputTxs;

      // Build CodaStatement
      CodaStatement statement = buildCodaStatement(bankName, accountNumber, currency, statementDate, openingBalance, txs);

      // Use simple write method for standard CODA generation
      return writer.write(statement);
   }

   private CodaStatement buildCodaStatement(String bankName, String accountNumber, String currency,
         LocalDate statementDate, BigDecimal openingBalance, List<BankTransaction> txs)
   {
      // Header record
      HeaderRecord header = HeaderRecord.builder()
            .sequenceNumber(1)
            .versionCode("2")
            .creationDate(statementDate)
            .bankIdentificationNumber("300")
            .applicationCode("05")
            .recipientName(bankName)
            .bic("BBRUBEBB")
            .accountNumber(accountNumber)
            .accountDescription("Current account")
            .oldBalanceSign("0")
            .duplicateCode("2")
            .build();

      // Old balance record
      OldBalanceRecord oldBalance = OldBalanceRecord.builder()
            .sequenceNumber(2)
            .accountNumber(accountNumber.replace(" ", ""))
            .accountNumberType("0")
            .currencyCode(currency)
            .countryCode("BE")
            .oldBalance(openingBalance)
            .balanceDate(statementDate)
            .accountHolderName(bankName)
            .accountDescription("Current account")
            .sequenceNumberDetail(123)
            .build();

      // Movement records
      List<MovementRecord> movements = new ArrayList<>();
      BigDecimal totalCredits = BigDecimal.ZERO;
      BigDecimal totalDebits = BigDecimal.ZERO;

      int seq = 3;
      for (BankTransaction tx : txs)
      {
         if (tx == null) continue;

         boolean credit = tx.type() == TransactionType.CREDIT;
         if (credit)
         {
            totalCredits = totalCredits.add(tx.amount());
         }
         else
         {
            totalDebits = totalDebits.add(tx.amount());
         }

         MovementRecord movement = MovementRecord.builder()
               .sequenceNumber(seq++)
               .accountNumber(accountNumber.replace(" ", ""))
               .transactionCode(credit ? "CR000000" : "DR000000")
               .amount(tx.amount())
               .valueDate(tx.bookingDate())
               .transactionReference(tx.reference() != null ? tx.reference() : "")
               .communicationStructured("")
               .transactionDate(tx.bookingDate())
               .statementNumber("123")
               .globalSequence("0")
               .statementSequence("0")
               .counterpartyName(tx.counterpartyName())
               .counterpartyBic("BBRUBEBB")
               .counterpartyAccount(tx.counterpartyAccount())
               .counterpartyAccountName(tx.counterpartyName())
               .counterpartyAddress(tx.description() != null ? tx.description() : "")
               .counterpartyPostalCode("")
               .counterpartyCity("")
               .transactionCategory("1")
               .purposeCategory("0")
               .build();

         movements.add(movement);
      }

      // New balance record - sequence is 3 + number of movements (since movements start at 3)
      BigDecimal closingBalance = openingBalance.add(totalCredits).subtract(totalDebits);
      int newBalanceSeq = 3 + movements.size();
      NewBalanceRecord newBalance = NewBalanceRecord.builder()
            .sequenceNumber(newBalanceSeq)
            .accountNumber(accountNumber.replace(" ", ""))
            .accountNumberType("0")
            .currencyCode(currency)
            .countryCode("BE")
            .newBalance(closingBalance)
            .balanceDate(statementDate)
            .build();

      // Trailer record - sequence is after new balance
      TrailerRecord trailer = TrailerRecord.builder()
            .sequenceNumber(newBalanceSeq + 1)
            .numberOfRecords(movements.size() + 4)  // header, old balance, movements, new balance
            .totalDebit(totalDebits)
            .totalCredit(totalCredits)
            .build();

      return CodaStatement.builder()
            .header(header)
            .oldBalance(oldBalance)
            .movements(movements)
            .newBalance(newBalance)
            .trailer(trailer)
            .build();
   }
}
