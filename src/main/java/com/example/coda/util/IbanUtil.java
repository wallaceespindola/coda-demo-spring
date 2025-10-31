package com.example.coda.util;

import java.util.regex.Pattern;

/**
 * Utility class for Belgian IBAN (BIBAN) validation and auto-completion
 */
public class IbanUtil
{
   private static final Pattern BELGIAN_IBAN_PATTERN = Pattern.compile("^BE\\d{14}$");
   private static final Pattern PARTIAL_BELGIAN_IBAN = Pattern.compile("^BE\\d{2,14}$");
   private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^\\d{3}-?\\d{7}-?\\d{2}$");

   /**
    * Check if the string is a valid Belgian IBAN
    */
   public static boolean isValidBelgianIban(String iban)
   {
      if (iban == null)
      {
         return false;
      }
      String cleaned = iban.replaceAll("\\s+", "");
      return BELGIAN_IBAN_PATTERN.matcher(cleaned).matches() && validateIbanChecksum(cleaned);
   }

   /**
    * Auto-complete Belgian IBAN from partial account number
    * Supports formats:
    * - BE + partial digits (e.g., "BE84" -> tries to complete)
    * - Account number format: 390-0601598-59 or 390060159859
    * - Partial IBAN: BE84390060159859
    */
   public static String autoCompleteIban(String input)
   {
      if (input == null || input.trim().isEmpty())
      {
         return input;
      }

      String cleaned = input.replaceAll("\\s+", "").toUpperCase();

      // Already a valid Belgian IBAN
      if (isValidBelgianIban(cleaned))
      {
         return formatIban(cleaned);
      }

      // Partial Belgian IBAN (BE + some digits)
      if (cleaned.startsWith("BE") && cleaned.length() >= 4)
      {
         // If it's a partial IBAN, try to extract account number and regenerate
         if (cleaned.length() > 4)
         {
            String accountPart = cleaned.substring(4);
            // If we have at least 12 digits after BE, we can try to generate check digits
            if (accountPart.length() == 12)
            {
               String checkDigits = calculateBelgianIbanCheckDigits(accountPart);
               String fullIban = "BE" + checkDigits + accountPart;
               return formatIban(fullIban);
            }
         }
         return formatIban(cleaned);
      }

      // Belgian account number format: XXX-XXXXXXX-XX or XXXXXXXXXXXX
      if (ACCOUNT_NUMBER_PATTERN.matcher(cleaned).matches() || 
          (cleaned.matches("^\\d{12}$")))
      {
         String accountNumber = cleaned.replaceAll("-", "");
         if (accountNumber.length() == 12)
         {
            String checkDigits = calculateBelgianIbanCheckDigits(accountNumber);
            String fullIban = "BE" + checkDigits + accountNumber;
            return formatIban(fullIban);
         }
      }

      // Try to extract 12 digits from the input
      String digitsOnly = cleaned.replaceAll("[^0-9]", "");
      if (digitsOnly.length() == 12)
      {
         String checkDigits = calculateBelgianIbanCheckDigits(digitsOnly);
         String fullIban = "BE" + checkDigits + digitsOnly;
         return formatIban(fullIban);
      }
      else if (digitsOnly.length() == 14 && !cleaned.startsWith("BE"))
      {
         // Might be check digits + account number
         String fullIban = "BE" + digitsOnly;
         if (isValidBelgianIban(fullIban))
         {
            return formatIban(fullIban);
         }
      }

      // Return original if we can't auto-complete
      return input;
   }

   /**
    * Calculate Belgian IBAN check digits
    * Algorithm: 98 - (mod97 of "account_number + country_code_numeric + 00")
    * For Belgium: BE = 1114
    */
   public static String calculateBelgianIbanCheckDigits(String accountNumber)
   {
      if (accountNumber == null || accountNumber.length() != 12)
      {
         throw new IllegalArgumentException("Account number must be exactly 12 digits");
      }

      try
      {
         // For IBAN calculation: account_number + "BE" (1114) + "00"
         String numericString = accountNumber + "1114" + "00";
         
         // Calculate mod 97 using string arithmetic for large numbers
         long mod = 0;
         for (int i = 0; i < numericString.length(); i++)
         {
            mod = (mod * 10 + Character.getNumericValue(numericString.charAt(i))) % 97;
         }
         
         int checkDigits = 98 - (int) mod;
         
         return String.format("%02d", checkDigits);
      }
      catch (NumberFormatException e)
      {
         throw new IllegalArgumentException("Invalid account number format", e);
      }
   }

   /**
    * Format IBAN with spaces for readability: BE68 5390 0754 7034
    */
   public static String formatIban(String iban)
   {
      if (iban == null)
      {
         return null;
      }

      String cleaned = iban.replaceAll("\\s+", "").toUpperCase();
      
      if (cleaned.length() < 4)
      {
         return cleaned;
      }

      // Format as: BE68 5390 0754 7034
      StringBuilder formatted = new StringBuilder();
      for (int i = 0; i < cleaned.length(); i++)
      {
         if (i > 0 && i % 4 == 0)
         {
            formatted.append(' ');
         }
         formatted.append(cleaned.charAt(i));
      }

      return formatted.toString();
   }

   /**
    * Validate IBAN checksum using mod-97 algorithm
    */
   private static boolean validateIbanChecksum(String iban)
   {
      if (iban == null || iban.length() < 4)
      {
         return false;
      }

      try
      {
         // Move first 4 characters to end
         String rearranged = iban.substring(4) + iban.substring(0, 4);
         
         // Replace letters with numbers (A=10, B=11, ..., Z=35)
         StringBuilder numeric = new StringBuilder();
         for (char c : rearranged.toCharArray())
         {
            if (Character.isDigit(c))
            {
               numeric.append(c);
            }
            else if (Character.isLetter(c))
            {
               numeric.append(Character.getNumericValue(c));
            }
         }

         // Calculate mod 97
         String numericStr = numeric.toString();
         long remainder = 0;
         for (int i = 0; i < numericStr.length(); i++)
         {
            remainder = (remainder * 10 + Character.getNumericValue(numericStr.charAt(i))) % 97;
         }

         return remainder == 1;
      }
      catch (Exception e)
      {
         return false;
      }
   }

   /**
    * Extract IBAN from CODA field (handles various formats)
    */
   public static String extractAndCompleteIban(String codaField)
   {
      if (codaField == null || codaField.trim().isEmpty())
      {
         return codaField;
      }

      String trimmed = codaField.trim();
      
      // Try to auto-complete
      String completed = autoCompleteIban(trimmed);
      
      // If auto-completion worked and it's valid, return formatted
      if (isValidBelgianIban(completed))
      {
         return formatIban(completed);
      }

      // Return original if we couldn't complete it
      return trimmed;
   }

   /**
    * Check if input looks like it could be a Belgian account/IBAN
    */
   public static boolean isBelgianAccountFormat(String input)
   {
      if (input == null)
      {
         return false;
      }

      String cleaned = input.replaceAll("\\s+", "").toUpperCase();
      
      return cleaned.startsWith("BE") || 
             ACCOUNT_NUMBER_PATTERN.matcher(cleaned).matches() ||
             cleaned.matches("^\\d{12}$") ||
             cleaned.matches("^\\d{14}$");
   }
}
