package com.example.coda.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.coda.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Core unit tests for CODA record parsing
 * Tests fundamental record types: Header (0), Old Balance (1), Global (2.1), and complete file integration
 * 
 * For comprehensive record-level testing including all sub-records (2.2, 2.3, 3.1, 3.2),
 * see CodaFileParserTest which tests complete CODA files with proper context.
 */
class CodaRecordParsingTest
{
   private CodaParser parser;

   @BeforeEach
   void setUp()
   {
      parser = new CodaParser();
   }

   /**
    * Test Record 0 - Header Record
    * Line: 0000003032530005        04308988  AZA BELGIUM SA            GKCCBEBB   00404483367 00000                                       2
    */
   @Test
   void testParseHeaderRecord() throws Exception
   {
      String line = "0000003032530005        04308988  AZA BELGIUM SA            GKCCBEBB   00404483367 00000                                       2";
      
      String codaContent = line + "\n" +
                          "10024310000017062 EUR0BE   0030000        0000000170022110270225AZA BELGIUM SA            Compte à vue                       024\n" +
                          "9               000002000000000000000000000000000000                                                                           1";
      
      CodaStatement statement = parser.parse(codaContent);
      CodaHeaderRecord header = statement.getHeader();
      
      assertNotNull(header, "Header should be parsed");
      assertEquals("0", header.getRecordIdentification());
      assertEquals("0000", header.getZeros());
      assertEquals(LocalDate.of(2025, 3, 3), header.getCreationDate());
      assertEquals("300", header.getBankIdentificationNumber());
      assertEquals("05", header.getApplicationCode());
      assertTrue(header.getFileReference().contains("04308988"));
      assertTrue(header.getNameAddressee().contains("AZA BELGIUM SA"));
      assertTrue(header.getBic().contains("GKCCBEBB"));
      assertEquals("00404483367", header.getVatNumber());
      assertEquals("2", header.getVersionCode());
      assertEquals(128, line.length(), "Line must be exactly 128 characters");
   }

   /**
    * Test Record 1 - Old Balance Record
    * Line: 10024310000017062 EUR0BE   0030000        0000000170022110270225AZA BELGIUM SA            Compte à vue                       024
    */
   @Test
   void testParseOldBalanceRecord() throws Exception
   {
      String line = "10024310000017062 EUR0BE   0030000        0000000170022110270225AZA BELGIUM SA            Compte à vue                       024";
      
      String codaContent = "0000003032530005        04308988  AZA BELGIUM SA            GKCCBEBB   00404483367 00000                                       2\n" +
                          line + "\n" +
                          "9               000002000000000000000000000000000000                                                                           1";
      
      CodaStatement statement = parser.parse(codaContent);
      CodaOldBalanceRecord oldBalance = statement.getOldBalance();
      
      assertNotNull(oldBalance, "Old balance should be parsed");
      assertEquals("1", oldBalance.getRecordIdentification());
      assertEquals("0", oldBalance.getAccountStructure());
      assertEquals("024", oldBalance.getStatementNumber());
      assertNotNull(oldBalance.getAccountNumber(), "Account number should be parsed");
      assertFalse(oldBalance.getAccountNumber().isEmpty(), "Account number should not be empty");
      assertEquals("0", oldBalance.getOldBalanceSign());
      assertEquals(0, new BigDecimal("170022.11").compareTo(oldBalance.getOldBalance()), 
          "Old balance should be 170022.11, got: " + oldBalance.getOldBalance());
      assertEquals(LocalDate.of(2025, 2, 27), oldBalance.getBalanceDate());
      assertTrue(oldBalance.getAccountHolderName().contains("AZA BELGIUM SA"));
      assertTrue(oldBalance.getAccountDescription().contains("Compte à vue"));
      assertEquals("024", oldBalance.getStatementNumberDetail());
      assertEquals(128, line.length(), "Line must be exactly 128 characters");
   }

   /**
    * Test Record 2.1 - Global Record (Mandatory Line 3)
    * Line: 21000100003010383003291000028  0000000000244120030325201500000REGROUPEMENT DE      6 VCS                           03032502410 0
    * 
    * This test verifies that the global record with globalisation code "1" at position 125 is correctly parsed.
    */
   @Test
   void testParseGlobalRecord() throws Exception
   {
      // Global record with globalisation code "1" at position 125 (0-indexed 124)
      String line = "21000100003010383003291000028  0000000000244120030325201500000REGROUPEMENT DE      6 VCS                           03032502410 0";
      
      String codaContent = "0000003032530005        04308988  AZA BELGIUM SA            GKCCBEBB   00404483367 00000                                       2\n" +
                          "10024310000017062 EUR0BE   0030000        0000000170022110270225AZA BELGIUM SA            Compte à vue                       024\n" +
                          line + "\n" +
                          "9               000003000000000000000000000000244120                                                                           1";
      
      CodaStatement statement = parser.parse(codaContent);
      CodaGlobalRecord global = statement.getGlobal();
      
      assertNotNull(global, "Global record should be parsed");
      assertEquals("2", global.getRecordIdentification());
      assertEquals("1", global.getArticleCode());
      assertEquals("0001", global.getContinuousSequenceNumber());
      assertEquals("0000", global.getDetailNumber());
      assertNotNull(global.getReferenceNumber(), "Reference number should be parsed");
      assertEquals("0", global.getMovementSign());
      assertNotNull(global.getAmount(), "Amount should be parsed");
      assertTrue(global.getAmount().compareTo(BigDecimal.ZERO) > 0, "Amount should be positive");
      assertNotNull(global.getValueDate(), "Value date should be parsed");
      assertEquals("20150000", global.getTransactionCode());
      assertEquals("0", global.getCommunicationType());
      assertTrue(global.getCommunicationZone().contains("REGROUPEMENT") || global.getCommunicationZone().contains("VCS"), 
          "Communication should mention REGROUPEMENT or VCS");
      assertNotNull(global.getEntryDate(), "Entry date should be parsed");
      assertNotNull(global.getStatementNumber(), "Statement number should be parsed");
      assertEquals("1", global.getGlobalisationCode(), "Global record must have globalisation code = 1");
      assertEquals("0", global.getNextCode());
      assertEquals("0", global.getLinkCode());
      assertEquals(128, line.length(), "Line must be exactly 128 characters");
   }

