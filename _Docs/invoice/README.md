## Extract information from PDF invoice

It's pretty easy to write code to generate a PDF file but quite hard to parse and get back information from it because PDF is complicated. Unfortunately, there're many cases that the only data we have is PDF which needs us to parse and model them before doing further logic.

If the PDF template is various, it's nearly impossible to write one abstract parser to understand and extract all information we need such as Order number, quantity, amount, vendor id. But if the number of templates is not many, yes there's a way to achieve that with PDF box and regex.

In this writing I will explain the way I used to parse PDF file below. Hopefully, it can be applied to yours as well.

Check out my code here [TestInvoice.java](../../src/test/java/com/giaybac/traprange/test/TestInvoice.java)

<img src="sample-invoice.png" height="500px" />

### Extraction requirements

Need to get following information from above file:
- PO number
- Date of the PO
- Vendor
- { Barcode, Description, Quantity } in the table

### Libs

As you may know, PDF stores strings and characters separately with absolute positioning. Meaning even 2 words look like belong to the same string but the raw data we receive can be a list of concrete strings with position. For example, the result when reading the word `Purchase` can be:
```
[{
    { text: "ch", x: 11, y: 4, w: 15, h: 10 },
    { text: "Pur", x: 0, y: 3, w: 10, h: 10},
    { text: "ase", x: 27, y: 4, w: 12, h: 10 }
}]
```
The difficulty is:
- They're not the same `y`
- The order of strings are not the same as they appear in PDF viewers

