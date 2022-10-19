/*
Name: Alex James Waddell
Student Number: C3330987
Description: the scanner class continuously receives requests for tokens and returns the next token
 */

import java.util.ArrayList;

public class Scanner {

    // array to print tokens
    final String[] TPRINT = {
            "TTEOF ",
            "TCD22 ", "TCONS ", "TTYPS ", "TTDEF ",
            "TARRS ", "TMAIN ",
            "TBEGN ", "TTEND ", "TARAY ", "TTTOF ",
            "TFUNC ", "TVOID ",
            "TCNST ", "TINTG ", "TFLOT ", "TBOOL ",
            "TTFOR ", "TREPT ",
            "TUNTL ", "TIFTH ", "TELSE ", "TELIF ",
            "TINPT ", "TPRNT ",
            "TPRLN ", "TRETN ", "TNOTT ",
            "TTAND ", "TTTOR ", "TTXOR ",
            "TTRUE ", "TFALS ", "TCOMA ",
            "TLBRK ", "TRBRK ", "TLPAR ",
            "TRPAR ", "TEQUL ", "TPLUS ",
            "TMINS ", "TSTAR ", "TDIVD ",
            "TPERC ", "TCART ", "TLESS ",
            "TGRTR ", "TCOLN ", "TSEMI ",
            "TDOTT ", "TLEQL ", "TGEQL ",
            "TNEQL ", "TEQEQ ", "TPLEQ ",
            "TMNEQ ", "TSTEQ ", "TDVEQ ", "TIDEN ",
            "TILIT ", "TFLIT ",
            "TSTRG ", "TUNDF "};

    ArrayList<Character> inputBuffer = new ArrayList<>();// responsible for storing inputted characters
    ArrayList<Token> tokenList = new ArrayList<>();  // list of tokens
    ArrayList<String> errors = new ArrayList<>(); // list of errors

    private final ArrayList<Character> program; // the source code in a char array format
    int position, column, row, stringLength, startCol, startRow; // startcol and startrow record where the last token began
    boolean endOfFile, tokenComplete;
    Token tempToken;

    // constructor
    public Scanner(ArrayList<Character> program_) {
        program = new ArrayList<>(program_);
        position = 0;
        column = 0;
        row = 0;
        endOfFile = false;
        stringLength = 0;
        tokenComplete = false;
    }

