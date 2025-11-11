package com.example.coda.service;

import com.example.coda.model.CodaBankTransaction;
import com.example.coda.model.CodaGlobalRecord;
import com.example.coda.model.CodaHeaderRecord;
import com.example.coda.model.CodaIndividualTransactionRecord;
import com.example.coda.model.CodaNewBalanceRecord;
import com.example.coda.model.CodaOldBalanceRecord;
import com.example.coda.model.CodaRecord21;
import com.example.coda.model.CodaRecord22;
import com.example.coda.model.CodaRecord23;
import com.example.coda.model.CodaStatement;
import com.example.coda.model.CodaTrailerRecord;
import com.example.coda.model.TransactionType;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CodaGenerator
{
   private final CodaWriter writer;

   public CodaGenerator(CodaWriter writer)
   {
      this.writer = writer;
   }

   public String generate(String bankName, String accountNumber, String currency, LocalDate statementDate,
         BigDecimal openingBalance, List<CodaBankTransaction> inputTxs)
   {
      List<CodaBankTransaction> txs = (inputTxs == null) ? List.of() : inputTxs;

      // Build CodaStatement
      CodaStatement statement = buildCodaStatement(bankName, accountNumber, currency, statementDate, openingBalance, txs);

      // Use simple write method for standard CODA generation
      return writer.write(statement);
   }

   private CodaStatement buildCodaStatement(String bankName, String accountNumber, String currency,
         LocalDate statementDate, BigDecimal openingBalance, List<CodaBankTransaction> txs)
   {
      // Header record
      CodaHeaderRecord header = CodaHeaderRecord.builder()
            .recordIdentification("0")
            .zeros("0000")
            .creationDate(statementDate)
            .bankIdentificationNumber("300")
            .applicationCode("05")
            .duplicateCode(" ")
            .filler1("       ")
            .fileReference("0123456789")
            .nameAddressee(String.format("%-26s", bankName.length() > 26 ? bankName.substring(0, 26) : bankName))
            .bic(String.format("%-11s", "BBRUBEBB"))
            .vatNumber("00000000097")
            .filler2(" ")
            .codeSeparateApplication("99991")
            .transactionReference("                ")
            .relatedReference("                ")
            .filler3("       ")
            .versionCode("2")
            .build();

      // Old balance record
      CodaOldBalanceRecord oldBalance = CodaOldBalanceRecord.builder()
            .recordIdentification("1")
            .accountStructure("0")
            .statementNumber("123")
            .accountNumber(String.format("%-37s", accountNumber.replace(" ", "") + " " + currency))
            .oldBalanceSign("0")
            .oldBalance(openingBalance)
            .balanceDate(statementDate)
            .accountHolderName(String.format("%-26s", bankName.length() > 26 ? bankName.substring(0, 26) : bankName))
            .accountDescription(String.format("%-35s", "Current account"))
            .statementNumberDetail("123")
            .build();

      // Global Record
      CodaGlobalRecord global = CodaGlobalRecord.builder()
            .recordIdentification("2")
            .articleCode("1")
            .continuousSequenceNumber("0001")
            .detailNumber("0000")
            .referenceNumber(String.format("%-21s", ""))
            .movementSign("0")
            .amount(txs.stream().map(CodaBankTransaction::amount).reduce(BigDecimal.ZERO, BigDecimal::add))
            .valueDate(statementDate)
            .transactionCode("20150000")
            .communicationType("0")
            .communicationZone(String.format("%-53s", "GROUPING OF " + txs.size() + " VCS"))
            .entryDate(statementDate)
            .statementNumber("123")
            .globalisationCode("1")
            .nextCode("0")
            .filler(" ")
            .linkCode("0")
            .build();

      // Individual Transaction records
      List<CodaIndividualTransactionRecord> individualTransactions = new ArrayList<>();
      BigDecimal totalCredits = BigDecimal.ZERO;
      BigDecimal totalDebits = BigDecimal.ZERO;

      int seq = 1;
      for (CodaBankTransaction tx : txs)
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

         // Create Record 21 - Individual Transaction Main Data
         CodaRecord21 record21 = CodaRecord21.builder()
               .recordIdentification("2")
               .articleCode("1")
               .continuousSequenceNumber(String.format("%04d", seq))
               .detailNumber("0000")
               .referenceNumber(String.format("%-21s", tx.reference() != null ? tx.reference() : ""))
               .movementSign(credit ? "0" : "1")
               .amount(tx.amount())
               .valueDate(tx.bookingDate())
               .transactionCode(credit ? "01050000" : "01050001")
               .communicationType("0")
               .communicationZone(String.format("%-53s", ""))
               .entryDate(tx.bookingDate())
               .statementNumber("123")
               .globalisationCode("0")
               .nextCode("1")
               .filler(" ")
               .linkCode("0")
               .build();

         // Create Record 22 - Communication
         CodaRecord22 record22 = CodaRecord22.builder()
               .recordIdentification("2")
               .articleCode("2")
               .continuousSequenceNumber(String.format("%04d", seq))
               .detailNumber("0000")
               .clientReference(String.format("%-53s", ""))
               .counterpartyName(String.format("%-27s", tx.counterpartyName() != null ? tx.counterpartyName().substring(0, Math.min(27, tx.counterpartyName().length())) : ""))
               .counterpartyBic(String.format("%-11s", "GKCCBEBB"))
               .filler1(String.format("%-24s", ""))
               .transactionCategory("1")
               .filler2(" ")
               .nextCode("1")
               .build();

         // Create Record 23 - Counterparty Account
         CodaRecord23 record23 = CodaRecord23.builder()
               .recordIdentification("2")
               .articleCode("3")
               .continuousSequenceNumber(String.format("%04d", seq))
               .detailNumber("0000")
               .counterpartyAccount(String.format("%-37s", tx.counterpartyAccount() != null ? tx.counterpartyAccount() : ""))
               .counterpartyAccountName(String.format("%-35s", tx.counterpartyName() != null ? tx.counterpartyName().substring(0, Math.min(35, tx.counterpartyName().length())) : ""))
               .filler1(String.format("%-43s", ""))
               .purposeCategory("0")
               .filler2(" ")
               .nextCode("0")
               .build();

         CodaIndividualTransactionRecord transactionRecord = CodaIndividualTransactionRecord.builder()
               .record21(record21)
               .record22(record22)
               .record23(record23)
               .build();

         individualTransactions.add(transactionRecord);
         seq++;
      }

      // New balance record
      BigDecimal closingBalance = openingBalance.add(totalCredits).subtract(totalDebits);
      String balanceSign = closingBalance.compareTo(BigDecimal.ZERO) >= 0 ? "0" : "1";
      // Filler is 64 chars (pos 65-128), with last char being "0"
      String fillerWith0 = String.format("%-63s", "") + "0";
      CodaNewBalanceRecord newBalance = CodaNewBalanceRecord.builder()
            .recordIdentification("8")
            .accountStructure("0")
            .statementNumber("123")
            .accountNumber(String.format("%-37s", accountNumber.replace(" ", "") + " " + currency))
            .newBalanceSign(balanceSign)
            .newBalance(closingBalance.abs())
            .balanceDate(statementDate)
            .filler(fillerWith0)
            .build();

      // Trailer record
      int totalRecordCount = 4 + individualTransactions.size() * 3;  // header, old balance, global, transaction records (21,22,23,31,32 per tx), new balance, trailer
      CodaTrailerRecord trailer = CodaTrailerRecord.builder()
            .recordIdentification("9")
            .filler1(String.format("%-15s", ""))
            .numberOfRecords(totalRecordCount)
            .totalDebit(totalDebits)
            .totalCredit(totalCredits)
            .filler2(String.format("%-75s", ""))
            .trailerMarker("1")
            .build();

      return CodaStatement.builder()
            .header(header)
            .oldBalance(oldBalance)
            .global(global)
            .individualTransactions(individualTransactions)
            .newBalance(newBalance)
            .trailer(trailer)
            .build();
   }
}
