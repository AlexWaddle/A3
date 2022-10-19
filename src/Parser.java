/*
Name: Alex James Waddell
Student Number: C3330987
Description: the parser class, reads in a list of tokens and converts it into a tree
 */
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class Parser {

    Token lookahead; // holds the token for the next token to be read, needed for ll1
    Node root; // the start node NPROG
    ArrayList<String> errors = new ArrayList<>(); // list of errors
    ArrayList<String> semanticErrors = new ArrayList<>();
    ArrayList<Token> tokenStream; // program in list of token form
    SymbolTable table; // symbol table, currently no scope
    int count; // assists in formatting output
    String className;

    // constructor
    public Parser() {

    }

    // constructor
    public Parser(ArrayList<Token> tokenStream_) {
        tokenStream = tokenStream_;
        table = new SymbolTable();
        count = 70;
    }

    // constructor
    public Node parse() {
        lookahead = nextToken();
        root = program();
        if (lookahead.getTokenNo() == 0) { // if the final token is the end of file token return
            return root;
        } else {
            return null;
        }
    }

    // prints any errors
    public void errors(ArrayList<String> listing) {

        try {
            // creates a file and writer to write to a ProgramListing.lst file
            File outputFile = new File("ProgramListing.lst");
            FileWriter outputWriter = new FileWriter("ProgramListing.lst");
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            // writes the program listing to the programlisting file
            for (int i = 0; i < listing.size(); i++) {
                outputWriter.write(line(i) + listing.get(i) + "\n");
            }
            // writes the error listing to the errorlisting file
            for (int i = 0; i < errors.size(); i++) {
                outputWriter.write(errors.get(i)+ "\n");
            }
            outputWriter.close();
        } catch (Exception ignored) {}
        // prints the errors encountered underneath the node tree
        System.out.print("\n");
        for (int i = 0; i < errors.size(); i++) {
            System.out.println(errors.get(i));
        }
        System.out.print("\n");
        for (int i = 0; i < semanticErrors.size(); i++) {
            System.out.println(semanticErrors.get(i));
        }
    }

    // formatts line numbers for the program listing
    private String line(int num) {
        String paddedNum = String.format("%03d", num);
        return (paddedNum + ": ");
    }

    public void traverseTree() {
        preorder(root);
    }

    // does a preorder traversal of the tree
    // count holds the length of the line to ensure that nothing new is written
    // after 10 7 character nodes or lexemes are printed
    public void preorder(Node node) {
        String lexeme = "";
        if (node == null) { // if a child is null simply return and continue
            return;
        }

        // prints the node
        System.out.print(paddString(node.getNodeValue()));
        count -= paddString(node.getNodeValue()).length();

        if (count <= 0) { // checks if the line has exceeded 70 chars long, if so go to a new line
            count = 70;
            System.out.println("");
        }

        // gets the lexeme from specific nodes, if the node has a lexeme
        if (node.getSymbolValue() != null && !node.getNodeValue().equals("NFCALL") && !node.getNodeValue().equals("NFUND") && !node.getNodeValue().equals("NCALL")) {
            count -= paddString(node.getSymbolValue()).length();
            lexeme = paddString(node.getSymbolValue());
        } else {
            lexeme = "";
        }
        // prints the above lexeme that was retrieved
        System.out.print(lexeme);

        if (count <= 0) {
            count = 70;
            System.out.println("");
        }

        // preorder traversal for all three possible children
        preorder(node.getLeftSubTree());
        preorder(node.getMiddleSubTree());
        preorder(node.getRightSubTree());

    }

    // padds the string to a multiple of 7 i.e. if its below 7 just to seven but if its 20, itl go to 21
    public String paddString(String string) {
        String result = string;
        for (int i = 0; i < (7 - (string.length() % 7)); i++) { // mod of the string length gets the amount needed to padd to 7
            result += " ";
        }
        return result;
    }

    // token grabber method, forgot i had made it hence its not used much
    public Token nextToken() {
        if (tokenStream.size() != 0) {
            return tokenStream.remove(0);
        } else {
            return null;
        }
    }

    // Program - <program> ::= CD22 <id> <globals> <funcs> <mainbody>
    private Node program() {
        Node node = new Node("NPROG");

        if (lookahead.getTokenNo() != Token.TCD22) { // does token = CD22
            errors.add("Expected CD22 on line" + lookahead.getRow() + " row " + lookahead.getCol());
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);
        if (lookahead.getTokenNo() != Token.TIDEN) {
            errors.add("Expected an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
            return new Node("NUNDEF");
        }
        // creates a new symbol and adds it to the node as well as the symbol table
        className = lookahead.getLexeme();
        Symbol symbol = new Symbol(lookahead.getLexeme(), Symbol.cD);
        node.setSymbolValue(lookahead.getLexeme());
        node.setSymbolType(Symbol.cD);
        table.insert(symbol);
        lookahead = tokenStream.remove(0);

        node.setLeftSubTree(globals()); // globals

        node.setMiddleSubTree(funcs()); // funcs

        node.setRightSubTree(mainbody()); // mainbody

        return node;
    }

    // globals - <globals> ::= <consts> <types> <arrays>
    public Node globals() {
        Node node = new Node("NGLOB");
        // if check for constants, types and then arrays respectively
        if (lookahead.getTokenNo() == Token.TCONS || lookahead.getTokenNo() == Token.TTYPS || lookahead.getTokenNo() == Token.TARRS) {
            node.setLeftSubTree(consts()); // these should return null if they dont exist
            node.setMiddleSubTree(types());
            node.setRightSubTree(arrays());
        }
        return node;

    }

    // consts
    // constants - <consts> ::= constants <initlist> | ε

    public Node consts() {

        if (lookahead.getTokenNo() != Token.TCONS) {
            return null; //  ε
        }
        lookahead = tokenStream.remove(0);
        return initlist();
    }


    // initlist - <initlist> ::= <init> <initlist’>
    // <initlist’> ::= , <initlist> | ε

    public Node initlist() {
        Node node1, node2;
        node1 = init();

        // FOLLOW set
        if (lookahead.getTokenNo() == Token.TTYPS || lookahead.getTokenNo() == Token.TARRS || lookahead.getTokenNo() == Token.TMAIN || lookahead.getTokenNo() != Token.TFUNC) {
            return node1; // ε
        }

        if (lookahead.getTokenNo() == Token.TCOMA) {
            lookahead = tokenStream.remove(0);
            node2 = initlist();
            return new Node("NILIST", node1, node2);
        } else {
            errors.add("Expected a comma on line " + lookahead.getRow() + " row " + lookahead.getCol());
            // the while loops in the error catches are for error recovery for the follow sets
            while (lookahead.getTokenNo() != Token.TTYPS && lookahead.getTokenNo() != Token.TARRS && lookahead.getTokenNo() != Token.TMAIN
                    && lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC && lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
    }


    // init - <init>   ::= <id> = <expr>

    public Node init() {
        Node node = new Node("NINIT");

        if (lookahead.getTokenNo() != Token.TIDEN) { // id
            errors.add("Expected an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TCOMA && lookahead.getTokenNo() != Token.TTYPS && lookahead.getTokenNo() != Token.TARRS
                    && lookahead.getTokenNo() != Token.TMAIN && lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC
                    && lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        Symbol symbol = new Symbol(lookahead.getLexeme(), Symbol.id);
        node.setSymbolValue(lookahead.getLexeme());
        node.setSymbolType(Symbol.id);
        table.insert(symbol);
        lookahead = tokenStream.remove(0);
        if (lookahead.getTokenNo() != Token.TEQUL) { // =
            errors.add("Expected an = symbol on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TCOMA && lookahead.getTokenNo() != Token.TTYPS && lookahead.getTokenNo() != Token.TARRS
                    && lookahead.getTokenNo() != Token.TMAIN && lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC
                    && lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);
        node.setLeftSubTree(expr());
        return node;
    }

    // types - <types> ::= types <typelist> | ε

    public Node types() {
        if (lookahead.getTokenNo() != Token.TTYPS) {
            return null;
        }
        lookahead = tokenStream.remove(0);
        return typelist();
    }

    // arrays - <arrays> ::= arrays <arrdecls> | ε

    public Node arrays() {
        if (lookahead.getTokenNo() != Token.TARRS) {
            return null;
        }
        lookahead = tokenStream.remove(0);
        return arrdecls();
    }

    // funcs - <funcs> ::= <func> <funcs> | ε

    public Node funcs() {
        if (lookahead.getTokenNo() != Token.TFUNC) {
            return null;
        }
        Node node = func();
        return new Node("NFUNCS", node, funcs());
    }

    // mainbody - <mainbody> ::= main <slist> begin <stats> end CD22 <id>

    public Node mainbody() {
        Node node = new Node("NMAIN");

        if (lookahead.getTokenNo() != Token.TMAIN) {
            errors.add("Expected main on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        node.setLeftSubTree(slist());

        if (lookahead.getTokenNo() != Token.TBEGN) {
            errors.add("Expected begin on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        node.setRightSubTree(stats());

        if (lookahead.getTokenNo() != Token.TTEND) {
            errors.add("Expected an end on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() != Token.TCD22) {
            errors.add("Expected CD22 on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() != Token.TIDEN) {
            errors.add("Expected an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }

        // check if start = end id
        if (!lookahead.getLexeme().equals(className)) {
            semanticErrors.add("CD22 name at start and end do not match");
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);
        return node;
    }

    // slist - <slist>  ::= <sdecl> <slist’>
    // <slist’> ::= , <slist> | ε

    public Node slist() {
        Node node = sdecl();
        if (lookahead.getTokenNo() == Token.TCOMA) {
            lookahead = tokenStream.remove(0);
            return new Node("NSDLST", node, slist());
        }
        return node;
    }

    // typelist - <typelist> ::= <type> <typelist’>
    // <typelist’> ::= <typelist> | ε

    public Node typelist() {
        Node node1;
        node1 = type();
        if (lookahead.getTokenNo() == Token.TIDEN) { // first of typelist
            return new Node("NTYPEL", node1, typelist());
        }
        return node1;
    }

    // type - <type>  ::= <structid/typeid> def <type’>
    // <type>  ::= array [ <expr> ] of <structid> end || <fields> end
    // for this rule only ive got structid and typeid as the same coz they are tiden
    // here is where they are defined in a symbol table so i can tell them apart in future

    public Node type() {

        Node node = new Node();
        if (lookahead.getTokenNo() != Token.TIDEN) {
            errors.add("Expected an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TARRS && lookahead.getTokenNo() != Token.TMAIN &&
                    lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC  && lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF"); //
        }
        Token tempToken = lookahead;

        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() != Token.TTDEF) {
            errors.add("Expected a def on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TARRS && lookahead.getTokenNo() != Token.TMAIN &&
                    lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC  && lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }

        lookahead = tokenStream.remove(0);
        if (lookahead.getTokenNo() == Token.TARAY) { // array part

            // id from up there gets clasified as a typeid
            Symbol symbol = new Symbol(tempToken.getLexeme(), Symbol.typeID);
            node.setSymbolValue(tempToken.getLexeme());
            node.setSymbolType(Symbol.typeID);
            table.insert(symbol);
            lookahead = tokenStream.remove(0);

            node.setNodeValue("NATYPE");

            if (lookahead.getTokenNo() != Token.TLBRK) {
                errors.add("Expected a [ on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TARRS && lookahead.getTokenNo() != Token.TMAIN &&
                        lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC  && lookahead.getTokenNo() != Token.TTEOF) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            lookahead = tokenStream.remove(0);

            node.setLeftSubTree(expr());

            // TODO

            if (lookahead.getTokenNo() != Token.TRBRK) {
                errors.add("Expected a ] on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TARRS && lookahead.getTokenNo() != Token.TMAIN &&
                        lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC && lookahead.getTokenNo() != Token.TTEOF) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            lookahead = tokenStream.remove(0);

            if (lookahead.getTokenNo() != Token.TTTOF) {
                errors.add("Expected of on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TARRS && lookahead.getTokenNo() != Token.TMAIN &&
                        lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC && lookahead.getTokenNo() != Token.TTEOF) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            lookahead = tokenStream.remove(0);

            if (lookahead.getTokenNo() != Token.TIDEN) {
                errors.add("Expected an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TARRS && lookahead.getTokenNo() != Token.TMAIN &&
                        lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC && lookahead.getTokenNo() != Token.TTEOF) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            // later semantic checking here
            lookahead = tokenStream.remove(0);

            if (lookahead.getTokenNo() != Token.TTEND) {
                errors.add("Expected an end on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TARRS && lookahead.getTokenNo() != Token.TMAIN &&
                        lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC && lookahead.getTokenNo() != Token.TTEOF) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            lookahead = tokenStream.remove(0);
            return node;


        } else {
            node.setNodeValue("NRTYPE");
            Symbol symbol = new Symbol(tempToken.getLexeme(), Symbol.structID);
            node.setSymbolValue(tempToken.getLexeme());
            node.setSymbolType(Symbol.structID);
            table.insert(symbol);
            node.setLeftSubTree(fields());

            // TODO here i can set what a type contains

            if (lookahead.getTokenNo() != Token.TTEND) {
                errors.add("Expected an end on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TARRS && lookahead.getTokenNo() != Token.TMAIN &&
                        lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC && lookahead.getTokenNo() != Token.TTEOF) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            lookahead = tokenStream.remove(0);
            return node;
        }

    }


    // fields - <fields> ::= <sdecl> <fields’>
    // <fields’> ::= , <fields> | ε

    public Node fields() {
        Node node = sdecl();

        if (lookahead.getTokenNo() == Token.TCOMA) {
            lookahead = tokenStream.remove(0);
            return new Node("NFLIST", node, fields());
        }
        return node;
    }

    // sdecl - <sdecl> ::= <id> : <sdecl’>
    // <sdecl’> ::= <stype> | <structid>
    // <stype> ::= int | float | bool
    // stype is just included within this rule as its easier

    public Node sdecl() {
        Node node = new Node();

        if (lookahead.getTokenNo() != Token.TIDEN) {
            errors.add("Expected an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TARRS && lookahead.getTokenNo() != Token.TMAIN &&
                    lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC && lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        Symbol symbol = new Symbol(lookahead.getLexeme());
        node.setSymbolValue(lookahead.getLexeme());


        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() != Token.TCOLN) {
            errors.add("Expected a colon on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TARRS && lookahead.getTokenNo() != Token.TMAIN &&
                    lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC && lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() == Token.TINTG || lookahead.getTokenNo() == Token.TFLOT || lookahead.getTokenNo() == Token.TBOOL) {
            // STYPE, easier to do in the rule itself
            if (lookahead.getTokenNo() == Token.TINTG) {
                symbol.setType(Symbol.integer);
                node.setSymbolType(Symbol.integer);
            } else if (lookahead.getTokenNo() == Token.TFLOT) {
                symbol.setType(Symbol.real);
                node.setSymbolType(Symbol.real);
            } else if (lookahead.getTokenNo() == Token.TBOOL) {
                symbol.setType(Symbol.bool);
                node.setSymbolType(Symbol.bool);
            }
            lookahead = tokenStream.remove(0);
            node.setNodeValue("NSDECL");
            table.insert(symbol);
            return node;
        } else if (lookahead.getTokenNo() == Token.TIDEN) {
            // semantic check for id structid exists
            symbol.setType(lookahead.getLexeme()); // sets it to the type of structure that the identifer is
            lookahead = tokenStream.remove(0);
            node.setNodeValue("NTDECL");
            table.insert(symbol);
            return node;
        } else {
            errors.add("Expected a primitive or structure type for the identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TARRS && lookahead.getTokenNo() != Token.TMAIN &&
                    lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC && lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }

    }

    //  arrdecls - <arrdecls> ::= <arrdecl> <arrdecls’>
    // <arrdecls’> ::= , <arrdecls> | ε

    public Node arrdecls() {
        Node node = arrdecl();

        if (lookahead.getTokenNo() == Token.TCOMA) {
            lookahead = tokenStream.remove(0);
            return new Node("NALIST", node, arrdecls());
        }
        return node;
    }

    // arrdecl - <arrdecl> ::= <id> : <typeid>

    public Node arrdecl() {
        Node node = new Node("NARRD");
        if (lookahead.getTokenNo() != Token.TIDEN) {
            errors.add("Expected an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TMAIN && lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC &&
                    lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        Symbol symbol = new Symbol(lookahead.getLexeme());
        node.setSymbolValue(lookahead.getLexeme());
        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() != Token.TCOLN) {
            errors.add("Expected a : symbol on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TMAIN && lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC &&
                    lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() != Token.TIDEN) {
            errors.add("Expected an array type on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TMAIN && lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC &&
                    lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        symbol.setType(lookahead.getLexeme());
        node.setSymbolType(lookahead.getLexeme());
        table.insert(symbol);
        lookahead = tokenStream.remove(0);
        return node;
    }

    // func - <func>  ::= func  <id> ( <plist> ) : <rtype> <funcbody>
    // <funcbody> ::= <locals> begin <stats> end
    // <rtype> ::= <stype> | void
    // <stype> ::= int | float | bool
    // funcbody, rtype and stype were all implemented within this node just to make it more simple
    // also allows for funcbody's children to be set to nfund as funcbody has no node of its own

    public Node func() {
        Node node = new Node("NFUND");

        if (lookahead.getTokenNo() != Token.TFUNC) {
            errors.add("Expected func on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TMAIN && lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC &&
                    lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }


        lookahead = tokenStream.remove(0);

        // SYMBOL TABLE

        if (lookahead.getTokenNo() != Token.TIDEN) {
            errors.add("Expected an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TMAIN && lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC &&
                    lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }

        Symbol symbol = new Symbol(lookahead.getLexeme());
        node.setSymbolValue(lookahead.getLexeme());
        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() != Token.TLPAR) {
            errors.add("Expected ( on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TMAIN && lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC &&
                    lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        node.setLeftSubTree(plist());

        // store in symbol table the params as an arraylist
        ArrayList<String> params = new ArrayList<>();
        params = getParams(node.getLeftSubTree(),params);
        symbol.setParams(params);

        if (lookahead.getTokenNo() != Token.TRPAR) {
            errors.add("Expected ) on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TMAIN && lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC &&
                    lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() != Token.TCOLN) {
            errors.add("Expected : on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TMAIN && lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC &&
                    lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() != Token.TVOID && lookahead.getTokenNo() != Token.TINTG && lookahead.getTokenNo() != Token.TFLOT && lookahead.getTokenNo() != Token.TBOOL) {
            errors.add("Expected either void or a type for the return type on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TMAIN && lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC &&
                    lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }



        if (lookahead.getTokenNo() == Token.TVOID) {
            symbol.setType(Symbol.empty);
            node.setSymbolType(Symbol.empty);

        } else if (lookahead.getTokenNo() == Token.TINTG) {
            symbol.setType(Symbol.integer);
            node.setSymbolType(Symbol.integer);

        } else if (lookahead.getTokenNo() == Token.TFLOT) {
            symbol.setType(Symbol.real);
            node.setSymbolType(Symbol.real);

        } else {
            symbol.setType(Symbol.bool);
            node.setSymbolType(Symbol.bool);

        }
        table.insert(symbol);

        lookahead = tokenStream.remove(0);

        // funcbody doesnt have a node of its own so I just put its children into nfund

        node.setMiddleSubTree(locals());

        if (lookahead.getTokenNo() != Token.TBEGN) {
            errors.add("Expected a begin line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TMAIN && lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC &&
                    lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        node.setRightSubTree(stats());

        // traverse it and find a


        // semantic check for if a return statement exists within the function

        if (preOrderExists(node.getRightSubTree())) {
            semanticErrors.add("Expected a return statement in function " + node.getNodeValue());
            return new Node("NUNDEF");
        }

        if (lookahead.getTokenNo() != Token.TTEND) {
            errors.add("Expected an end on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TMAIN && lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TFUNC &&
                    lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        return node;
    }

    // funcbody - is done within func

    // locals - <locals> ::= <dlist> |  ε

    public Node locals() {
        if (lookahead.getTokenNo() != Token.TIDEN) { // check if sdecl/arrdecl are next
            return null;
        }

        return dlist();
    }

    // dlist - <dlist>  ::= <decl> <dlist’>
    // <dlist’> ::= , <dlist’> | ε

    public Node dlist() {
        Node node = decl();
        if (lookahead.getTokenNo() == Token.TCOMA) {
            lookahead = tokenStream.remove(0);
            while (lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TBEGN && lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NDLIST", node, dlist());
        }
        return node;
    }

    // decl - <decl>  ::= <sdecl> | <arrdecl>
    // <sdecl> ::= <id> : <sdecl’>
    // <sdecl’> ::= <stype> | <structid>
    // <stype> ::= int | float | bool
    // <arrdecl> ::= <id> : <typeid>
    // all of these were included as i need to be able to access the structid/typeid

    public Node decl() {
        Node node = new Node();
        if (lookahead.getTokenNo() != Token.TIDEN) {
            errors.add("Expected an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TCOMA && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TBEGN &&
                    lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        Symbol symbol = new Symbol(lookahead.getLexeme());
        node.setSymbolValue(lookahead.getLexeme());

        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() != Token.TCOLN) {
            errors.add("Expected an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TCOMA && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TBEGN &&
                    lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);


        if (lookahead.getTokenNo() == Token.TIDEN) {
            if (table.getSymbol(lookahead.getLexeme()) != null) {
                if (table.getSymbol(lookahead.getLexeme()).getType() != null) {
                    if (table.getSymbol(lookahead.getLexeme()).getType().equals(Symbol.structID)) { // sdecl
                        node.setNodeValue("NTDECL");
                        symbol.setType(Symbol.structID);
                        node.setSymbolType(Symbol.structID);
                    } else if (table.getSymbol(lookahead.getLexeme()).getType().equals(Symbol.typeID)) { // arrdecl
                        node.setNodeValue("NARRD");
                        symbol.setType(Symbol.typeID);
                        node.setSymbolType(Symbol.typeID);
                    }
                    table.insert(symbol);
                    return node;
                }
            }
            errors.add("Expected either a structure or type identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TCOMA && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TBEGN &&
                    lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        } else if (lookahead.getTokenNo() == Token.TINTG || lookahead.getTokenNo() == Token.TFLOT || lookahead.getTokenNo() == Token.TBOOL) {
            // STYPE, easier to do in the rule itself
            if (lookahead.getTokenNo() == Token.TINTG) {
                symbol.setType(Symbol.integer);
                node.setSymbolType(Symbol.integer);
            } else if (lookahead.getTokenNo() == Token.TFLOT) {
                symbol.setType(Symbol.real);
                node.setSymbolType(Symbol.real);
            } else if (lookahead.getTokenNo() == Token.TBOOL) {
                symbol.setType(Symbol.bool);
                node.setSymbolType(Symbol.bool);
            }
            lookahead = tokenStream.remove(0);
            table.insert(symbol);
            node.setNodeValue("NSDECL");
            return node;
        } else {
            errors.add("Expected a primitive type, structure or type on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TCOMA && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TBEGN &&
                    lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
    }

    // stype, not done here, easier to do in the rules themselves


    // stats - <stats>  ::= <stat> ; <stats’>| <strstat> <stats’>
    // <stats’> ::= <stats> | ε

    public Node stats() {
        Node node;

        if (lookahead.getTokenNo() == Token.TIFTH || lookahead.getTokenNo() == Token.TTFOR) {
            node = strstat();
        } else if (lookahead.getTokenNo() == Token.TREPT || lookahead.getTokenNo() == Token.TRETN || lookahead.getTokenNo() == Token.TIDEN ||
                lookahead.getTokenNo() == Token.TINPT || lookahead.getTokenNo() == Token.TPRNT || lookahead.getTokenNo() == Token.TPRLN) {

            node = stat();

            if (lookahead.getTokenNo() != Token.TSEMI) {

                errors.add("Expected ; on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TTEOF) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            lookahead = tokenStream.remove(0);

        } else {

            errors.add("Expected a statement on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TTEOF) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }

        if (lookahead.getTokenNo() == Token.TIFTH || lookahead.getTokenNo() == Token.TTFOR || lookahead.getTokenNo() == Token.TREPT ||
                lookahead.getTokenNo() == Token.TRETN || lookahead.getTokenNo() == Token.TIDEN || lookahead.getTokenNo() == Token.TINPT ||
                lookahead.getTokenNo() == Token.TPRNT || lookahead.getTokenNo() == Token.TPRLN) {
            return new Node("NSTATS", node, stats());
        }

        return node;
    }

    // strstat - <strstat> ::= <forstat> | <ifstat>

    public Node strstat() {
        if (lookahead.getTokenNo() == Token.TIFTH) {
            return ifstat();
        } else if (lookahead.getTokenNo() == Token.TTFOR) {
            return forstat();
        } else {
            errors.add("Expected a statement on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
    }

    // stat - <stat>   ::= <reptstat> | <asgnstat> | <iostat> | <callstat> | <returnstat>

    public Node stat() {

        if (lookahead.getTokenNo() == Token.TREPT) {
            return reptstat();
        } else if (lookahead.getTokenNo() == Token.TIDEN) {
            Token tempToken = lookahead;
            lookahead = tokenStream.remove(0);
            if (lookahead.getTokenNo() == Token.TLPAR) {// either callstat or asgnstat
                return callstat(tempToken);
            } else {
                return asgnstat(tempToken);
            }
        } else if (lookahead.getTokenNo() == Token.TRETN) {
            return returnstat();
        } else if (lookahead.getTokenNo() == Token.TINPT || lookahead.getTokenNo() == Token.TPRLN || lookahead.getTokenNo() == Token.TPRNT) {
            return iostat();
        } else {
            errors.add("Expected a statement on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
    }

    // forstat - <forstat> ::= for ( <asgnlist> ; <bool> ) <stats> end

    public Node forstat() {
        Node node = new Node("NFORL");
        if (lookahead.getTokenNo() != Token.TTFOR) {
            errors.add("Expected for on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() != Token.TLPAR) {
            errors.add("Expected ( on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        node.setLeftSubTree(asgnlist());

        if (lookahead.getTokenNo() != Token.TSEMI) {

            errors.add("Expected ; on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        node.setMiddleSubTree(bool());

        if (lookahead.getTokenNo() != Token.TRPAR) {
            errors.add("Expected ) on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        node.setRightSubTree(stats());

        if (lookahead.getTokenNo() != Token.TTEND) {
            errors.add("Expected end on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);
        return node;
    }

    // repstat - <repstat> ::= repeat ( <asgnlist> ) <stats> until <bool>

    public Node reptstat() {
        Node node = new Node("NREPT");
        if (lookahead.getTokenNo() != Token.TREPT) {
            errors.add("Expected repeat on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);
        if (lookahead.getTokenNo() != Token.TLPAR) {
            errors.add("Expected ( on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);
        node.setLeftSubTree(asgnlist());
        if (lookahead.getTokenNo() != Token.TRPAR) {
            errors.add("Expected ) on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        node.setMiddleSubTree(stats());

        if (lookahead.getTokenNo() != Token.TUNTL) {
            errors.add("Expected until on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);
        node.setRightSubTree(bool());
        return node;
    }

    // asgnlist - <asgnlist> ::=  <alist> | ε

    public Node asgnlist() {
        if (lookahead.getTokenNo() != Token.TIDEN) { // first of var
            return null;
        }
        return alist();
    }

    // alist - <alist>  ::= <asgnstat> <alist’>
    // <alist’> ::= , <alist> | ε

    public Node alist() {
        Node node = asgnstat(null);

        if (lookahead.getTokenNo() == Token.TCOMA) {
            lookahead = tokenStream.remove(0);
            return new Node("NASGNS", node, alist());
        }
        return node;
    }

    // ifstat - <ifstat> ::= if ( <bool> ) <stats> <ifstat’>
    // <ifstat’> ::= end | else <stats> end | elif (<bool>) <stats> end

    public Node ifstat() {
        Node node = new Node();

        if (lookahead.getTokenNo() != Token.TIFTH) {
            errors.add("Expected if on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() != Token.TLPAR) {
            errors.add("Expected ( on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        node.setLeftSubTree(bool());

        if (lookahead.getTokenNo() != Token.TRPAR) {
            errors.add("Expected ) on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        Node node1 = stats();

        if (lookahead.getTokenNo() == Token.TTEND) {
            lookahead = tokenStream.remove(0);
            node.setRightSubTree(node1);
            node.setNodeValue("NIFTH");
            return node;
        } else if (lookahead.getTokenNo() == Token.TELSE) {
            lookahead = tokenStream.remove(0);
            node.setNodeValue("NIFTE");
            node.setMiddleSubTree(node1);
            node.setRightSubTree(stats());
            if (lookahead.getTokenNo() == Token.TTEND) {
                lookahead = tokenStream.remove(0);
                return node;
            } else {
                errors.add("Expected End on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                        lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                        lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
        } else if (lookahead.getTokenNo() == Token.TELIF) {
            lookahead = tokenStream.remove(0);
            if (lookahead.getTokenNo() != Token.TLPAR) {
                errors.add("Expected ( on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                        lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                        lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            lookahead = tokenStream.remove(0);
            node.setNodeValue("NIFEF");
            node.setMiddleSubTree(node1);
            Node childleft = bool();
            if (lookahead.getTokenNo() != Token.TRPAR) {
                errors.add("Expected ) on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                        lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                        lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            lookahead = tokenStream.remove(0);
            Node childright = stats();
            if (lookahead.getTokenNo() != Token.TTEND) {
                errors.add("Expected End on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                        lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                        lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            lookahead = tokenStream.remove(0);
            Node rightNode = new Node("NIFTH");
            rightNode.setLeftSubTree(childleft);
            rightNode.setRightSubTree(childright);
            node.setRightSubTree(rightNode);
            return node;

        } else {
            errors.add("Expected an end, else or elif on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
    }

    // asgnstat - <asgnstat> ::= <var> <asgnop> <bool>

    public Node asgnstat(Token tempToken) {
        Node leftChild;

        if (tempToken != null) {
            leftChild = varToken(tempToken);
        } else {
            leftChild = varToken(null);
        }

        Node parentNode = asgnop();
        Node rightChild = bool();

        parentNode.setLeftSubTree(leftChild);
        parentNode.setRightSubTree(rightChild);
        return parentNode;
    }

    // asgnop - <asgnop> ::= = | += | -= | *= | /=

    public Node asgnop() {
        Node node = new Node();

        if (lookahead.getTokenNo() == Token.TEQUL) {
            node.setNodeValue("NASGN");
        } else if (lookahead.getTokenNo() == Token.TPLEQ) {
            node.setNodeValue("NPLEQ");
        } else if (lookahead.getTokenNo() == Token.TMNEQ) {
            node.setNodeValue("NMNEQ");
        } else if (lookahead.getTokenNo() == Token.TSTEQ) {
            node.setNodeValue("NSTEA");
        } else if (lookahead.getTokenNo() == Token.TDVEQ) {
            node.setNodeValue("NDVEQ");
        } else {
            errors.add("Expected = or += or -= or *= or /= on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);
        return node;
    }

    // iostat - <iostat> ::= input <vlist> | print  <prlist> | printline <prlist>

    public Node iostat() {
        Node node = new Node();

        if (lookahead.getTokenNo() == Token.TINPT) {
            lookahead = tokenStream.remove(0);
            node.setNodeValue("NINPUT");
            node.setLeftSubTree(vlist());

        } else if (lookahead.getTokenNo() == Token.TPRNT) {
            lookahead = tokenStream.remove(0);
            node.setNodeValue("NPRINT");
            node.setLeftSubTree(prlist());

        } else if (lookahead.getTokenNo() == Token.TPRLN) {
            lookahead = tokenStream.remove(0);
            node.setNodeValue("NPRLN");
            node.setLeftSubTree(prlist());

        } else {
            errors.add("Expected input, print or printline on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        return node;
    }

    // callstat - <callstat> ::= <id> ( <callstat’>
    // <callstat’> ::= <elist> ) | )

    public Node callstat(Token token_) {

        Node node = new Node("NCALL");

        if (lookahead.getTokenNo() != Token.TIDEN && token_ == null) {
            errors.add("Expected an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
            return new Node("NUNDEF");
        }

        Symbol symbol = new Symbol(token_.getLexeme(), Symbol.id);
        node.setSymbolValue(token_.getLexeme());
        node.setSymbolType(Symbol.id);
        table.insert(symbol);

        if (lookahead.getTokenNo() != Token.TLPAR) {
            errors.add("Expected ( on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() == Token.TRPAR) { // saves me having to do first of elist()
            lookahead = tokenStream.remove(0);
        } else {
            node.setLeftSubTree(elist());

            // TODO check here for param num

            if (lookahead.getTokenNo() != Token.TRPAR) {
                errors.add("Expected ) on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                        lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                        lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            lookahead = tokenStream.remove(0);
        }
        return node;
    }

    // returnstat <returnstat> ::= return <returnstat’>
    // <returnstat’> ::= void | <expr>

    public Node returnstat() {
        Node node = new Node("NRETN");

        if (lookahead.getTokenNo() != Token.TRETN) {
            errors.add("Expected return on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() == Token.TVOID) {
            lookahead = tokenStream.remove(0);
        } else if (lookahead.getTokenNo() == Token.TIDEN || lookahead.getTokenNo() == Token.TILIT || lookahead.getTokenNo() == Token.TFLIT || lookahead.getTokenNo() == Token.TTRUE || lookahead.getTokenNo() == Token.TFALS || lookahead.getTokenNo() == Token.TLPAR) {
            node.setLeftSubTree(expr());
        } else {
            errors.add("Expected void or a return type on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        return node;
    }

    // vlist - <vlist>  ::= <var> <vlist’>
    // <vlist’> ::= , <vlist> | ε

    public Node vlist() {
        Node node = varToken(null);

        if (lookahead.getTokenNo() == Token.TCOMA) {
            lookahead = tokenStream.remove(0);
            return new Node("NVLIST", node, vlist());
        } else {
            return node;
        }
    }

    // var - <var>  ::= <id> <vartail>
    // <vartail> ::= [<expr>] <vartail2> | ε | . <id>
    // <vartail2> ::= . <id> | ε
    // <id>.<id> was just called NCOMV
    // vartoken = var, i just named it vartoken to indicate it takes a token input
    public Node varToken(Token token_) { // identical rule to var but uses an inputted token
        Node node = new Node();

        // token_ == null, allows the vartoken to either take a token input representing an id
        // or just take null and read it normally
        if (lookahead.getTokenNo() != Token.TIDEN && token_ == null) {
            errors.add("Expected an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TIFTH && lookahead.getTokenNo() != Token.TTFOR &&
                    lookahead.getTokenNo() != Token.TREPT && lookahead.getTokenNo() != Token.TRETN && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TINPT &&
                    lookahead.getTokenNo() != Token.TPRNT && lookahead.getTokenNo() != Token.TPRLN) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }

        if (token_ == null) {
            // stores the lookahead for later use if needed
            token_ = lookahead;
            lookahead = tokenStream.remove(0);
        }

        if (lookahead.getTokenNo() == Token.TLBRK) {
            lookahead = tokenStream.remove(0);
            Node tempNode = expr();
            node.setLeftSubTree(tempNode);

            if (lookahead.getTokenNo() != Token.TRBRK) {
                errors.add("Expected [ on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TDVEQ &&
                        lookahead.getTokenNo() != Token.TSTEQ && lookahead.getTokenNo() != Token.TMNEQ && lookahead.getTokenNo() != Token.TPLEQ &&
                        lookahead.getTokenNo() != Token.TEQUL && lookahead.getTokenNo() != Token.TCART && lookahead.getTokenNo() != Token.TCOMA) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            lookahead = tokenStream.remove(0);

            if (lookahead.getTokenNo() == Token.TDOTT) { // [<expr>] with a .<id> at the end
                lookahead = tokenStream.remove(0);
                if (lookahead.getTokenNo() != Token.TIDEN) {
                    errors.add("Expected an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
                    while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TDVEQ &&
                            lookahead.getTokenNo() != Token.TSTEQ && lookahead.getTokenNo() != Token.TMNEQ && lookahead.getTokenNo() != Token.TPLEQ &&
                            lookahead.getTokenNo() != Token.TEQUL && lookahead.getTokenNo() != Token.TCART && lookahead.getTokenNo() != Token.TCOMA) {
                        lookahead = tokenStream.remove(0);
                    }
                    return new Node("NUNDEF");
                }
                Node rightChild = new Node("NSIMV");
                Symbol rightSymbol = new Symbol(lookahead.getLexeme(), Symbol.id);
                rightChild.setSymbolValue(lookahead.getLexeme());
                rightChild.setSymbolType(Symbol.id);
                table.insert(rightSymbol);
                node.setRightSubTree(rightChild);
                // make right a NSIMV

                lookahead = tokenStream.remove(0);
                node.setNodeValue("NARRV");
            } else { // [<expr>] with no .<id> at the end
                node.setNodeValue("NAELT");
            }

        } else if (lookahead.getTokenNo() == Token.TDOTT) { // . <id> option
            lookahead = tokenStream.remove(0);

            if (lookahead.getTokenNo() != Token.TIDEN) {
                errors.add("Expected an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TDVEQ &&
                        lookahead.getTokenNo() != Token.TSTEQ && lookahead.getTokenNo() != Token.TMNEQ && lookahead.getTokenNo() != Token.TPLEQ &&
                        lookahead.getTokenNo() != Token.TEQUL && lookahead.getTokenNo() != Token.TCART && lookahead.getTokenNo() != Token.TCOMA) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            // creates a child node to store the second id
            Node leftChild = new Node("NSIMV");
            Symbol leftSymbol = new Symbol(lookahead.getLexeme(), Symbol.id);
            leftChild.setSymbolValue(lookahead.getLexeme());
            leftChild.setSymbolType(Symbol.id);
            table.insert(leftSymbol);
            lookahead = tokenStream.remove(0);
            node.setLeftSubTree(leftChild);
            node.setNodeValue("NCOMV");
            Symbol symbol = new Symbol(token_.getLexeme(), Symbol.id);
            node.setSymbolValue(token_.getLexeme());
            node.setSymbolType(Symbol.id);
            table.insert(symbol);
            return node;
        } else { // ε option
            Symbol symbol = new Symbol(token_.getLexeme(), Symbol.id);
            node.setSymbolValue(token_.getLexeme());
            node.setSymbolType(Symbol.id);
            table.insert(symbol);
            node.setNodeValue("NSIVM");
        }
        return node;
    }


    // elist - <elist>  ::=  <bool> <elist’>
    // <elist’> ::= , <elist> | ε

    public Node elist() {
        Node node = bool();

        if (lookahead.getTokenNo() == Token.TCOMA) {
            lookahead = tokenStream.remove(0);
            return new Node("NEXPL", node, elist());
        }
        return node;
    }


    // rtype - done elsewhere in other rules

    // plist - <plist>  ::= <params> |  ε

    public Node plist() {
        if (lookahead.getTokenNo() == Token.TIDEN || lookahead.getTokenNo() == Token.TCONS) {
            return params();
        } else {
            return null; // ε option
        }

    }

    // params - <params> ::= <param> <params’>
    // <params’> ::= , <params> | ε

    public Node params() {

        Node node = param();
        if (lookahead.getTokenNo() == Token.TCOMA) {
            lookahead = tokenStream.remove(0);
            return new Node("NPLIST", node, plist());
        }
        return node;
    }

    // param - <param> ::= <sdecl> | <arrdecl> | const <arrdecl>
    // <arrdecl> ::= <id> : <typeid>
    // <sdecl> ::= <id> : <sdecl’>
    // <sdecl’> ::= <stype> | <structid>
    // includes both sdecl and arrdecl within this node so i can check the typeid/structid if its a tiden

    public Node param() {
        Node node = new Node();

        if (lookahead.getTokenNo() == Token.TCNST) {
            lookahead = tokenStream.remove(0);
            node.setNodeValue("NARRC");
            node.setLeftSubTree(arrdecl());
        } else if (lookahead.getTokenNo() == Token.TIDEN) {

            Node childNode = new Node();
            Symbol symbol = new Symbol(lookahead.getLexeme());
            childNode.setSymbolValue(lookahead.getLexeme());
            lookahead = tokenStream.remove(0);

            if (lookahead.getTokenNo() != Token.TCOLN) {
                errors.add("Expected : on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TCOMA
                        && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TCONS) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            lookahead = tokenStream.remove(0);

            if (lookahead.getTokenNo() == Token.TINTG) {
                lookahead = tokenStream.remove(0);
                node.setNodeValue("NSIMP");
                childNode.setNodeValue("NSDECL");
                symbol.setType(Symbol.integer);
                childNode.setSymbolType(Symbol.integer);
            } else if (lookahead.getTokenNo() == Token.TFLOT) {
                lookahead = tokenStream.remove(0);
                node.setNodeValue("NSIMP");
                childNode.setNodeValue("NSDECL");
                symbol.setType(Symbol.real);
                childNode.setSymbolType(Symbol.real);
            } else if (lookahead.getTokenNo() == Token.TBOOL) {
                lookahead = tokenStream.remove(0);
                node.setNodeValue("NSIMP");
                childNode.setNodeValue("NSDECL");
                symbol.setType(Symbol.bool);
                childNode.setSymbolType(Symbol.bool);
            } else if (lookahead.getTokenNo() == Token.TIDEN) {

                if (table.getSymbol(lookahead.getLexeme()) != null) {

                    if (table.getSymbol(lookahead.getLexeme()).getType().equals(Symbol.structID)) {
                        lookahead = tokenStream.remove(0);
                        node.setNodeValue("NSIMP");
                        childNode.setNodeValue("NTDECL");
                        symbol.setType(Symbol.structID);
                        childNode.setSymbolType(Symbol.structID);
                    } else if (table.getSymbol(lookahead.getLexeme()).getType().equals(Symbol.typeID)) {
                        lookahead = tokenStream.remove(0);
                        node.setNodeValue("NARRP");
                        childNode.setNodeValue("NARRD");
                        symbol.setType(Symbol.typeID);
                        childNode.setSymbolType(Symbol.typeID);
                    }
                } else {
                    errors.add("Expected either a structure or type identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
                    while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TCOMA
                            && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TCONS) {
                        lookahead = tokenStream.remove(0);
                    }
                    return new Node("NUNDEF");
                }

            } else {
                errors.add("Expected a data type for the identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TCOMA
                        && lookahead.getTokenNo() != Token.TIDEN && lookahead.getTokenNo() != Token.TCONS) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            table.insert(symbol);
            node.setLeftSubTree(childNode);
        }
        return node;
    }

    // bool -  <bool>  ::= <rel><bool’>

    public Node bool() {// always going to start with a rel


        Node node = rel();

        return booltail(node);
    }

    // booltail - <bool’> ::= <logop><rel><bool’> | ε

    public Node booltail(Node node_) {

        if (lookahead.getTokenNo() == Token.TTAND || lookahead.getTokenNo() == Token.TTTOR || lookahead.getTokenNo() == Token.TTXOR) {
            Node middleChild = logop();
            Node rightChild = rel();
            return new Node("NBOOL", node_, middleChild, booltail(rightChild));
        } else {
            return node_; // for epsilon
        }
    }

    // rel - <rel>  ::= not <expr> <relop> <expr> | <expr> <rel’>
    // <rel’> ::= <relop><expr> | ε
    public Node rel() {

        if (lookahead.getTokenNo() == Token.TNOTT) {
            lookahead = tokenStream.remove(0);
            Node leftChild = expr();
            Node middleChild = relop();
            Node rightChild = expr();
            return new Node("NNOT", leftChild, middleChild, rightChild);
        }
        Node node = expr();


        // this section sets the relop as the parent i.e. == is the parent of two different expr()
        if (lookahead.getTokenNo() == Token.TEQEQ || lookahead.getTokenNo() == Token.TNEQL || lookahead.getTokenNo() == Token.TGRTR || lookahead.getTokenNo() == Token.TLESS ||
                lookahead.getTokenNo() == Token.TLEQL || lookahead.getTokenNo() == Token.TGEQL) {
            Node parent = relop();
            Node rightChild = expr();
            parent.setLeftSubTree(node);
            parent.setRightSubTree(rightChild);
            return parent;
        }
        return node;
    }

    // logop - <logop> ::= and | or | xor

    public Node logop() {
        if (lookahead.getTokenNo() == Token.TTAND) {
            lookahead = tokenStream.remove(0);
            return new Node("NAND");
        } else if (lookahead.getTokenNo() == Token.TTTOR) {
            lookahead = tokenStream.remove(0);
            return new Node("NOR");
        } else if (lookahead.getTokenNo() == Token.TTXOR) {
            lookahead = tokenStream.remove(0);
            return new Node("NXOR");
        } else {
            errors.add("Expected XOR, or, and on line " + lookahead.getRow() + " row " + lookahead.getCol());
            return new Node("NUNDEF");
        }
    }

    // relop - <relop>  ::=  == | != | > | <= | < | >=

    public Node relop() {
        if (lookahead.getTokenNo() == Token.TEQEQ) {
            lookahead = tokenStream.remove(0);
            return new Node("NEQL");
        } else if (lookahead.getTokenNo() == Token.TNEQL) {
            lookahead = tokenStream.remove(0);
            return new Node("NNEQ");
        } else if (lookahead.getTokenNo() == Token.TLESS) {
            lookahead = tokenStream.remove(0);
            return new Node("NLSS");
        } else if (lookahead.getTokenNo() == Token.TGRTR) {
            lookahead = tokenStream.remove(0);
            return new Node("NGRT");
        } else if (lookahead.getTokenNo() == Token.TLEQL) {
            lookahead = tokenStream.remove(0);
            return new Node("NLEQ");
        } else if (lookahead.getTokenNo() == Token.TGEQL) {
            lookahead = tokenStream.remove(0);
            return new Node("NGEQ");
        } else {
            errors.add("Expected =, !=, >=, <=, >, or < on line " + lookahead.getRow() + " row " + lookahead.getCol());
            return new Node("NUNDEF");
        }
    }


    // expr - <expr>  ::= <term> <expr’>
    public Node expr() {
        Node node = term();
        return exprtail(node);
    }

    // exprtail - <expr’> ::= + <term> <expr’> | - <term> <expr’> | ε

    public Node exprtail(Node node_) {
        if (lookahead.getTokenNo() == Token.TPLUS) {
            lookahead = tokenStream.remove(0);
            return exprtail(new Node("NADD", node_, expr()));
        } else if (lookahead.getTokenNo() == Token.TMINS) {
            lookahead = tokenStream.remove(0);
            return exprtail(new Node("NSUB", node_, expr()));
        } else {
            return node_;
        }
    }

    // term - <term>  ::= <fact> <term’>
    public Node term() {
        Node node = fact();
        return termtail(node);
    }

    // term' - <term’> ::= * <fact> <term’> | / <fact> <term’> | % <fact> <term’> | ε

    public Node termtail(Node node_) {
        if (lookahead.getTokenNo() == Token.TSTAR) {
            lookahead = tokenStream.remove(0);
            return termtail(new Node("NMUL", node_, term()));
        } else if (lookahead.getTokenNo() == Token.TDIVD) {
            lookahead = tokenStream.remove(0);
            return termtail(new Node("NDIV", node_, term()));
        } else if (lookahead.getTokenNo() == Token.TPERC) {
            lookahead = tokenStream.remove(0);
            return termtail(new Node("NMOD", node_, term()));
        } else {
            return node_;
        }
    }

    // fact - <fact>   ::= <exponent> <fact’>
    public Node fact() {
        Node node = exponent();
        return facttail(node);
    }

    // facttail - <fact’> ::= ^ <exponent> <fact’> | ε

    public Node facttail(Node node_) {
        if (lookahead.getTokenNo() == Token.TCART) {
            lookahead = tokenStream.remove(0);
            return facttail(new Node("NPOW", node_, fact()));
        } else {
            return node_;
        }
    }

    // exponent - <exponent> ::= <var> | <intlit> | <reallit> | <fncall> | true | false
    // <exponent> ::= ( <bool> )
    public Node exponent() {
        Node node = new Node();

        if (lookahead.getTokenNo() == Token.TILIT) { // <intlit>
            Symbol symbol = new Symbol(lookahead.getLexeme(), Symbol.integer);
            node.setSymbolValue(lookahead.getLexeme());
            node.setSymbolType(Symbol.integer);
            table.insert(symbol);
            node.setNodeValue("NILIT");
            lookahead = tokenStream.remove(0);
            return node;
        } else if (lookahead.getTokenNo() == Token.TFLIT) { // <reallit>
            Symbol symbol = new Symbol(lookahead.getLexeme(), Symbol.real);
            node.setSymbolValue(lookahead.getLexeme());
            node.setSymbolType(Symbol.real);
            table.insert(symbol);
            node.setNodeValue("NFLIT");
            lookahead = tokenStream.remove(0);
            return node;
        } else if (lookahead.getTokenNo() == Token.TTRUE) { // true
            lookahead = tokenStream.remove(0);
            return new Node("NTRUE");
        } else if (lookahead.getTokenNo() == Token.TFALS) { // false
            lookahead = tokenStream.remove(0);
            return new Node("NFALS");
        } else if (lookahead.getTokenNo() == Token.TLPAR) { // ( <bool> )

            if (lookahead.getTokenNo() != Token.TLPAR) {
                errors.add("Expected ( on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TEQUL
                        && lookahead.getTokenNo() != Token.TNEQL && lookahead.getTokenNo() != Token.TGRTR && lookahead.getTokenNo() != Token.TLESS
                        && lookahead.getTokenNo() != Token.TGEQL && lookahead.getTokenNo() != Token.TLEQL && lookahead.getTokenNo() != Token.TPLUS
                        && lookahead.getTokenNo() != Token.TMINS && lookahead.getTokenNo() != Token.TSTAR && lookahead.getTokenNo() != Token.TDIVD
                        && lookahead.getTokenNo() != Token.TPERC && lookahead.getTokenNo() != Token.TCART) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            lookahead = tokenStream.remove(0);
            node = bool();
            if (lookahead.getTokenNo() != Token.TRPAR) {
                errors.add("Expected ) on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TEQUL
                        && lookahead.getTokenNo() != Token.TNEQL && lookahead.getTokenNo() != Token.TGRTR && lookahead.getTokenNo() != Token.TLESS
                        && lookahead.getTokenNo() != Token.TGEQL && lookahead.getTokenNo() != Token.TLEQL && lookahead.getTokenNo() != Token.TPLUS
                        && lookahead.getTokenNo() != Token.TMINS && lookahead.getTokenNo() != Token.TSTAR && lookahead.getTokenNo() != Token.TDIVD
                        && lookahead.getTokenNo() != Token.TPERC && lookahead.getTokenNo() != Token.TCART) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            lookahead = tokenStream.remove(0);
            return node;
        } else if (lookahead.getTokenNo() == Token.TIDEN) { // both var and fncall
            Token tempToken = lookahead;
            lookahead = tokenStream.remove(0);
            if (lookahead.getTokenNo() == Token.TLPAR) { // ( is the next thing for fncall
                return fncall(tempToken);
            } else {
                return varToken(tempToken);
            }
        } else {
            errors.add("Expected int, float, true, false, ( or an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TEQUL
                    && lookahead.getTokenNo() != Token.TNEQL && lookahead.getTokenNo() != Token.TGRTR && lookahead.getTokenNo() != Token.TLESS
                    && lookahead.getTokenNo() != Token.TGEQL && lookahead.getTokenNo() != Token.TLEQL && lookahead.getTokenNo() != Token.TPLUS
                    && lookahead.getTokenNo() != Token.TMINS && lookahead.getTokenNo() != Token.TSTAR && lookahead.getTokenNo() != Token.TDIVD
                    && lookahead.getTokenNo() != Token.TPERC && lookahead.getTokenNo() != Token.TCART) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }

    }

    // fncall - <fncall> ::= <id> ( <fncall’>
    // <fncall’> ::= <elist> ) | )

    public Node fncall(Token token_) {
        Node node = new Node("NFCALL");

        if (lookahead.getTokenNo() != Token.TIDEN && token_ == null) {
            errors.add("Expected an identifier on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TEQUL
                    && lookahead.getTokenNo() != Token.TNEQL && lookahead.getTokenNo() != Token.TGRTR && lookahead.getTokenNo() != Token.TLESS
                    && lookahead.getTokenNo() != Token.TGEQL && lookahead.getTokenNo() != Token.TLEQL && lookahead.getTokenNo() != Token.TPLUS
                    && lookahead.getTokenNo() != Token.TMINS && lookahead.getTokenNo() != Token.TSTAR && lookahead.getTokenNo() != Token.TDIVD
                    && lookahead.getTokenNo() != Token.TPERC && lookahead.getTokenNo() != Token.TCART) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }

        // if a the id token was already read create the corresponding symbol
        if (token_ != null) {
            Symbol symbol = new Symbol(token_.getLexeme(), Symbol.id);
            node.setSymbolValue(token_.getLexeme());
            node.setSymbolType(Symbol.id);
            table.insert(symbol);
        } else { // otherwise use the current lookahead token
            Symbol symbol = new Symbol(lookahead.getLexeme(), Symbol.id);
            node.setSymbolValue(lookahead.getLexeme());
            node.setSymbolType(Symbol.id);
            table.insert(symbol);
            lookahead = tokenStream.remove(0);
        }
        // todo store this function id
        if (lookahead.getTokenNo() != Token.TLPAR) {
            errors.add("Expected ( on line " + lookahead.getRow() + " row " + lookahead.getCol());
            while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TEQUL
                    && lookahead.getTokenNo() != Token.TNEQL && lookahead.getTokenNo() != Token.TGRTR && lookahead.getTokenNo() != Token.TLESS
                    && lookahead.getTokenNo() != Token.TGEQL && lookahead.getTokenNo() != Token.TLEQL && lookahead.getTokenNo() != Token.TPLUS
                    && lookahead.getTokenNo() != Token.TMINS && lookahead.getTokenNo() != Token.TSTAR && lookahead.getTokenNo() != Token.TDIVD
                    && lookahead.getTokenNo() != Token.TPERC && lookahead.getTokenNo() != Token.TCART) {
                lookahead = tokenStream.remove(0);
            }
            return new Node("NUNDEF");
        }
        lookahead = tokenStream.remove(0);

        if (lookahead.getTokenNo() == Token.TRPAR) {
            lookahead = tokenStream.remove(0);
            // check if its an empty list for this particular function if so good otherwise semantic error
        } else if (lookahead.getTokenNo() == Token.TNOTT || lookahead.getTokenNo() == Token.TIDEN || lookahead.getTokenNo() == Token.TILIT || lookahead.getTokenNo() == Token.TFLIT || lookahead.getTokenNo() == Token.TTRUE || lookahead.getTokenNo() == Token.TFALS) {
            node.setLeftSubTree(elist());


            // TODO check here for param num



            // check if the nodes in this subtree match the order and list of params given by the id
            if (lookahead.getTokenNo() != Token.TRPAR) {
                errors.add("Expected ) on line " + lookahead.getRow() + " row " + lookahead.getCol());
                while (lookahead.getTokenNo() != Token.TTEND  && lookahead.getTokenNo() != Token.TTEOF && lookahead.getTokenNo() != Token.TEQUL
                        && lookahead.getTokenNo() != Token.TNEQL && lookahead.getTokenNo() != Token.TGRTR && lookahead.getTokenNo() != Token.TLESS
                        && lookahead.getTokenNo() != Token.TGEQL && lookahead.getTokenNo() != Token.TLEQL && lookahead.getTokenNo() != Token.TPLUS
                        && lookahead.getTokenNo() != Token.TMINS && lookahead.getTokenNo() != Token.TSTAR && lookahead.getTokenNo() != Token.TDIVD
                        && lookahead.getTokenNo() != Token.TPERC && lookahead.getTokenNo() != Token.TCART) {
                    lookahead = tokenStream.remove(0);
                }
                return new Node("NUNDEF");
            }
            lookahead = tokenStream.remove(0);
        }
        return node;
    }


    // prlist - <prlist> ::= <printitem> <prlist’>
    // <prlist’> ::= , <prlist> | ε

    public Node prlist() {
        Node node = printitem();

        if (lookahead.getTokenNo() == Token.TCOMA) {
            lookahead = tokenStream.remove(0);
            return new Node("NPRLST", node, prlist());
        }
        return node;
    }

    // printitem - <printitem> ::= <expr> | <string>

    public Node printitem() {
        if (lookahead.getTokenNo() == Token.TSTRG) {
            Node node = new Node("NSTRG");
            Symbol symbol = new Symbol(lookahead.getLexeme(), Symbol.string);
            node.setSymbolValue(lookahead.getLexeme());
            node.setSymbolType(Symbol.string);
            table.insert(symbol);
            lookahead = tokenStream.remove(0);
            return node;
        } else {
            return expr();
        }
    }

//    private boolean arraySizeKnown(Node node) {
//
//    }

    private ArrayList<String> getParams(Node node, ArrayList<String> params) {


        // null check

        if (node == null) { // if a child is null simply return and continue
            return params;
        }

        // lhs

        // rhs

         if (node.getNodeValue().equals("NPLIST")) {
            params.add(0,node.getSymbolType()); // adding at the start so i once it finishes i dont have to invert it
        }


        return params;
    }

    private boolean preOrderExists(Node node) { // finds if a function has a return statement

        if (node == null) { // if a child is null simply return and continue
            return false;
        } else if (node.getNodeValue().equals("NRETN")) {
            return true;
        }

        // preorder traversal for all three possible children that might contain a return statement

        if (preOrderExists(node.getLeftSubTree())) {
            return true;
        } else if (preOrderExists(node.getMiddleSubTree())) {
            return true;
        } else if (preOrderExists(node.getRightSubTree())) {
            return true;
        } else {
            return false;
        }


    }

}
