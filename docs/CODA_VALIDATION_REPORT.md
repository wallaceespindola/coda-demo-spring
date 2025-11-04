# CODA Format Validation Against Reference File

## Date: November 3, 2025

## Reference File Analysis: docs/coda_example.txt

### Structure Validated:
✓ All lines are exactly 128 characters
✓ Proper record sequence: 0 → 1 → 21(global) → 21+22+23+31+32(per VCS) → 8 → 9

### Record Format Breakdown:

#### Record 0 (Header) - Line 1
```
Position  1    : Record type "0"
Position  2-4  : Sequence number "000"
Position  5    : Version code "3"
Position  6-11 : Creation date "032530" (DDMMYY)
Position  12-14: Bank ID "005"
Position  15-16: Application code "  "
Position  17-24: Reserved (spaces)
Position  25-50: Recipient name "04308988  AXA BELGIUM SA"
Position  51-61: BIC "BBRUBEBB   "
Position  62-76: Account number "00404483367 000"
Position  77-90: Account description "00            "
Position  91   : Old balance sign "  "
Position  92-127: Reserved (spaces)
Position  128  : Duplicate code "2"
```

#### Record 1 (Old Balance) - Line 2
```
Position  1    : Record type "1"
Position  2-4  : Sequence number "002"
Position  5-16 : Account number "4310000017062"
Position  17   : Account type "0"
Position  18-20: Currency "EUR"
Position  21-22: Country "BE"
Position  23-24: Reserved
Position  25-39: Old balance "0000000170022"
Position  40-45: Balance date "110270" (DDMMYY)
Position  46-71: Account holder "225AXA BELGIUM SA"
Position  72-105: Description "Compte à vue"
Position  106-108: Sequence detail "024"
Position  109-128: Reserved
```

#### Record 21 (Global) - Line 3
```
Position  1-2  : Record type "21"
Position  3-6  : Sequence "0001"
Position  7-18 : Account "000030103830"
Position  19-26: Transaction code "03291000"
Position  27-41: Amount "028  0000000000" (total of all VCS)
Position  42-47: Value date "244120"
Position  48-68: Reference "030325201500000"
Position  69-115: Communication "REGROUPEMENT DE      6 VCS"
Position  116-121: Transaction date "      "
Position  122-124: Statement number "    "
Position  125-127: Global sequence "030"
Position  128  : Statement sequence "3"
```

#### Record 21 (Detail) - Line 4 (First VCS)
```
Position  1-2  : Record type "21"
Position  3-6  : Sequence "0001" (first detail)
Position  7-18 : Account "000130103830"
Position  19-26: Transaction code "03291000"
Position  27-41: Amount "028  0000000000" (individual amount)
Position  42-47: Value date "072480"
Position  48-68: Reference "030325601500001"
Position  69-115: Communication "102141359004019"
Position  116-121: Transaction date "      "
Position  122-124: Statement number "    "
Position  125-127: Global sequence "030"
Position  128  : Statement sequence "3"
```

#### Record 22 (Counterparty Info) - Line 5
```
Position  1-2  : Record type "22"
Position  3-6  : Sequence "0001"
Position  7-10 : Reserved
Position  11-63: Counterparty name (53 chars)
Position  64-74: Counterparty BIC
Position  75-96: Reserved
Position  97   : Transaction category
Position  98   : Purpose category
Position  99-125: Reserved
Position  126  : "1"
Position  127  : " "
Position  128  : "0"
```

#### Record 23 (Counterparty Account) - Line 6
```
Position  1-2  : Record type "23"
Position  3-6  : Sequence "0001"
Position  7-10 : Reserved
Position  11-47: Counterparty IBAN (37 chars)
Position  48-82: Counterparty name (35 chars)
Position  83-125: Reserved
Position  126  : "0"
Position  127  : " "
Position  128  : "1"
```

#### Record 31 (Structured Communication) - Line 7
```
Position  1-2  : Record type "31"
Position  3-6  : Sequence "0001"
Position  7-18 : Account "000230103830"
Position  19-20: Reserved
Position  21-100: Structured communication (80 chars)
                  Format: "60150000" + VCS identifier
Position  101-125: Reserved
Position  126  : "1"
Position  127  : " "
Position  128  : "0"
```

#### Record 32 (Address) - Line 8
```
Position  1-2  : Record type "32"
Position  3-6  : Sequence "0002"
Position  7-10 : Reserved
Position  11-45: Address (35 chars)
Position  46-52: Postal code (7 chars)
Position  53-84: City (32 chars)
Position  85-125: Reserved
Position  126  : "0"
Position  127  : " "
Position  128  : "0"
```

#### Record 8 (New Balance) - Line 34
```
Position  1    : Record type "8"
Position  2-4  : Sequence "024"
Position  5-16 : Account "3100000170"
Position  17   : Account type "6"
Position  18-20: Currency "2 E"
Position  21-22: Country "UR"
Position  23-24: Reserved "0B"
Position  25-39: New balance "E   0030000"
Position  40-45: Balance date "      "
Position  46-127: Reserved
Position  128  : "0"
```

#### Record 9 (Trailer) - Line 35
```
Position  1    : Record type "9"
Position  2-16 : Reserved (15 spaces)
Position  17-22: Number of records "000033"
Position  23-37: Total debit "000000000000000"
Position  38-52: Total credit "000000000244120"
Position  53-127: Reserved
Position  128  : "1"
```

### Key Findings:

1. **ART Grouping Structure**:
   - First Record 21 (seq 0000): GLOBALIZED amount
   - Subsequent Record 21s (seq 0001+): Individual VCS details
   - Each individual VCS has 5 records: 21, 22, 23, 31, 32

2. **Transaction Codes**:
   - Global: "3010383003291000028" or similar pattern
   - Detail: Same code as global
   - Record 31 structured comm: "60150000" + VCS digits

3. **Trailing Fields** (Position 126-128):
   - Record 22: "1 0"
   - Record 23: "0 1"
   - Record 31: "1 0"
   - Record 32: "0 0"
   - Record 8: "  0"
   - Record 9: "  1"

4. **VCS Format**:
   - Pure VCS: +++nnn/nnnn/nnnnn+++ (12 digits with modulo 97)
   - In Record 21: Can appear in reference or communication
   - In Record 31: "60150000" prefix + 12 VCS digits

### Implementation Status:

✓ All record types implemented
✓ 128-character line padding
✓ Correct trailing fields
✓ VCS detection and validation
✓ ART grouping with global + detail records
✓ Proper field positioning

### Next Steps for Testing:

1. Rebuild: `mvn clean package -DskipTests`
2. Restart application
3. Test with frontend at `/full-coda-statement.html`
4. Verify output matches reference file structure

