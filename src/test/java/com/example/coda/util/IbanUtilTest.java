package com.example.coda.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Test Belgian IBAN auto-completion and validation
 */
class IbanUtilTest
{
   @Test
   void validBelgianIbanIsRecognized()
   {
      assertTrue(IbanUtil.isValidBelgianIban("BE68539007547034"));
      assertTrue(IbanUtil.isValidBelgianIban("BE68 5390 0754 7034"));
      assertTrue(IbanUtil.isValidBelgianIban("BE84390060159859"));
   }

   @Test
   void invalidBelgianIbanIsRejected()
   {
      assertFalse(IbanUtil.isValidBelgianIban("BE99999999999999"));
      assertFalse(IbanUtil.isValidBelgianIban("FR1234567890123"));
      assertFalse(IbanUtil.isValidBelgianIban("INVALID"));
   }

   @Test
   void calculateCheckDigitsCorrectly()
   {
      // BE68 5390 0754 7034 -> account: 539007547034, check: 68
      assertEquals("68", IbanUtil.calculateBelgianIbanCheckDigits("539007547034"));

      // BE84 3900 6015 9859 -> account: 390060159859, check: 84
      assertEquals("84", IbanUtil.calculateBelgianIbanCheckDigits("390060159859"));
   }

   @Test
   void autoCompleteFromAccountNumber()
   {
      // From 12-digit account number
      String result = IbanUtil.autoCompleteIban("539007547034");
      assertTrue(result.contains("BE68"));
      assertTrue(result.contains("5390 0754 7034"));

      // From formatted account number
      result = IbanUtil.autoCompleteIban("390-0601598-59");
      assertTrue(result.contains("BE84"));
      assertTrue(result.contains("3900 6015 9859"));
   }

   @Test
   void autoCompleteFromPartialIban()
   {
      // Already complete IBAN
      String result = IbanUtil.autoCompleteIban("BE68539007547034");
      assertEquals("BE68 5390 0754 7034", result);

      // Partial IBAN with spaces
      result = IbanUtil.autoCompleteIban("BE84 3900 6015 9859");
      assertEquals("BE84 3900 6015 9859", result);
   }

   @Test
   void formatIbanWithSpaces()
   {
      assertEquals("BE68 5390 0754 7034", IbanUtil.formatIban("BE68539007547034"));
      assertEquals("BE84 3900 6015 9859", IbanUtil.formatIban("BE84390060159859"));
      assertEquals("BE03 7376 2318 0684", IbanUtil.formatIban("BE03737623180684"));
   }

   @Test
   void extractAndCompleteFromCodaField()
   {
      // Test with various CODA field formats
      String result = IbanUtil.extractAndCompleteIban("BE84390060159859");
      assertTrue(result.contains("BE84"));

      result = IbanUtil.extractAndCompleteIban("390060159859");
      assertTrue(result.contains("BE84"));

      result = IbanUtil.extractAndCompleteIban("  BE84390060159859  ");
      assertTrue(result.contains("BE84"));
   }

   @Test
   void recognizeBelgianAccountFormats()
   {
      assertTrue(IbanUtil.isBelgianAccountFormat("BE68539007547034"));
      assertTrue(IbanUtil.isBelgianAccountFormat("539007547034"));
      assertTrue(IbanUtil.isBelgianAccountFormat("539-0075470-34"));
      assertTrue(IbanUtil.isBelgianAccountFormat("BE68 5390 0754 7034"));

      assertFalse(IbanUtil.isBelgianAccountFormat("FR1234567890"));
      assertFalse(IbanUtil.isBelgianAccountFormat("INVALID"));
   }

   @Test
   void handleEdgeCases()
   {
      // Null or empty
      assertNull(IbanUtil.autoCompleteIban(null));
      assertEquals("", IbanUtil.autoCompleteIban(""));

      // Too short
      String result = IbanUtil.autoCompleteIban("BE");
      assertEquals("BE", result);

      // Invalid format
      result = IbanUtil.autoCompleteIban("NOTANIBAN");
      assertEquals("NOTANIBAN", result);
   }

   @Test
   void realWorldExamplesFromPaiTxt()
   {
      // Examples from the Pai.txt file
      String result = IbanUtil.autoCompleteIban("BE84390060159859");
      assertEquals("BE84 3900 6015 9859", result);
      assertTrue(IbanUtil.isValidBelgianIban(result));

      result = IbanUtil.autoCompleteIban("BE03737623180684");
      assertEquals("BE03 7376 2318 0684", result);
      assertTrue(IbanUtil.isValidBelgianIban(result));

      result = IbanUtil.autoCompleteIban("BE63340091652308");
      assertEquals("BE63 3400 9165 2308", result);
      assertTrue(IbanUtil.isValidBelgianIban(result));
   }
}
