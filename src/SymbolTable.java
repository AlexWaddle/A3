/*
Name: Alex James Waddell
Student Number: C3330987
Description: extremely bare bones class does not yet deal with scoping, just contains
one list of symbols
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    ArrayList<Symbol> table;
    ArrayList<ArrayList<Symbol>> tables;



    public SymbolTable() {
        tables = new ArrayList<>();
        table = new ArrayList<>();
    }

    public void insert(Symbol symbol_) {
        tables.get(0).add(symbol_); // gets the scope on top of the stack and adds to it
        table.add(symbol_);
    }

    // this will need to be changed for scoping
    public Symbol getSymbol (String lexeme) { // returns true if it exists within the array, false otherwise
        for (int i = 0; i < table.size(); i++) {
            if (table.get(i).getName().equals(lexeme)) {
                return table.get(i);
            }
        }
        return null;
    }

    public void create() {
        // create a new table and place it on top of the stack
    }

    public void destroy() {
        // remove the table on top of the stack
    }

//    public Symbol enter() {
//
//    }

    public void newScope() {

    }
//
//    public Symbol find() {
//        boolean found = false;
//
//        while (!found) {
//            found = true;
//        }
//
//    }

    // structureid attributes here store here

    // func attributes here store here

    public void set_attributes(Symbol symbol, String attributes) {

    }

    public String get_attributes(Symbol symbol) {
        String tempAttributes = "";

        return tempAttributes;
    }

}