   /**
    * Test complete file with all record types
    * This is the most important integration test
    */
   @Test
   void testParseCompleteFileAllRecordTypes() throws Exception
   {
      String codaContent = 
         "0000003032530005        04308988  AZA BELGIUM SA            GKCCBEBB   00404483367 00000                                       2\n" +
         "10024310000017062 EUR0BE   0030000        0000000170022110270225AZA BELGIUM SA            Compte à vue                       024\n" +
         "21000100003010383003291000028  0000000000244120030325201500000REGROUPEMENT DE      6 VCS                           03032502410 0\n" +
         "21000100013010383003291000028  0000000000072480030325601500001102141359004019                                      03032502401 0\n" +
         "2200010001                                                     NOTPROVIDED                        GKCCBEBB                   1 0\n" +
         "2300010001BE84390060159859                     UCAR                                                                          0 1\n" +
         "31000100023010383003291000028  601500001001UCAR                                                                              1 0\n" +
         "3200010002BEKE TUINSTRAT 7                   9950        WALESCHELT                                                          0 0\n" +
         "8024310000017062 EUR0BE   0030000        0000000170266230030325                                                                0\n" +
         "9               000009000000000000000000000000316600                                                                           1";
      
      CodaStatement statement = parser.parse(codaContent);
      
      // Verify all record types are present
      assertNotNull(statement.getHeader(), "Header (Record 0) should be present");
      assertNotNull(statement.getOldBalance(), "Old Balance (Record 1) should be present");
      assertNotNull(statement.getGlobal(), "Global Record (Record 2.1) should be present");
      assertNotNull(statement.getIndividualTransactions(), "Individual transactions should be present");
      assertNotNull(statement.getNewBalance(), "New Balance (Record 8) should be present");
      assertNotNull(statement.getTrailer(), "Trailer (Record 9) should be present");
      
      // Verify global record
      assertEquals("1", statement.getGlobal().getGlobalisationCode(), "Global record should have globalisation code = 1");
      
      // Verify we have individual transactions
      assertTrue(statement.getIndividualTransactions().size() > 0, "Should have at least one individual transaction");
      
      // Verify individual transaction has all sub-records
      CodaIndividualTransactionRecord firstTx = statement.getIndividualTransactions().get(0);
      assertNotNull(firstTx.getRecord21(), "Transaction should have Record 2.1");
      assertNotNull(firstTx.getRecord22(), "Transaction should have Record 2.2");
      assertNotNull(firstTx.getRecord23(), "Transaction should have Record 2.3");
      assertNotNull(firstTx.getRecord31(), "Transaction should have Record 3.1");
      assertNotNull(firstTx.getRecord32(), "Transaction should have Record 3.2");
      
      // Verify Record 3.2 address parsing (critical test for the bug fix)
      assertEquals("UCAR", firstTx.getRecord23().getCounterpartyAccountName().trim());
      assertEquals("BEKE TUINSTRAT 7", firstTx.getRecord32().getCounterpartyAddress().trim());
      assertEquals("9950", firstTx.getRecord32().getCounterpartyPostalCode().trim());
      assertEquals("WALESCHELT", firstTx.getRecord32().getCounterpartyCity().trim());
   }

   /**
    * Test line length for all record types
    * Every CODA line must be exactly 128 characters
    */
   @Test
   void testAllRecordLinesAre128Characters()
   {
      String[] lines = {
         "0000003032530005        04308988  AZA BELGIUM SA            GKCCBEBB   00404483367 00000                                       2",
         "10024310000017062 EUR0BE   0030000        0000000170022110270225AZA BELGIUM SA            Compte à vue                       024",
         "21000100003010383003291000028  0000000000244120030325201500000REGROUPEMENT DE      6 VCS                           03032502410 0",
         "21000100013010383003291000028  0000000000072480030325601500001102141359004019                                      03032502401 0",
         "2200010001                                                     NOTPROVIDED                        GKCCBEBB                   1 0",
         "2300010001BE84390060159859                     UCAR                                                                          0 1",
         "31000100023010383003291000028  601500001001UCAR                                                                              1 0",
         "3200010002BEKE TUINSTRAT 7                   9950        WALESCHELT                                                          0 0",
         "3200010006BOSDORP              118           9190        ETEBELE                                                             0 0",
         "3200010010RUE DU CHENEUX 7                   4540        AMAY                                                                0 0",
         "8024310000017062 EUR0BE   0030000        0000000170266230030325                                                                0",
         "9               000033000000000000000000000000244120                                                                           1"
      };
      
      for (int i = 0; i < lines.length; i++)
      {
         assertEquals(128, lines[i].length(), 
            "Line " + (i + 1) + " must be exactly 128 characters, but was " + lines[i].length());
      }
   }
}

