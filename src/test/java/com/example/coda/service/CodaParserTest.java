package com.example.coda.service;

import com.example.coda.model.*;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CODA Parser (CODA text → Java objects)
 */
class CodaParserTest
{
   private final CodaParser parser = new CodaParser();

   @Test
   void parseHeaderRecordCorrectly() throws IOException
   {
      String codaLine = "0000003032530005        04308988  AXA BELGIUM SA            BBRUBEBB   00404483367 00000                                       2";
      
      CodaStatement statement = parser.parse(codaLine);
      
      assertNotNull(statement.getHeader());
      HeaderRecord header = statement.getHeader();
      assertEquals("AXA BELGIUM SA", header.getRecipientName().trim());
      assertEquals("BBRUBEBB", header.getBic().trim());
   }

   @Test
   void parseOldBalanceRecordCorrectly() throws IOException
   {
      String coda = "0000003032530005        04308988  AXA BELGIUM SA            BBRUBEBB   00404483367 00000                                       2\n" +
                    "10024310000017062 EUR0BE   0030000        0000000170022110270225AXA BELGIUM SA            Compte à vue                       024";
      
      CodaStatement statement = parser.parse(coda);
      
      assertNotNull(statement.getOldBalance());
      OldBalanceRecord oldBalance = statement.getOldBalance();
      assertEquals("EUR", oldBalance.getCurrencyCode().trim());
      assertEquals(new BigDecimal("1700.22"), oldBalance.getOldBalance());
   }

   @Test
   void parseMovementRecordCorrectly() throws IOException
   {
      String coda = "0000003032530005        04308988  AXA BELGIUM SA            BBRUBEBB   00404483367 00000                                       2\n" +
                    "10024310000017062 EUR0BE   0030000        0000000170022110270225AXA BELGIUM SA            Compte à vue                       024\n" +
                    "21000100003010383003291000028  0000000000244120030325201500000REGROUPEMENT DE      6 VCS                           03032502410 0\n" +
                    "2200010001                                                     NOTPROVIDED                        BBRUBEBB                   1 0\n" +
                    "2300010001BE84390060159859                     UCAR                                                                          0 1";
      
      CodaStatement statement = parser.parse(coda);
      
      assertNotNull(statement.getMovements());
      assertFalse(statement.getMovements().isEmpty());
      
      MovementRecord movement = statement.getMovements().get(0);
      assertEquals(new BigDecimal("2441.20"), movement.getAmount());
      assertEquals("BE84 3900 6015 9859", movement.getCounterpartyAccount());
      assertEquals("UCAR", movement.getCounterpartyAccountName().trim());
   }

   @Test
   void parseCounterpartyAddressCorrectly() throws IOException
   {
      String coda = "0000003032530005        04308988  AXA BELGIUM SA            BBRUBEBB   00404483367 00000                                       2\n" +
                    "10024310000017062 EUR0BE   0030000        0000000170022110270225AXA BELGIUM SA            Compte à vue                       024\n" +
                    "21000100003010383003291000028  0000000000244120030325201500000REGROUPEMENT DE      6 VCS                           03032502410 0\n" +
                    "2200010001                                                     NOTPROVIDED                        BBRUBEBB                   1 0\n" +
                    "2300010001BE84390060159859                     UCAR                                                                          0 1\n" +
                    "31000100023010383003291000028  601500001001UCAR                                                                              1 0\n" +
                    "3200010002BEKE TUINWIJK 35                   9950        WAARSCHOOT                                                          0 0";
      
      CodaStatement statement = parser.parse(coda);
      
      MovementRecord movement = statement.getMovements().get(0);
      assertEquals("BEKE TUINWIJK 35", movement.getCounterpartyAddress().trim());
      assertEquals("9950", movement.getCounterpartyPostalCode().trim());
      assertEquals("WAARSCHOOT", movement.getCounterpartyCity().trim());
   }

