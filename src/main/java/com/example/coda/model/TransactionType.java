package com.example.coda.model;

public enum TransactionType
{
   CREDIT("C"), DEBIT("D");

   private final String code;

   TransactionType(String code)
   {
      this.code = code;
   }

   public String getCode()
   {
      return code;
   }

}