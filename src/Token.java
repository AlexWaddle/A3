/*
Name: Alex James Waddell
Student Number: C3330987
Description: the token class acts as an object that the scanner can pass to the A1 class
 */

public class Token {
    public static final int
            TTEOF =  0,   // Token value for end of file
    // The 32 keywords
    TCD22 =  1, TCONS = 2, TTYPS = 3, TTDEF = 4, TARRS = 5,
            TMAIN = 6,
            TBEGN =  7, TTEND = 8, TARAY = 9, TTTOF = 10, TFUNC = 11,
            TVOID = 12,
            TCNST = 13, TINTG = 14, TFLOT = 15, TBOOL = 16, TTFOR = 17,
            TREPT = 18,
            TUNTL = 19, TIFTH = 20, TELSE = 21, TELIF = 22,     TINPT
            = 23, TPRNT = 24,
            TPRLN = 25,     TRETN = 26, TNOTT = 27, TTAND = 28, TTTOR
            = 29, TTXOR = 30,
            TTRUE = 31,     TFALS = 32,
    // the operators and delimiters
    TCOMA = 33, TLBRK = 34, TRBRK = 35, TLPAR = 36, TRPAR = 37,
            TEQUL = 38,
            TPLUS = 39, TMINS = 40, TSTAR = 41, TDIVD = 42, TPERC = 43,
            TCART = 44,
            TLESS = 45, TGRTR = 46, TCOLN = 47, TSEMI = 48, TDOTT = 49,
            TLEQL = 50,
            TGEQL = 51,     TNEQL = 52, TEQEQ = 53, TPLEQ = 54, TMNEQ
            = 55, TSTEQ = 56,
            TDVEQ = 57,
    // the tokens which need tuple values
    TIDEN = 58, TILIT = 59, TFLIT = 60, TSTRG = 61, TUNDF = 62;

    private int tokenNo;
    private String lexeme;
    private int row;
    private int col;
    private int st; // symbol table record

    public Token (int tokenNo_, String lexeme_, int row_, int col_) {
        tokenNo = tokenNo_;
        lexeme = lexeme_;
        row = row_;
        col = col_;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public int getTokenNo() {
        return tokenNo;
    }

    public String getLexeme() {
        return lexeme;
    }

    public void setSt(int st) {
        this.st = st;
    }

    public int getSt() {
        return st;
    }
}