   @Test
   void parseNewBalanceRecordCorrectly() throws IOException
   {
      String coda = "0000003032530005        04308988  AXA BELGIUM SA            BBRUBEBB   00404483367 00000                                       2\n" +
                    "10024310000017062 EUR0BE   0030000        0000000170022110270225AXA BELGIUM SA            Compte à vue                       024\n" +
                    "8024310000017062 EUR0BE   0030000        0000000170266230030325                                                                0";
      
      CodaStatement statement = parser.parse(coda);
      
      assertNotNull(statement.getNewBalance());
      NewBalanceRecord newBalance = statement.getNewBalance();
      assertEquals("EUR", newBalance.getCurrencyCode().trim());
      assertEquals(new BigDecimal("1702.66"), newBalance.getNewBalance());
   }

   @Test
   void parseTrailerRecordCorrectly() throws IOException
   {
      String coda = "0000003032530005        04308988  AXA BELGIUM SA            BBRUBEBB   00404483367 00000                                       2\n" +
                    "10024310000017062 EUR0BE   0030000        0000000170022110270225AXA BELGIUM SA            Compte à vue                       024\n" +
                    "9               000033000000000000000000000000244120                                                                           1";
      
      CodaStatement statement = parser.parse(coda);
      
      assertNotNull(statement.getTrailer());
      TrailerRecord trailer = statement.getTrailer();
      assertEquals(33, trailer.getNumberOfRecords());
      assertEquals(new BigDecimal("2441.20"), trailer.getTotalCredit());
      assertEquals(BigDecimal.ZERO, trailer.getTotalDebit());
   }

   @Test
   void parseMultipleMovements() throws IOException
   {
      String coda = "0000003032530005        04308988  AXA BELGIUM SA            BBRUBEBB   00404483367 00000                                       2\n" +
                    "10024310000017062 EUR0BE   0030000        0000000170022110270225AXA BELGIUM SA            Compte à vue                       024\n" +
                    "21000100003010383003291000028  0000000000244120030325201500000REGROUPEMENT DE      6 VCS                           03032502410 0\n" +
                    "21000100013010383003291000028  0000000000072480030325601500001102141359004019                                      03032502401 0\n" +
                    "2200010001                                                     NOTPROVIDED                        BBRUBEBB                   1 0\n" +
                    "2300010001BE84390060159859                     UCAR                                                                          0 1";
      
      CodaStatement statement = parser.parse(coda);
      
      assertEquals(2, statement.getMovements().size());
      assertEquals(new BigDecimal("2441.20"), statement.getMovements().get(0).getAmount());
      assertEquals(new BigDecimal("724.80"), statement.getMovements().get(1).getAmount());
   }

   @Test
   void parseIbanAutoCompletion() throws IOException
   {
      String coda = "0000003032530005        04308988  AXA BELGIUM SA            BBRUBEBB   00404483367 00000                                       2\n" +
                    "10024310000017062 EUR0BE   0030000        0000000170022110270225AXA BELGIUM SA            Compte à vue                       024\n" +
                    "21000100003010383003291000028  0000000000244120030325201500000REGROUPEMENT DE      6 VCS                           03032502410 0\n" +
                    "2200010001                                                     NOTPROVIDED                        BBRUBEBB                   1 0\n" +
                    "2300010001BE84390060159859                     UCAR                                                                          0 1";
      
      CodaStatement statement = parser.parse(coda);
      
      MovementRecord movement = statement.getMovements().get(0);
      // IBAN should be auto-completed and formatted
      assertTrue(movement.getCounterpartyAccount().contains("BE84"));
      assertTrue(movement.getCounterpartyAccount().contains(" ")); // Should have spaces
   }

   @Test
   void parseEmptyLinesGracefully() throws IOException
   {
      String coda = "0000003032530005        04308988  AXA BELGIUM SA            BBRUBEBB   00404483367 00000                                       2\n" +
                    "\n" +
                    "10024310000017062 EUR0BE   0030000        0000000170022110270225AXA BELGIUM SA            Compte à vue                       024\n" +
                    "\n";
      
      CodaStatement statement = parser.parse(coda);
      
      assertNotNull(statement.getHeader());
      assertNotNull(statement.getOldBalance());
   }