We need a lib to reorder pieces of words and concatenate them if needed. The lib I use is [PDFLayoutTextStripper](https://github.com/JonathanLink/PDFLayoutTextStripper) which helps to transform PDF to plain text but pretty well keep the original layout. Below is the sample output:

```
                       
                                                                                                *PO-003847945*                                           
                                                                                                                                                         
                                                                                      Page.........................: 1    of    1                        
                                                                                                                                                         
                                                                                                                                                         
                                                                                                                                                         
                                                                                                                                                         
                                                                                                                                                         
                Address...........:     Aeeee  Consumer  Good  Co.(QSC)            Purchase       Order                                                  
                                        P.O.Box 1234                                                                                                     
                                        Dooo,                                      PO-003847945                                                          
                                        ABC                                       TL-00074                                   
                                                                                                                                                         
                Telephone........:                                                 USR\S.Morato         5/10/2020 3:40 PM                                
                Fax...................:                                                                                                                  
                                                                                                                                                         
                                                                                                                                                         
               100225                Aaaaaa  Eeeeee                                 Date...................................: 5/10/2020                   
                                                                                    Expected  DeliveryDate...:  5/10/2020                                
               Phone........:                                                       Attention Information                                                
               Fax.............:                                                                                                                         
               Vendor :    TL-00074                                                                                                                      
               AAAA BBBB CCCCCAAI    W.L.L.                                         Payment  Terms     Current month  plus  60  days                     
                                                                                                                                                         
                                                                                                                                                         
                                                                                                                         Discount                        
          Barcode           Item number     Description                  Quantity   Unit     Unit price       Amount                  Discount           
          5449000165336     304100          CRET ZERO 350ML  PET             5.00 PACK24          54.00        270.00         0.00         0.00          
                                                     350                                                                                                 
          5449000105394     300742          CEEOCE  EOE SOFT DRINKS                                                                                      
                                            1.25LTR                          5.00  PACK6          27.00        135.00         0.00         0.00          
                                                                                                                                                         
                                                1.25                                                                                                                        
(truncated...)
```

### Using regex

After having PDF content in a single string, we can split it into lines and loop through them, using regex to find desired information.

#### Match PO number

Observing that the PO number is the first substring with following format
```
PO-{list of digits}
```
we also see that the PO number stays alone, far from other words so we can make the pattern stronger by adding suffix and prefix spaces. The better pattern should be
```
{at least 5 spaces}PO-{list of digits}{at least 5 spaces}
```

turn this into Java Regex pattern:
```
\\s{5,}(PO\\-\\d+)\\s{5,}
```

#### Match PO date and vendor

PO date is the first substring match following pattern
```
Date{list of dots}{anything but not a digit e.g. space}{1 or 2 digits/1 or 2 digits/4 digits}
```

In Regex:
```
Date\\.+[^\\d]*(\\d+\\/\\d+\\/\\d{4})
```

with a similar observation we have regex for vendor:
```
Vendor\\s*\\:\\s*([^\\s]+)
```

#### Read table content

To read table content while looping through all the lines in PDF file, we need to know following signals:
1. The signal of the table header line to turn reading mode to `reading-table-content`. Also, once we know the header line we know bounds to trap column content.
2. The signal of the first line that not belongs to the table to stop `reading-table-content` mode otherwise it will keep adding wrong content into the table

Check out my code here [TestInvoice.java](../../src/test/java/com/giaybac/traprange/test/TestInvoice.java)

There're some important points in this implementation:
1. I only use some headers not all for header line detection. The reason is because that's strong enough for identifying and the `Discount` header does not stay in the same line as others
2. `Description` is multiple lines cell, its content spreads from the line with barcode and before the next barcode line

With these observations we need to find barcode and use it as the anchor cell for the row.

### A more accurate way to detect PO number

Many values in forms is with their labels e.g. `Po Number: PO-1234422312446`. It will give us higher accuracy if we can find data label and data value together. That's what I applied to find PO Date and Vendor above. But some of value have the label and value are in the vertical alignment. For example:
```
              PO Number

           PO-1234422312446
```
For this layout we can first, detect position of the label, then scan next lines at the same x-range as label with tolerance to find the first non-empty value. That should be the value we're finding. The implementation is as below:
```java
String poNumberLabel = "PO Number";
String poNumber = null;
boolean foundPONumberLabel = false;
int spaceTolerance = 5;

for (String line : lines) {
    // ...
    // detect PO Number
    if (poNumber != null) {
        continue;
    }
    int start = line.indexOf(poNumberLabel);
    if (start >= 0) {
        foundPONumberLabel = true;
    }
    int end = start + poNumberLabel.length();
    if (foundPONumberLabel) {
        poNumber = match(line.substring(start - spaceTolerance, end + spaceTolerance), "po-regex-here");
    }
}
```

### Design for multi-template parsers

If your system has several PDF templates, the suggested pattern to manage all parsers is factory pattern, the design is as below:

#### Interfaces

```java
class ParsedContent {
    // e.g.
    // private string poNumber;
    // private string date;
    // private Row[] rows;
}

interface Parser {
    public ParsedContent parse(String[] lines);
}

interface ParserFactory {
    public Parser get(String[] lines); // detect Parser from its content
}
```

#### Implementation

```java
abstract class AbstractParser implements Parser {
    /**
     * Check and determine if the input lines are acceptable for this parser
     */
    protected boolean isValid(String[] lines);
}

class Template1Parser implements AbstractParser {
    // ...
}

class Template2Parser implements AbstractParser {
    // ...
}

class ParserFactoryImpl implements ParserFactory {
    private Parser[] parsers = new Parser[] {
        new Template1Parser(),
        new Template2Parser()
    };

    public Parser get(String[] lines) {
        Parser retVal = null;
        for (Parser p : this.parsers) {
            if (p.isValid(lines)) {
                if (retVal != null) {
                    throw new Found2ParsersException();
                }
                retVal = p;
            }
        }
        if (retVal == null) {
            throw new ParserNotFoundException();
        }
        return retVal;
    }
}
```

#### Usage:
```java
ParserFactory pf = new ParserFactoryImpl();

// read pdf file and store content in String[] lines
ParsedContent content = pf.get(lines).parse(lines);
```


### Source code

Check out my code here [TestInvoice.java](../../src/test/java/com/giaybac/traprange/test/TestInvoice.java)
