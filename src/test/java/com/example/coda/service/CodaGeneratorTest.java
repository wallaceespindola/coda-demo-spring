package com.example.coda.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import com.example.coda.model.CodaBankTransaction;
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

      assertTrue(out.startsWith("00000"), "Header record marker missing (should start with 0 + 0000)");
      assertTrue(out.contains("\n10"), "Opening balance record marker missing (should contain newline + 1 + 0)");
      assertTrue(out.contains("\n9"), "Closing record marker missing");
   }

   @Test
   void generateIncludesTransactionLinesAndTotals()
   {
      CodaBankTransaction tx = CodaBankTransaction.builder().bookingDate(LocalDate.of(2025, 9, 3)).type(
            TransactionType.CREDIT).amount(new BigDecimal("125.00")).counterpartyName("CLIENT X").counterpartyAccount(
            "BE12 3456 7890 1234").description("Payment received").reference("INV-2025-0456").build();

      String out = generator.generate("BELFIUS", "BE68 5390 0754 7034", "EUR", LocalDate.of(2025, 9, 4),
            new BigDecimal("1200.00"), List.of(tx));


      assertTrue(out.contains("21"), "Transaction record marker missing");
      // Amount 125.00 in CODA format: sign (0 for credit) + 15 digits (125000 thousandths = 000000000125000)
      assertTrue(out.contains("0000000000125000"), "Credit amount should be 125.00 = 000000000125000 in thousandths");
      assertTrue(out.contains("CLIENT X"), "Counterparty name should appear in output");
      // Closing balance: 1200.00 + 125.00 = 1325.00 = 1325000 thousandths = 000000001325000
      assertTrue(out.contains("000000001325000"), "Closing balance should reflect credit addition (1325.00)");
   }
}
