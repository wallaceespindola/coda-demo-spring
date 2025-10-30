package com.example.coda.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.coda.model.BankTransaction;
import com.example.coda.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class CodaGeneratorTest
{
   private final CodaGenerator generator = new CodaGenerator();

   @Test
   void generateWithoutTransactionsProducesHeaders()
   {
      String out = generator.generate("BELFIUS", "ACC123", "EUR", LocalDate.now(), new BigDecimal("1000.00"),
            List.of());

      assertTrue(out.contains("000000001"), "Header record marker missing");
      assertTrue(out.contains("100000002"), "Opening balance record marker missing");
      assertTrue(out.contains("900000004"), "Closing record marker missing");
   }

   @Test
   void generateIncludesTransactionLinesAndTotals()
   {
      BankTransaction tx = BankTransaction.builder()
            .bookingDate(LocalDate.of(2025, 9, 3))
            .type(TransactionType.CREDIT)
            .amount(new BigDecimal("125.00"))
            .counterpartyName("CLIENT X")
            .counterpartyAccount("BE12 3456 7890 1234")
            .description("Payment received")
            .reference("INV-2025-0456")
            .build();

      String out = generator.generate("BELFIUS", "BE68 5390 0754 7034", "EUR",
            LocalDate.of(2025, 9, 4), new BigDecimal("1200.00"), List.of(tx));

      assertTrue(out.contains("200000003"), "Transaction record marker missing");
      assertTrue(out.contains("CR000000000012500"), "Credit amount should be represented in cents");
      assertTrue(out.contains("CLIENT X"), "Counterparty name should appear in output");
      assertTrue(out.contains("CL000000000132500"), "Closing balance should reflect credit addition");
   }
}
