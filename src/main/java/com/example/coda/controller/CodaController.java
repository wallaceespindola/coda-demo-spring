package com.example.coda.controller;

import com.example.coda.model.CodaBankTransaction;
import com.example.coda.model.CodaRequest;
import com.example.coda.model.TransactionType;
import com.example.coda.service.CodaGenerator;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coda")
public class CodaController
{
   private final CodaGenerator generator;

   public CodaController(CodaGenerator generator)
   {
      this.generator = generator;
   }

   @GetMapping(value = "/generate", produces = MediaType.TEXT_PLAIN_VALUE)
   public ResponseEntity<String> getCoda(
         @RequestParam(defaultValue = "BELFIUS") String bankName,
         @RequestParam(defaultValue = "BE68 5390 0754 7034") String account,
         @RequestParam(defaultValue = "EUR") String currency,
         @RequestParam(required = false) String date,
         @RequestParam(defaultValue = "1200.00") String opening,
         @RequestParam(name = "tx", required = false) List<String> tx)
   {
      return buildResponse(bankName, account, currency, date, opening, tx, false, null);
   }

   @GetMapping(value = "/download", produces = MediaType.TEXT_PLAIN_VALUE)
   public ResponseEntity<String> downloadCoda(
         @RequestParam(defaultValue = "BELFIUS") String bankName,
         @RequestParam(defaultValue = "BE68 5390 0754 7034") String account,
         @RequestParam(defaultValue = "EUR") String currency,
         @RequestParam(required = false) String date,
         @RequestParam(defaultValue = "1200.00") String opening,
         @RequestParam(required = false) String filename,
         @RequestParam(name = "tx", required = false) List<String> tx)
   {
      return buildResponse(bankName, account, currency, date, opening, tx, true, filename);
   }

   @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE,
         produces = MediaType.TEXT_PLAIN_VALUE)
   public ResponseEntity<String> postCoda(@Valid @RequestBody CodaRequest req)
   {
      return buildResponse(req, false, null);
   }

   @PostMapping(value = "/json/download", consumes = MediaType.APPLICATION_JSON_VALUE,
         produces = MediaType.TEXT_PLAIN_VALUE)
   public ResponseEntity<String> postCodaDownload(@Valid @RequestBody CodaRequest req,
         @RequestParam(required = false) String filename)
   {
      return buildResponse(req, true, filename);
   }

   private ResponseEntity<String> buildResponse(CodaRequest req, boolean attachment, String filename)
   {
      List<CodaBankTransaction> txs = req.transactions().stream()
            .map(tx -> CodaBankTransaction.builder()
                  .bookingDate(tx.bookingDate())
                  .type(tx.type())
                  .amount(tx.amount())
                  .counterpartyAccount(tx.counterpartyAccount())
                  .counterpartyName(tx.counterpartyName())
                  .description(tx.description())
                  .reference(tx.reference())
                  .build())
            .collect(Collectors.toList());

      return respond(req.bankName(), req.account(), req.currency(), req.date(), req.opening(), txs,
            attachment, filename);
   }

   private ResponseEntity<String> buildResponse(String bankName, String account, String currency, String date,
         String opening, List<String> rawTx, boolean attachment, String filename)
   {
      LocalDate statementDate = parseDate(date);
      BigDecimal openingBalance = parseAmount(opening);
      List<CodaBankTransaction> txs = parseInlineTransactions(rawTx);
      return respond(bankName, account, currency, statementDate, openingBalance, txs, attachment, filename);
   }

   private ResponseEntity<String> respond(String bankName, String account, String currency, LocalDate statementDate,
         BigDecimal openingBalance, List<CodaBankTransaction> txs, boolean attachment, String filename)
   {
      String body = generator.generate(bankName, account, currency, statementDate, openingBalance, txs);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.TEXT_PLAIN);
      ContentDisposition.Builder disposition = attachment ? ContentDisposition.attachment() : ContentDisposition.inline();
      headers.setContentDisposition(disposition.filename(resolveFilename(filename)).build());
      return ResponseEntity.ok().headers(headers).body(body);
   }

   private LocalDate parseDate(String date)
   {
      if (date == null || date.isBlank())
      {
         return LocalDate.now();
      }
      try
      {
         return LocalDate.parse(date.trim());
      }
      catch (DateTimeParseException ex)
      {
         throw new IllegalArgumentException("Invalid date '" + date + "'. Use yyyy-MM-dd format.");
      }
   }

   private BigDecimal parseAmount(String opening)
   {
      try
      {
         return new BigDecimal(opening.trim());
      }
      catch (RuntimeException ex)
      {
         throw new IllegalArgumentException("Invalid amount '" + opening + "'.");
      }
   }

   private List<CodaBankTransaction> parseInlineTransactions(List<String> rawTx)
   {
      if (rawTx == null || rawTx.isEmpty())
      {
         return List.of();
      }

      List<CodaBankTransaction> txs = new ArrayList<>();
      int index = 1;
      for (String entry : rawTx)
      {
         if (entry == null || entry.isBlank())
         {
            index++;
            continue;
         }

         String[] parts = entry.split(":", -1);
         if (parts.length < 5)
         {
            throw new IllegalArgumentException(
                  "Transaction #" + index + " must contain at least 5 fields: type:date:amount:account:name");
         }

         TransactionType type;
         try
         {
            type = TransactionType.valueOf(parts[0].trim().toUpperCase());
         }
         catch (IllegalArgumentException ex)
         {
            throw new IllegalArgumentException("Transaction #" + index + ": invalid type '" + parts[0] + "'.");
         }

         LocalDate bookingDate;
         try
         {
            bookingDate = LocalDate.parse(parts[1].trim());
         }
         catch (DateTimeParseException ex)
         {
            throw new IllegalArgumentException(
                  "Transaction #" + index + ": invalid date '" + parts[1] + "'. Use yyyy-MM-dd format.");
         }

         BigDecimal amount;
         try
         {
            amount = new BigDecimal(parts[2].trim());
         }
         catch (NumberFormatException ex)
         {
            throw new IllegalArgumentException(
                  "Transaction #" + index + ": invalid amount '" + parts[2] + "'.");
         }

         String counterpartyAccount = parts[3].trim();
         String counterpartyName = parts[4].trim();
         if (counterpartyAccount.isEmpty())
         {
            throw new IllegalArgumentException("Transaction #" + index + ": counterparty account is required.");
         }
         if (counterpartyName.isEmpty())
         {
            throw new IllegalArgumentException("Transaction #" + index + ": counterparty name is required.");
         }

         String description = (parts.length > 5 && !parts[5].isBlank()) ? parts[5].trim() : null;
         String reference = (parts.length > 6 && !parts[6].isBlank()) ? parts[6].trim() : null;

         txs.add(CodaBankTransaction.builder()
               .bookingDate(bookingDate)
               .type(type)
               .amount(amount)
               .counterpartyAccount(counterpartyAccount)
               .counterpartyName(counterpartyName)
               .description(description)
               .reference(reference)
               .build());
         index++;
      }

      return txs;
   }

   private String resolveFilename(String requested)
   {
      String base = (requested == null || requested.isBlank()) ? "statement.coda" : requested.trim();
      base = base.replace('\\', '-').replace('/', '-');
      if (!base.contains("."))
      {
         base += ".coda";
      }
      return base;
   }
}