   @Test
   void parseAmountsInCentsCorrectly() throws IOException
   {
      String coda = "0000003032530005        04308988  AXA BELGIUM SA            BBRUBEBB   00404483367 00000                                       2\n" +
                    "10024310000017062 EUR0BE   0030000        0000000012500000270225AXA BELGIUM SA            Compte à vue                       024";
      
      CodaStatement statement = parser.parse(coda);
      
      // 12500000 cents = 125000.00 EUR
      assertEquals(new BigDecimal("125000.00"), statement.getOldBalance().getOldBalance());
   }

   @Test
   void parseZeroAmountsCorrectly() throws IOException
   {
      String coda = "0000003032530005        04308988  AXA BELGIUM SA            BBRUBEBB   00404483367 00000                                       2\n" +
                    "10024310000017062 EUR0BE   0030000        0000000000000000270225AXA BELGIUM SA            Compte à vue                       024";
      
      CodaStatement statement = parser.parse(coda);
      
      assertEquals(BigDecimal.ZERO, statement.getOldBalance().getOldBalance());
   }

   @Test
   void parseStructuredCommunication() throws IOException
   {
      String coda = "0000003032530005        04308988  AXA BELGIUM SA            BBRUBEBB   00404483367 00000                                       2\n" +
                    "10024310000017062 EUR0BE   0030000        0000000170022110270225AXA BELGIUM SA            Compte à vue                       024\n" +
                    "21000100003010383003291000028  0000000000244120030325201500000REGROUPEMENT DE      6 VCS                           03032502410 0\n" +
                    "31000100023010383003291000028  601500001001UCAR                                                                              1 0";
      
      CodaStatement statement = parser.parse(coda);
      
      MovementRecord movement = statement.getMovements().get(0);
      assertNotNull(movement.getStructuredCommunication());
      assertTrue(movement.getStructuredCommunication().contains("UCAR"));
   }

   @Test
   void parseCompleteStatementFromPaiTxt() throws IOException
   {
      String coda = "0000003032530005        04308988  AXA BELGIUM SA            BBRUBEBB   00404483367 00000                                       2\n" +
                    "10024310000017062 EUR0BE   0030000        0000000170022110270225AXA BELGIUM SA            Compte à vue                       024\n" +
                    "21000100003010383003291000028  0000000000244120030325201500000REGROUPEMENT DE      6 VCS                           03032502410 0\n" +
                    "21000100013010383003291000028  0000000000072480030325601500001102141359004019                                      03032502401 0\n" +
                    "2200010001                                                     NOTPROVIDED                        BBRUBEBB                   1 0\n" +
                    "2300010001BE84390060159859                     UCAR                                                                          0 1\n" +
                    "31000100023010383003291000028  601500001001UCAR                                                                              1 0\n" +
                    "3200010002BEKE TUINWIJK 35                   9950        WAARSCHOOT                                                          0 0\n" +
                    "8024310000017062 EUR0BE   0030000        0000000170266230030325                                                                0\n" +
                    "9               000033000000000000000000000000244120                                                                           1";
      
      CodaStatement statement = parser.parse(coda);
      
      // Verify all components
      assertNotNull(statement.getHeader());
      assertNotNull(statement.getOldBalance());
      assertNotNull(statement.getMovements());
      assertNotNull(statement.getNewBalance());
      assertNotNull(statement.getTrailer());
      
      // Verify counts
      assertEquals(2, statement.getMovements().size());
      assertEquals(33, statement.getTrailer().getNumberOfRecords());
      
      // Verify balance calculation
      assertEquals(new BigDecimal("1700.22"), statement.getOldBalance().getOldBalance());
      assertEquals(new BigDecimal("1702.66"), statement.getNewBalance().getNewBalance());
   }
}
