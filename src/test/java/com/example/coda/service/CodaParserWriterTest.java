package com.example.coda.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.example.coda.model.CodaStatement;
import com.example.coda.model.CodaIndividualTransactionRecord;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test CODA Parser and Writer with real CODA file
 */
@SpringBootTest
class CodaParserWriterTest
{
   @Autowired
   private CodaParser parser;

   @Autowired
   private CodaWriter writer;

   @Test
   void parseRealCodaFile() throws IOException
   {
      // Read the coda_test.txt file
      String codaContent = new String(Files.readAllBytes(Paths.get("src/test/java/resources/coda_test.txt")));

      // Parse it
      CodaStatement statement = parser.parse(codaContent);

      // Verify header
      assertNotNull(statement.getHeader());
      assertTrue(statement.getHeader().getNameAddressee().contains("AZA BELGIUM SA"));
      assertNotNull(statement.getHeader().getBic(), "BIC should not be null");
      assertTrue(statement.getHeader().getBic().trim().length() > 0, 
            "BIC should have content, got: '" + statement.getHeader().getBic() + "'");

      // Verify old balance
      assertNotNull(statement.getOldBalance());
      assertNotNull(statement.getOldBalance().getAccountNumber());
      assertTrue(statement.getOldBalance().getAccountNumber().trim().length() > 0,
            "Account number should have content: " + statement.getOldBalance().getAccountNumber());
      assertNotNull(statement.getOldBalance().getOldBalance());

      // Verify individual transactions
      assertNotNull(statement.getIndividualTransactions());
      assertTrue(statement.getIndividualTransactions().size() > 0);

      // Check first individual transaction
      CodaIndividualTransactionRecord firstIndividualTransaction = statement.getIndividualTransactions().get(0);
      assertNotNull(firstIndividualTransaction);
      assertNotNull(firstIndividualTransaction.getAmount());

      // Verify new balance
      assertNotNull(statement.getNewBalance());
      assertNotNull(statement.getNewBalance().getNewBalance());

      // Verify trailer
      assertNotNull(statement.getTrailer());
      assertTrue(statement.getTrailer().getNumberOfRecords() > 0);
   }

   @Test
   void parseAndWriteBackProducesSimilarOutput() throws IOException
   {
      // Read original file
      String originalContent = new String(Files.readAllBytes(Paths.get("src/test/java/resources/coda_test.txt")));

      // Parse it
      CodaStatement statement = parser.parse(originalContent);

      // Write it back
      String regeneratedContent = writer.write(statement);

      // Verify structure is maintained
      assertNotNull(regeneratedContent);
      assertTrue(regeneratedContent.contains("0"));
      assertTrue(regeneratedContent.contains("1"));
      assertTrue(regeneratedContent.contains("8"));
      assertTrue(regeneratedContent.contains("9"));
   }

   @Test
   void parseExtractsTransactionDetails() throws IOException
   {
      String codaContent = new String(Files.readAllBytes(Paths.get("src/test/java/resources/coda_test.txt")));
      CodaStatement statement = parser.parse(codaContent);

      // Find a transaction with counterparty details
      CodaIndividualTransactionRecord transaction = statement.getIndividualTransactions().stream()
            .filter(m -> m.getCounterpartyAccountName() != null)
            .findFirst()
            .orElse(null);

      assertNotNull(transaction, "Should have at least one transaction with counterparty");
      assertNotNull(transaction.getCounterpartyAccount());
   }

   @Test
   void parseExtractsBalances() throws IOException
   {
      String codaContent = new String(Files.readAllBytes(Paths.get("src/test/java/resources/coda_test.txt")));
      CodaStatement statement = parser.parse(codaContent);

      assertNotNull(statement.getOldBalance());
      assertNotNull(statement.getNewBalance());
      assertNotNull(statement.getOldBalance().getOldBalance());
      assertNotNull(statement.getNewBalance().getNewBalance());
   }
}
