## TrapRange Invoice
We're applying `TrapRange` solution to parse PDF bills and invoices with high accuracy.

Here're some examples:

## Sample 1.

**PDF File**:
![sample 1](https://github.com/thoqbk/traprange/blob/master/_Docs/invoice/sample1.png)

**Result**:
```json
{
  "NO": [
    "1",
    "2"
  ],
  "ACTIVITY": [
    "HOUSE KEEPING SERVICE CHARGES 4 BOYS AT YOUR END FOR THE MONTH OF AUGUST-2015. TOTAL PRESENT DAYS - 97 DAYS 354.83*97 = 34,419/-",
    "Management Fees 10% OF THE BILL FOR THE MONTH OF AUGUST 2015"
  ],
  "QTY": [
    "4",
    "4"
  ],
  "RATE": [
    "8,604.75",
    "860.50"
  ],
  "AMOUNT": [
    "34,419.00",
    "3,442.00"
  ],
  "SUBTOTAL": "37,861.00",
  "TAX": "5,300.54",
  "TOTAL": "43,161.54",
  "ROUND OFF AMOUNT": "0.46",
  "BALANCE DUE": "Rs43,162.00",
  "NET": "37,861.00"
}
```

## Sample 2.

**PDF File:**
![sample 2](https://github.com/thoqbk/traprange/blob/master/_Docs/invoice/sample2.png)

**Result:**
```json
{
  "DATE": [
    "1/TBD",
    "1/TBD",
    "1/TBD",
    "",
    "1/5"
  ],
  "DESCRIPTION": [
    "Car Seat- Labor and Stroage Invoice for January 2015 Labor: Traffic Control Labor: 2 Men 10AM to 6PM",
    "Labor: Traffic Control Labor: 2 Men 10AM to 6PM",
    "Labor: Traffic Control Labor: 2 Men 10AM to 6PM",
    "Garbage Removal",
    "Storage of Pallets- $100/mo per pallet January Pallet Storage 15 Pallets"
  ],
  "QTY": [
    "16",
    "16",
    "16",
    "1",
    "15"
  ],
  "RATE": [
    "$25.00",
    "$25.00",
    "$25.00",
    "$500.00",
    "$100.00"
  ],
  "TOTAL": [
    "$400.00",
    "$400.00",
    "$400.00",
    "$500.00",
    "$1,500.00"
  ],
  "AMOUNT DUE:": "$3,200.00",
}
```

## Sample 3.

**PDF File:**
![sample 3](https://github.com/thoqbk/traprange/blob/master/_Docs/invoice/sample3.png)

**Result:**
```json
{
  "P.O. No.": "",
  "Terms": "Net 30",
  "Project": "CS Ops Implementation",
  "Item": [
    "CS Implementation",
    "CS Implementation",
    "CS Implementation",
    "CS Implementation",
    "CS Implementation",
    "CS Implementation"
  ],
  "Quantity": [
    "0.50",
    "1.50",
    "0.50",
    "3.00",
    "0.75",
    "0.25"
  ],
  "Description": [
    "WORK PERFORMED JANUARY 1-15, 2015. 1/2: create table of contents for WO training documentation",
    "1/7: project staus call, test WO fixes",
    "1/9: test prod mobile wo sso",
    "1/11: WO training doc - capture screen shots",
    "1/13: security setup",
    "1/15: check in call w/Caitlin"
  ],
  "Rate": [
    "125.00",
    "125.00",
    "125.00",
    "125.00",
    "125.00",
    "125.00"
  ],
  "Amount": [
    "62.50",
    "187.50",
    "62.50",
    "375.00",
    "93.75",
    "31.25"
  ],
  "Total": "$812.50"
}
```

## Contact us for more details
[ThoQ Luong](https://github.com/thoqbk/)

Email: thoqbk@gmail.com
