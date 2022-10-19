/*
Name: Alex James Waddell
Student Number: C3330987
Description: the symbol class, holds a symbol for semantic checking and code generation
 */

import java.util.ArrayList;

public class Symbol {
    private String type;
    private String name;
    private int value;
    private double dblval;
    private int base;
    private int offset;
    private int location;
    private int scope;
    private String attributes;
    private boolean declared;
    private ArrayList<String> parameterTypes;
    public static final String

    id = "ID", cD = "CD22", structID = "structID", typeID = "typeID",
    integer = "integer", real = "Float", bool = "Bool", func = "function",
    empty = "void", string = "string";


    public Symbol() {

    }

    public Symbol(String name_, String type_) {
        name = name_;
        type = type_;

    }

    public Symbol(String name_) {
        name = name_;
    }


    public void setDeclared(boolean declared_) {
        declared = declared_;
    }

    public boolean isDeclared() {
        return declared;
    }

    public void setParams(ArrayList<String> params) {
        parameterTypes = params;
    }

    public ArrayList<String> getParameterTypes() {
        return parameterTypes;
    }

    public int getLocation() {
        return location;
    }

    public int getScope() {
        return scope;
    }

    public String getAttributes() {
        return attributes;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public void setType(String type) {
        this.type = type;
    }

}