    public Token gettoken() {
        // resets the token
        tempToken = null;

        // ensures that if it consumes something like a space or tab it will then go ahead and grab the next column
        while (tempToken == null && eof()) {

            tokenComplete = false;
            // sets the start col and row for the error messages
            startCol = column+1;
            startRow = row+1;

            // if a char was scanned and not apart of the previous token this prevents it from getting a new one
            if (inputBuffer.size() == 0) {
                increment();
                // consumes any leading spaces, tabs or new lines
                while ((inputBuffer.get(0) == ' ' || inputBuffer.get(0) == '\t' || inputBuffer.get(0) == '\n') && eof()) {
                    inputBuffer.clear();
                    increment();
                }

            }


            // handles identifiers and keywords
            if (Character.isLetter(inputBuffer.get(0))) { // either a keyword or identifier

                if (inputBuffer.size() == 1) {
                    increment();
                }

                while (!tokenComplete) {

                    // case for if the identifier is complete i.e. there is a space,tab or new line
                    if (spaceCheck()) {
                        // removes the tab, space or new line character and finds the correct keyword if not lists it as an identifier
                        if (inputBuffer.get(inputBuffer.size() - 1) == ' ') {
                            column--;
                            inputBuffer.remove(inputBuffer.size() - 1);
                            reservedWords();
                            column++;
                        } else if (inputBuffer.get(inputBuffer.size() - 1) == '\t') {
                            column -= 4;
                            inputBuffer.remove(inputBuffer.size() - 1);
                            reservedWords();
                            column += 4;
                        } else if (inputBuffer.get(inputBuffer.size() - 1) == '\n') {
                            row--;
                            inputBuffer.remove(inputBuffer.size() - 1);
                            reservedWords();
                            row++;
                            column = 0;
                        }


                        // case for reading in anything that isnt an integer or letter in the identifier/keyword
                    } else if ((!Character.isLetter(inputBuffer.get(inputBuffer.size() - 1)) && !(Character.isDigit(inputBuffer.get(inputBuffer.size() - 1))))) {
                        // holds the incorrect character while the token is made and then adds it back into the input buffer
                        char tempchar = inputBuffer.remove(inputBuffer.size() - 1);
                        reservedWords();
                        // adds it back
                        inputBuffer.add(tempchar);
                    }
                    if (!tokenComplete) {
                        increment();
                    }
                }
            } else if (Character.isDigit(inputBuffer.get(0))) { // case for either a digit or floating point
                if (inputBuffer.size() == 1) {
                    increment();
                }
                while (!tokenComplete) {
                    // handles digits only
                    // keeps integers below 9 x 10^18 characters
                    if (inputBuffer.size() > 20) {
                        // keeps grabbing the next integer to report it as an error
                        while (Character.isDigit(inputBuffer.get(inputBuffer.size()-1))) {
                            increment();
                        }
                        errors.add("error: integer value too large on line:"+ startRow+",col: "+startCol);
                        createTokenS1(62);
                    }
                    // case for if the integer is complete i.e. there is a space,tab or new line
                    else if (spaceCheck()) {
                        // removes the tab, space or new line character and creates the integer token
                        if (inputBuffer.get(inputBuffer.size() - 1) == ' ') {
                            column--;
                            inputBuffer.remove(inputBuffer.size() - 1);
                            createToken(59);
                            column++;
                        } else if (inputBuffer.get(inputBuffer.size() - 1) == '\t') {
                            column -= 4;
                            inputBuffer.remove(inputBuffer.size() - 1);
                            createToken(59);
                            column += 4;
                        } else if (inputBuffer.get(inputBuffer.size() - 1) == '\n') {
                            row--;
                            inputBuffer.remove(inputBuffer.size() - 1);
                            createToken(59);
                            row++;
                            column = 0;
                        }

                        // handles any character that isnt a number or .
                        // i.e. 123a would return 123 and leaving a in the input buffer for the next token
                    } else if (!Character.isDigit(inputBuffer.get(inputBuffer.size() - 1)) && !((inputBuffer.get(inputBuffer.size() - 1)) == '.')) {
                        createTokenS1(59);

                        // handles floating points
                    } else if ((inputBuffer.get(inputBuffer.size() - 1)) == '.') {
                        while (!tokenComplete) {
                            increment();
                            // handles digits only
                            // keeps significant figures below 9 x 10^18 characters
                            if (inputBuffer.size() > 20) {
                                while (Character.isDigit(inputBuffer.get(inputBuffer.size()-1))) {
                                    increment();
                                }
                                errors.add("error: float value too large on line:"+ startRow+",col: "+startCol);
                                createTokenS1(62);
                            } else if (spaceCheck()) {
                                // removes the tab, space or new line character and creates the float token
                                if (inputBuffer.get(inputBuffer.size() - 1) == ' ') {
                                    column--;
                                    inputBuffer.remove(inputBuffer.size() - 1);
                                    createToken(60);
                                    column++;
                                } else if (inputBuffer.get(inputBuffer.size() - 1) == '\t') {
                                    column -= 4;
                                    inputBuffer.remove(inputBuffer.size() - 1);
                                    createToken(60);
                                    column += 4;
                                } else if (inputBuffer.get(inputBuffer.size() - 1) == '\n') {
                                    row--;
                                    inputBuffer.remove(inputBuffer.size() - 1);
                                    createToken(60);
                                    row++;
                                    column = 0;
                                }

                                // handles non digit characters in a float number
                            } else if (!Character.isDigit(inputBuffer.get(inputBuffer.size() - 1))) {
                                // edge case for if the dot has no integer after it, so the dot isnt consumed
                                if (inputBuffer.get(inputBuffer.size() - 2) == '.') {
                                    createTokenS2(59);
                                } else {
                                    // case for if the float is made but has a none digit in it
                                    createTokenS1(60);
                                }

                            }
                        }
                    }
                    if (!tokenComplete) {
                        increment();
                    }
                }

                // operators and delimiters
            } else if (inputBuffer.get(0) == ',' || inputBuffer.get(0) == '[' || inputBuffer.get(0) == ']' || inputBuffer.get(0) == '(' || inputBuffer.get(0) == ')'
                    || inputBuffer.get(0) == '=' || inputBuffer.get(0) == '+' || inputBuffer.get(0) == '-' || inputBuffer.get(0) == '*' || inputBuffer.get(0) == '%'
                    || inputBuffer.get(0) == '^' || inputBuffer.get(0) == '<' || inputBuffer.get(0) == '>' || inputBuffer.get(0) == ':' || inputBuffer.get(0) == ';'
                    || inputBuffer.get(0) == '.' || inputBuffer.get(0) == '/') { // beginning of a comment or combined significant lexical or standalone lexical

                if (inputBuffer.size() == 1) {
                    increment();
                }
                while (!tokenComplete) {

                    // case for if the operator is complete i.e. there is a space,tab or new line
                    if (spaceCheck()) {
                        // removes the tab, space or new line character and creates the operator token
                        if (inputBuffer.get(inputBuffer.size() - 1) == ' ') {
                            column--;
                            inputBuffer.remove(inputBuffer.size() - 1);
                            reservedSymbols();
                            column++;
                        } else if (inputBuffer.get(inputBuffer.size() - 1) == '\t') {
                            column -= 4;
                            inputBuffer.remove(inputBuffer.size() - 1);
                            reservedSymbols();
                            column += 4;
                        } else if (inputBuffer.get(inputBuffer.size() - 1) == '\n') {
                            row--;
                            inputBuffer.remove(inputBuffer.size() - 1);
                            reservedSymbols();
                            row++;
                            column = 0;
                        }
                    } else {
                        // case for reading the composite operators
                        if ((charToString(inputBuffer).equalsIgnoreCase("<=")) || (charToString(inputBuffer).equalsIgnoreCase(">="))
                                || (charToString(inputBuffer).equalsIgnoreCase("==")) || (charToString(inputBuffer).equalsIgnoreCase("+="))
                                || (charToString(inputBuffer).equalsIgnoreCase("-=")) || (charToString(inputBuffer).equalsIgnoreCase("*="))
                                || (charToString(inputBuffer).equalsIgnoreCase("/="))) {
                            reservedSymbols();
                        } else if ((charToString(inputBuffer).equalsIgnoreCase("/-"))) { // case for single line comment \--
                            // grab next one and check
                            increment();
                            // if it is a single line comment continue consuming until new line
                            if ((charToString(inputBuffer).equalsIgnoreCase("/--"))) {
                                while (!tokenComplete) {
                                    if (inputBuffer.get(inputBuffer.size() - 1) == '\n') {
                                        inputBuffer.clear();
                                        tokenComplete = true;
                                    } else {
                                        increment();
                                    }
                                }
                            } else { // deals with cases like /-/
                                // holds the incorrect character
                                char tempchar = inputBuffer.remove(inputBuffer.size() - 1);
                                char tempchar2 = inputBuffer.remove(inputBuffer.size() - 1);
                                reservedSymbols();
                                // adds it back
                                inputBuffer.add(tempchar2);
                                inputBuffer.add(tempchar);
                            }
                        } else if ((charToString(inputBuffer).equalsIgnoreCase("/*"))) { //case for multi line comment \**
                            // grab next one and check
                            increment();
                            // if it is a multi line comment continue until **/ is seen and size is >=6 so /**/ isnt valid
                            if ((charToString(inputBuffer).equalsIgnoreCase("/**"))) {
                                while (!tokenComplete && eof()) {
                                    if (charToString(inputBuffer).contains("**/") && inputBuffer.size() >= 6) {
                                        inputBuffer.clear();
                                        tokenComplete = true;
                                    } else {
                                        increment();
                                    }
                                }
                                // edge case to handle if multi line comment is never closed
                                if (!eof() && !charToString(inputBuffer).substring(inputBuffer.size() - 2, inputBuffer.size() - 1).equalsIgnoreCase("**/")) {
                                    errors.add("error: no closing quotations for multi line comment on line:" + startRow + ",col: " + startCol);
                                    createToken(62);
                                }
                            } else { // deals with cases like /*/
                                // holds the incorrect character
                                char tempchar = inputBuffer.remove(inputBuffer.size() - 1);
                                char tempchar2 = inputBuffer.remove(inputBuffer.size() - 1);
                                reservedSymbols();
                                // adds it back
                                inputBuffer.add(tempchar2);
                                inputBuffer.add(tempchar);
                            }
                        } else { // handles case where there is a / but no - or * following it
                            char tempchar = inputBuffer.remove(inputBuffer.size() - 1);
                            reservedSymbols();
                            inputBuffer.add(tempchar);
                        }
                    }
                    if (!tokenComplete) {
                        increment();
                    }
                }
            } else if (inputBuffer.get(0) == '"') { // beginning of a string literal
                while (!tokenComplete) {
                    // if next one is a newline char or end of file make an error token
                    if (inputBuffer.get(inputBuffer.size() - 1) == '\n' || !eof()) {
                        errors.add("error: no closing quotations for string literal on line:"+ startRow+",col: "+startCol);
                        // removes the \n if it exists
                        if (inputBuffer.get(inputBuffer.size() - 1) == '\n') {
                            inputBuffer.remove(inputBuffer.size() - 1);
                        }
                        createToken(62);
                    } else if (inputBuffer.get(inputBuffer.size() - 1) == '"' && inputBuffer.size() >= 2) { // if next one is " make token
                        createToken(61);
                    } else {
                        increment();
                    }
                    // loop until u encounter either a new line or a closing "
                }


            } else if (spaceCheck()) { // handles spaces, likely redundant given above code
                inputBuffer.remove(inputBuffer.size() - 1);
                // handles edge case with ! as by itself its not within the CD22 language but with != it is
            } else if (inputBuffer.get(inputBuffer.size() - 1) == '!' || charToString(inputBuffer).equals("!=")) {
                if (inputBuffer.size() == 1) {
                    increment();
                }
                if (charToString(inputBuffer).equals("!=")) {
                    reservedSymbols();
                } else {
                    // keep grabbing tokens until a valid token within the CD22 language is present
                    while (!tokenComplete) {
                        if (symbolCheck()) {
                            // if != is within the string return the error and put != into the input buffer
                            if (charToString(inputBuffer).contains("!=")) {
                                errors.add("error: invalid CD22 characters on line:"+ startRow+",col: "+startCol);
                                createTokenS2(62);
                            } else { // a valid char within the CD22 language has been read in, store it for later and print error
                                errors.add("error: invalid CD22 characters on line:"+ startRow+",col: "+startCol);
                                createTokenS1(62);
                            }

                        }
                        if (!tokenComplete) {
                            increment();
                        }
                    }
                }
            } else { // unknown char entry, not usable in cd22 language
                if (inputBuffer.size() == 1) {
                    increment();
                }
                // keeps grabbing incorrect tokens until a valid one is present
                while (!tokenComplete) {
                    if (symbolCheck()) {
                        errors.add("error: invalid CD22 characters on line:"+ startRow+",col: "+startCol);
                        createTokenS1(62);
                    }
                    if (!tokenComplete) {
                        increment();
                    }
                }
            }
        }
        return tempToken;
    }

