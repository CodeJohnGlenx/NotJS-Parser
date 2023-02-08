import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
public class Parser {
    static ArrayList<String> lexemes = new ArrayList<>();
    static ArrayList<String> tokens = new ArrayList<>();
    static int pointer = 0;
    static String lexeme;
    static String token; 

    public static void main(String[] args) {
        try {
            // convert .njs file into Java String
            Scanner scanner = new Scanner(new File(args[0]));

            String fileExtension = args[0].substring(args[0].indexOf('.'));

            // throw Exception if file extension is not ".njs"
            if (!fileExtension.equals(".nlex")) {
                throw new Exception("Input File only accepts \".nlex\" extension!");
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                int delIndex = line.lastIndexOf(",");
                String[] lexTokenSplit = {line.substring(0, delIndex), line.substring(delIndex+1)}; 
                lexemes.add(lexTokenSplit[0]);
                tokens.add(lexTokenSplit[1]);
            }           
            
            if (lexemes.size() > 0 && tokens.size() > 0) {
                // perform parsing
                lexeme = getLexeme();
                token = getToken();
                if (program() && lexemes.isEmpty() && tokens.isEmpty()) {
                    System.out.println("Syntax Correct");
                } else {
                    System.out.println("Syntax Error");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean program() {
        if (lexemeCheck("void") && lexemeCheck("main") && lexemeCheck("(") && lexemeCheck(")") 
        && lexemeCheck("{") && mainBody() && lexemeCheck("}")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean mainBody() {
        if (fieldDeclaration()) {
            return true;
        } else if (tokens.isEmpty() || lexemes.isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean lexemeCheck(String s) {
        if (lexeme.equals(s)) {
            consume();
            return true;
        } else {
            return false;
        }
    }


    public static boolean tokenCheck(String s) {
        if (token.equals(s)) {
            consume();
            return true;
        } else {
            return false;
        }
    }

    // <field declaration> ::= <field modifier>? <type> <variable declarators>;
    public static boolean fieldDeclaration() {
        // add <field modifier> later
        if (type()) {
            if (variableDeclarators()) {
                if (tokenCheck("SEMICOLON_DEL")) {
                    return true;
                }
            }
        }
        return false;
    }

    // lexemeCheck("final")
    //<field modifier> ::= final
    public static boolean fieldModifier() {
        return false;
    }

    // <type> ::= int | float | char | boolean | String
    public static boolean type() {
        String [] types = {"int", "float", "char", "bool", "String"};
        for (String t : types) {
            if (lexemeCheck(t)) {
                return true;
            }
        }
        return false;
    }

    // <variable declarators> ::= <variable declarator> | <variable declarator>, <variable declarators> 
    public static boolean variableDeclarators() {
        if (variableDeclarator()) {
            if (tokenCheck("COMMA_DEL")) {
                if(variableDeclarators()) {
                    return true;
                } else {
                    return false;
                }
            } 
            return true;
        }
        return false;
    }

    // <variable declarator> ::= <variable declarator id> | <variable declarator id> = <variable initializer>
    public static boolean variableDeclarator() {
        if (variableDeclaratorId()) {
            if (tokenCheck("ASSIGNMENT_OP")) {
                if (variableInitializer()) {
                    return true;
                } 
                return false;
            } 
            return true;
        }
        return false;
    }

    // <variable declarator id> ::= <identifier>
    public static boolean variableDeclaratorId() {
        if (tokenCheck("IDENTIFIER")) {
            return true;
        }
        return false;
    }

    // <variable initializer> ::= <expression> | <variable declarator id>
    public static boolean variableInitializer() {
        if (expression()) {
            return true;
        }
        return false;
    }


    public static boolean stringLiteral() {
        if (tokenCheck("OPEN_DOUBLE_QUOTE_DEL")) {
            stringStream();
            if (tokenCheck("CLOSE_DOUBLE_QUOTE_DEL")) {
                return true;
            }
        }
        return false;
    }

    /*
     <string stream> := STRING_LITERAL <string stream> | NEWLINE_ESC <string stream> | 
     HORIZONAL_TAB_ESC <string stream> | DOUBLE_QUOTE_ESC <string stream> | epsilon
     */
    public static boolean stringStream() {
        boolean flag = false;
        String[] streams = {"STRING_LITERAL", "NEWLINE_ESC", "HORIZONAL_TAB_ESC", "DOUBLE_QUOTE_ESC"};
        for (String s: streams) {
            if (tokenCheck(s)) {
                flag = true;
                break;
            }
        }
        if (flag) {
            stringStream();
        }
        return true; 
    }

    

    // <expression> :: INT_LITERAL | FLOAT_LITERAL | CHAR_LITERAL | BOOL_LITERAL | STRING_LITERAL
    public static boolean expression() {
        String [] literals = {"INT_LITERAL", "FLOAT_LITERAL", "CHAR_LITERAL", "BOOL_LITERAL"};
        for (String literal : literals) {
            if (tokenCheck(literal)) {
                return true;
            }
        }
        if (variableDeclaratorId()) {
            return true;
        } else if (stringLiteral()) {
            return true;
        }
        return false;
    }


    public static String getLexeme() {
        return lexemes.get(0);
    }

    public static String getToken() {
       return tokens.get(0);
    }

    public static void consume() {
        lexemes.remove(0);
        tokens.remove(0);
        if (lexemes.isEmpty() && tokens.isEmpty()) {
            lexeme = "";
            token = "";
        } else {
            lexeme = getLexeme();
            token= getToken();
        }
    }



}