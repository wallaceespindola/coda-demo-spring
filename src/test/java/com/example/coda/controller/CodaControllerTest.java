package com.example.coda.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.example.coda.model.CodaRequest;
import com.example.coda.model.CodaRequest.Transaction;
import com.example.coda.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class CodaControllerTest
{
   @Autowired
   private CodaController controller;

   @Test
   void downloadEndpointProducesAttachmentWithBody()
   {
      ResponseEntity<String> response = controller.downloadCoda("BELFIUS", "BE68 5390 0754 7034", "EUR", "2025-09-04",
            "1200.00", "booking_statement",
            List.of("CREDIT:2025-09-03:125.00:BE12 3456 7890 1234:CLIENT X:Payment received:INV-2025-0456"));

      HttpHeaders headers = response.getHeaders();
      ContentDisposition disposition = headers.getContentDisposition();

      assertEquals("attachment", disposition.getType());
      assertEquals("booking_statement.coda", disposition.getFilename());
      assertTrue(response.getBody().contains("CLIENT X"));
      // Check for CODA format: sign "0" for credit + amount "000000000125000" (125.00 in thousandths)
      assertTrue(response.getBody().contains("0000000000125000"), "Should contain amount in CODA format");
   }

   @Test
   void jsonDownloadUsesRequestBodyTransactions()
   {
      CodaRequest request = CodaRequest.builder().bankName("BELFIUS").account("BE68 5390 0754 7034").currency(
            "EUR").date(LocalDate.of(2025, 9, 3)).opening(new BigDecimal("1200.00")).transactions(List.of(
            Transaction.builder().bookingDate(LocalDate.of(2025, 9, 3)).type(TransactionType.CREDIT).amount(
                  new BigDecimal("125.00")).counterpartyName("CLIENT X").counterpartyAccount(
                  "BE12 3456 7890 1234").description("Payment received").reference("INV-2025-0456").build())).build();

      ResponseEntity<String> response = controller.postCodaDownload(request, "statement");

      ContentDisposition disposition = response.getHeaders().getContentDisposition();
      assertEquals("attachment", disposition.getType());
      assertEquals("statement.coda", disposition.getFilename());
      assertTrue(response.getBody() != null && response.getBody().contains("CLIENT X"));
      // Check for CODA format: amount "000000000125000" (125.00 in thousandths)
      assertTrue(response.getBody() != null && response.getBody().contains("0000000000125000"),
            "Should contain amount in CODA format");
   }

   @Test
   void invalidQueryTransactionReturnsBadRequest()
   {
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> controller.getCoda("BELFIUS", "BE68 5390 0754 7034", "EUR", null, "1200.00", List.of("INVALID")));

      assertTrue(ex.getMessage().contains("must contain at least 5 fields"));
   }
}