    // method to list all lexical errors encountered during scanning
    public void printErrors() {
        System.out.print("\n");
        for (int i = 0; i < errors.size(); i++) {
            System.out.println(errors.get(i));
        }
    }

    public boolean errorsDetected() {
        if (errors.size() == 0) {
            return false;
        } else {
            return true;
        }
    }
    // prints the next token, handles printing requirements for lexical errors and normal tokens
    public void printtoken(Token token_) {
        if (token_.getTokenNo() == 62) {
            if (stringLength != 0) {
                System.out.print("\n");
                stringLength = 0;
            }
            stringLength += 6;
            System.out.println(TPRINT[token_.getTokenNo()]);
            System.out.print("lexical error ");


        } else {
            if (stringLength > 60) {
                System.out.print('\n');
                stringLength = 0;
            }
            stringLength += 6;
            System.out.print(TPRINT[token_.getTokenNo()]);
        }
        print(token_);
    }

    // helper function for printing tokens
    private void print(Token token_) {
        if (token_.getLexeme() != null) {
            stringLength += token_.getLexeme().length() + 1;
            System.out.print(token_.getLexeme() + " ");
            if (token_.getTokenNo() == 62) {
                System.out.print("\n");
                stringLength = 0;
            }
        }

    }

    // creates and returns the eof token
    public Token endFile() {
        tempToken = new Token(0, null, row, column);
        tokenList.add(tempToken);
        return tempToken;
    }

    // returns false if it is the eof and true if it isnt, in hindsight these should be the other way around
    public boolean eof() {
        if (position == program.size()) {
            return false;
        } else {
            return true;
        }
    }

    private void createToken(int num) { // creates a token with its corresponding lexeme
        tempToken = new Token(num, stringBuiler(inputBuffer), row, column);
        tokenList.add(tempToken);
        inputBuffer.clear();
        tokenComplete = true;
    }

    private void createTokenS1(int num) { // creates a token with its corresponding lexeme but stores the last char
        char tempchar = inputBuffer.remove(inputBuffer.size() - 1);
        createToken(num);
        inputBuffer.add(tempchar);
    }

    private void createTokenS2(int num) { // creates a token with its corresponding lexeme but stores the 2 last chars
        char tempchar = inputBuffer.remove(inputBuffer.size() - 1);
        char tempchar2 = inputBuffer.remove(inputBuffer.size() - 1);
        createToken(num);
        inputBuffer.add(tempchar2);
        inputBuffer.add(tempchar);
    }



    public String charToString(ArrayList<Character> array) { // returns the char array with all the added chars removed
        return array.toString().replace("[", "").replace("]", "").replace(",", "").replace(" ", "");
    }

    public String stringBuiler(ArrayList<Character> array) { // returns the lexeme for a corresponding token
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            string.append(array.get(i));
        }
        return string.toString();
    }

    private boolean symbolCheck() { // check for if the input buffer contains any valid char within the CD22 language
        return (inputBuffer.get(inputBuffer.size() - 1) == ',' || inputBuffer.get(inputBuffer.size() - 1) == '[' || inputBuffer.get(inputBuffer.size() - 1) == ']' || inputBuffer.get(inputBuffer.size() - 1) == '(' || inputBuffer.get(inputBuffer.size() - 1) == ')'
                || inputBuffer.get(inputBuffer.size() - 1) == '=' || inputBuffer.get(inputBuffer.size() - 1) == '+' || inputBuffer.get(inputBuffer.size() - 1) == '-' || inputBuffer.get(inputBuffer.size() - 1) == '*' || inputBuffer.get(inputBuffer.size() - 1) == '%'
                || inputBuffer.get(inputBuffer.size() - 1) == '^' || inputBuffer.get(inputBuffer.size() - 1) == '<' || inputBuffer.get(inputBuffer.size() - 1) == '>' || inputBuffer.get(inputBuffer.size() - 1) == ':' || inputBuffer.get(inputBuffer.size() - 1) == ';'
                || inputBuffer.get(inputBuffer.size() - 1) == '.' || inputBuffer.get(inputBuffer.size() - 1) == '/' || Character.isDigit(inputBuffer.get(inputBuffer.size() - 1)) || Character.isLetter(inputBuffer.get(inputBuffer.size() - 1))
                || inputBuffer.get(inputBuffer.size() - 1) == ' ' || charToString(inputBuffer).contains("!=") || inputBuffer.get(inputBuffer.size() - 1) == '\t' || inputBuffer.get(inputBuffer.size() - 1) == '\n' || !eof());
    }

    private boolean spaceCheck() { // checks if there is a space, tab or new line
        return (inputBuffer.get(inputBuffer.size() - 1) == ' ' || inputBuffer.get(inputBuffer.size() - 1) == '\t' || inputBuffer.get(inputBuffer.size() - 1) == '\n');
    }



    private void reservedWords() { // creates reserved words, if none are identified then creates an identifier
        if (charToString(inputBuffer).equalsIgnoreCase("CD22")) {
            tempToken = new Token(1, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("constants")) {
            tempToken = new Token(2, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("types")) {
            tempToken = new Token(3, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("def")) {
            tempToken = new Token(4, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("arrays")) {
            tempToken = new Token(5, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("main")) {
            tempToken = new Token(6, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("begin")) {
            tempToken = new Token(7, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("end")) {
            tempToken = new Token(8, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("array")) {
            tempToken = new Token(9, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("of")) {
            tempToken = new Token(10, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("func")) {
            tempToken = new Token(11, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("void")) {
            tempToken = new Token(12, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("const")) {
            tempToken = new Token(13, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("int")) {
            tempToken = new Token(14, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("float")) {
            tempToken = new Token(15, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("bool")) {
            tempToken = new Token(16, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("for")) {
            tempToken = new Token(17, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("repeat")) {
            tempToken = new Token(18, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("until")) {
            tempToken = new Token(19, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("if")) {
            tempToken = new Token(20, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("else")) {
            tempToken = new Token(21, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("elif")) {
            tempToken = new Token(22, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("input")) {
            tempToken = new Token(23, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("print")) {
            tempToken = new Token(24, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("printline")) {
            tempToken = new Token(25, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("return")) {
            tempToken = new Token(26, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("not")) {
            tempToken = new Token(27, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("and")) {
            tempToken = new Token(28, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("or")) {
            tempToken = new Token(29, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("xor")) {
            tempToken = new Token(30, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("true")) {
            tempToken = new Token(31, null, row, column);
        } else if (charToString(inputBuffer).equalsIgnoreCase("false")) {
            tempToken = new Token(32, null, row, column);
        } else {
            tempToken = new Token(58, charToString(inputBuffer), row, column);
        }
        // tokenize it
        tokenList.add(tempToken);
        // clear input buffer
        inputBuffer.clear();
        // mark token as complete
        tokenComplete = true;
    }


    private void reservedSymbols() { // delimiters and operators
        if (inputBuffer.get(inputBuffer.size() - 1) == ',') {
            tempToken = new Token(33, null, row, column);
        } else if (inputBuffer.get(inputBuffer.size() - 1) == '[') {
            tempToken = new Token(34, null, row, column);
        } else if (inputBuffer.get(inputBuffer.size() - 1) == ']') {
            tempToken = new Token(35, null, row, column);
        } else if (inputBuffer.get(inputBuffer.size() - 1) == '(') {
            tempToken = new Token(36, null, row, column);
        } else if (inputBuffer.get(inputBuffer.size() - 1) == ')') {
            tempToken = new Token(37, null, row, column);
        } else if (inputBuffer.get(0) == '=' && inputBuffer.size() == 1) { // this is required as == is the only operator that as 2 of the same, in hindsight I could just place the if statement for == before this
            tempToken = new Token(38, null, row, column);
        } else if (inputBuffer.get(inputBuffer.size() - 1) == '+') {
            tempToken = new Token(39, null, row, column);
        } else if (inputBuffer.get(inputBuffer.size() - 1) == '-') {
            tempToken = new Token(40, null, row, column);
        } else if (inputBuffer.get(inputBuffer.size() - 1) == '*') {
            tempToken = new Token(41, null, row, column);
        } else if (inputBuffer.get(inputBuffer.size() - 1) == '/') {
            tempToken = new Token(42, null, row, column);
        } else if (inputBuffer.get(inputBuffer.size() - 1) == '%') {
            tempToken = new Token(43, null, row, column);
        } else if (inputBuffer.get(inputBuffer.size() - 1) == '^') {
            tempToken = new Token(44, null, row, column);
        } else if (inputBuffer.get(inputBuffer.size() - 1) == '<') {
            tempToken = new Token(45, null, row, column);
        } else if (inputBuffer.get(inputBuffer.size() - 1) == '>') {
            tempToken = new Token(46, null, row, column);
        } else if (inputBuffer.get(inputBuffer.size() - 1) == ':') {
            tempToken = new Token(47, null, row, column);
        } else if (inputBuffer.get(inputBuffer.size() - 1) == ';') {
            tempToken = new Token(48, null, row, column);
        } else if (inputBuffer.get(inputBuffer.size() - 1) == '.') {
            tempToken = new Token(49, null, row, column);
        } else if (charToString(inputBuffer).equals("<=")) {
            tempToken = new Token(50, null, row, column);
        } else if (charToString(inputBuffer).equals(">=")) {
            tempToken = new Token(51, null, row, column);
        } else if (charToString(inputBuffer).equals("!=")) {
            tempToken = new Token(52, null, row, column);
        } else if (charToString(inputBuffer).equals("==")) {
            tempToken = new Token(53, null, row, column);
        } else if (charToString(inputBuffer).equals("+=")) {
            tempToken = new Token(54, null, row, column);
        } else if (charToString(inputBuffer).equals("-=")) {
            tempToken = new Token(55, null, row, column);
        } else if (charToString(inputBuffer).equals("*=")) {
            tempToken = new Token(56, null, row, column);
        } else if (charToString(inputBuffer).equals("/=")) {
            tempToken = new Token(57, null, row, column);
        }
        // tokenize it
        tokenList.add(tempToken);
        // clear input buffer
        inputBuffer.clear();
        // mark token as complete
        tokenComplete = true;
    }


    private void increment() { // increments the input buffer reading one more char from the input program

        if (position != inputBuffer.size() - 1) {// check for end of file
            inputBuffer.add(program.get(position));
            position++;

            // tracks the incrementing of rows and columns
            if (inputBuffer.get(inputBuffer.size() - 1) == '\n') {
                row++;
                column = 0;
            } else if (inputBuffer.get(inputBuffer.size() - 1) == '\t') {
                column += 4;
            } else {
                column++;
            }

        }
    }
}