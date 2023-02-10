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
        if (constantExpression()) {
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

    // <constant expression> ::= <expression>
    public static boolean constantExpression() {
        if (expression()) {
            return true;
        }
        return false;
    }

    // <expression> ::= <assignment expression>
    public static boolean expression() {
        if (assignmentExpression()) {
            return true;
        }
        return false;
    }

    // <assignment expression> ::= <conditional expression> | <assignment>
    public static boolean assignmentExpression() {
        String[] assignOps = {"ASSIGNMENT_OP", "ADDITION_ASSIGNMENT_OP", "SUBTRACTION_ASSIGNMENT_OP", "MULTIPLICATION_ASSIGNMENT_OP",
        "DIVISION_ASSIGNMENT_OP", "MODULUS_ASSIGNMENT_OP", "EXPONENTIATION_ASSIGNMENT_OP", "FLOOR_DIVISION_ASSIGNMENT_OP"};
        for (String op: assignOps) {
            if (tokens.get(0).equals("IDENTIFIER") && tokens.get(1).equals(op)) {
                if (assignment()) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        if (conditionalExpression()) {
            return true;
        }
        return false;
    }

    // <assignment> ::= <left hand side> <assignment operator> <assignment expression>
    public static boolean assignment() {
        if (leftHandSide()) {
            if (assignmentOperator()) {
                if (assignmentExpression()) {
                    return true;
                }
            }
        }
        return false;
    }

    // <left hand side> ::= <expression name>
    public static boolean leftHandSide() {
        if (expressionName()) {
            return true;
        }
        return false;
    }

    // <assignment operator> ::= = | += | -= | *= | /= | %= | **= | //=
    public static boolean assignmentOperator() { 
        String[] assignOps = {"ASSIGNMENT_OP", "ADDITION_ASSIGNMENT_OP", "SUBTRACTION_ASSIGNMENT_OP", "MULTIPLICATION_ASSIGNMENT_OP",
        "DIVISION_ASSIGNMENT_OP", "MODULUS_ASSIGNMENT_OP", "EXPONENTIATION_ASSIGNMENT_OP", "FLOOR_DIVISION_ASSIGNMENT_OP"};
        for (String op: assignOps) {
            if (tokenCheck(op)) {
                return true;
            }
        }
        return false;
    }

    // <conditional expression> ::= <conditional or expression> | <conditional or expression> ? <expression> : <conditional expression>
    public static boolean conditionalExpression() { 
        if (conditionalOrExpression()) {
            if (tokenCheck("TERNARY_OP")) {
                if (expression()) {
                    if (tokenCheck("COLON_DEL")) {
                        if (conditionalExpression()) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // <conditional or expression> ::= <conditional and expression> || <conditional or expression> || <conditional and expression>
    public static boolean conditionalOrExpression() { 
        if (conditionalAndExpression()) {
            if (tokenCheck("OR_OP")) {
                if (conditionalOrExpression()) {
                    return true;
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    // <conditional and expression> ::= <and expression> && <conditional and expression> | <and expression>
    public static boolean conditionalAndExpression() { 
        if (andExpression()) {
            if (tokenCheck("AND_OP")) {
                if (conditionalAndExpression()) {
                    return true;
                } else { 
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // <and expression> ::= <equality expression>
    public static boolean andExpression() {
        if (equalityExpression()) {
            return true;
        }
        return false;
    }

    // <equality expression> ::= <relational expression> == <relational expression> | <relational expression> != <relational expression> | <relational expression>
    public static boolean equalityExpression() {
        if (relationalExpression()) {
            if (token.equals("EQUAL_TO_OP") || token.equals("NOT_EQUAL_TO_OP")) {
                if (tokenCheck(token)) {
                    if (relationalExpression()) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    // <relational expression> ::= <shift expression> < <relational expression> | <shift expression> > <relational expression> | <shift expression> <= <relational expression> | <shift expression> >= <relational expression> | <shift expression>
    public static boolean relationalExpression() {
        if (shiftExpression()) {
            if (token.equals("GREATER_THAN_OP") || token.equals("GREATER_THAN_EQUAL_TO_OP") ||
            token.equals("LESS_THAN_OP") || token.equals("LESS_THAN_EQUAL_TO_OP")) {
                if (tokenCheck(token)) {
                    if (shiftExpression()) {
                        return true;
                    } else {
                        return false;
                    }
                } 
            }
            return true;
        }
        return false; 
    }

    // <shift expression> ::= <additive expression>
    public static boolean shiftExpression() { 
        if (additiveExpression()) {
            return true;
        }
        return false;
    }

    // <additive expression> ::= <multiplicative expression> + <additive expression> | <multiplicative expression> - <additive expression> | <multiplicative expression>
    public static boolean additiveExpression() { 
        if (multiplicativeExpression()) {
            if (token.equals("ADDITION_OP") || token.equals("SUBTRACTION_OP")) {
                if (tokenCheck(token)) {
                    if (additiveExpression()) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }


    // <multiplicative expression> ::= <unary expression> * <multiplicative expression> |  <unary expression> / <multiplicative expression>  |  <unary expression> % <multiplicative expression>  |  <unary expression> ** <multiplicative expression> |  <unary expression> // <multiplicative expression> | <unary expression> 
    public static boolean multiplicativeExpression() { 
        if (unaryExpression()) {
            if (token.equals("MULTIPLICATION_OP") || token.equals("EXPONENTIATION_OP") || 
            token.equals("FLOOR_DIVISION_OP") || token.equals("DIVISION_OP") || token.equals("MODULUS_OP")) {
                if (tokenCheck(token)) {
                    if (multiplicativeExpression()) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } 
            return true;
        }
        return false;
    }

    // <unary expression> ::= <preincrement expression> | <predecrement expression> | + <unary expression> | - <unary expression> | <unary expression not plus minus>
    public static boolean unaryExpression() { 
        if (preIncrementExpression()) {
            return true;
        } else if (preDecrementExpression()) {
            return true;
        } else if (tokenCheck("ADDITION_OP")) {
            if (unaryExpression()) {
                return true;
            }
        } else if (tokenCheck("SUBTRACTION_OP")) {
            if (unaryExpression()) {
                return true;
            }
        } else if (unaryExpressionNotPlusMinus()) {
            return true;
        }
        return false;
    }

    // <predecrement expression> ::= -- <unary expression>
    public static boolean preDecrementExpression() { 
        if (tokenCheck("DECREMENT")) {
            if (unaryExpression()) {
                return true;
            }
        }
        return false;
    }

    // <preincrement expression> ::= ++ <unary expression>
    public static boolean preIncrementExpression() { 
        if (tokenCheck("INCREMENT")) {
            if (unaryExpression()) {
                return true;
            }
        }
        return false;
    }

    // <unary expression not plus minus> ::= ! <unary expression> | <postfix expression>
    public static boolean unaryExpressionNotPlusMinus() {
        if (tokenCheck("NOT_OP")) {
            if (unaryExpression()) {
                return true;
            }
        } else if (postfixExpression()) {
            return true;
        }
        return false;
    }

    // <postdecrement expression> ::= <postfix expression> --
    public static boolean postDecrementExpression() {
        if (tokenCheck("IDENTIFIER")) {
            if (tokenCheck("DECREMENT")) {
                return true;
            }
        }
        return false;
    }

    // <postincrement expression> ::= <postfix expression> ++
    public static boolean postIncrementExpression() {
        if (tokenCheck("IDENTIFIER")) {
            if (tokenCheck("INCREMENT")) {
                return true;
            }
        }
        return false;
    }

    // <postfix expression> ::= <primary> | <postincrement expression> | <postdecrement expression> | <expression name> 
    public static boolean postfixExpression() {
        // <primary>
        if (primary()) {
            return true;
        } else if (tokens.size() > 1 && tokens.get(0).equals("IDENTIFIER") && tokens.get(1).equals("INCREMENT")) {
            postIncrementExpression();
            return true;
        } else if (tokens.size() > 1 && tokens.get(0).equals("IDENTIFIER") && tokens.get(1).equals("DECREMENT")) {
            postDecrementExpression();
            return true;
        } else if (expressionName()) {
            return true;
        }
        // <expression name>
        return false;
    }

    // <primary> ::= <primary no new array>
    public static boolean primary() {
        if (primaryNoNewArray()) {
            return true;
        }
        return false;
    }

    // <primary no new array> ::= <literal> | ( <expression> ) |  <method invocation> 
    public static boolean primaryNoNewArray() {
        // literal 
        if (literal()) {
            return true;
        // (expression)
        } else if (tokenCheck("OPEN_PARENTHESIS_DEL")) {
            if (expression()) {
                if (tokenCheck("CLOSE_PARENTHESIS_DEL")) {
                    return true;
                }
            }
        }
        // method invocation 
        return false;
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

    // <string literal> ::= "<stringStream>"
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

    
    /* 
    // <expression> :: INT_LITERAL | FLOAT_LITERAL | CHAR_LITERAL | BOOL_LITERAL | STRING_LITERAL
    public static boolean expression() {
        String [] literals = {"INT_LITERAL", "FLOAT_LITERAL", "BOOL_LITERAL"};
        for (String literal : literals) {
            if (tokenCheck(literal)) {
                return true;
            }
        }

        if (stringLiteral()) {
            return true;
        } else if (charLiteral()) {
            return true;
        }
            return false;
    }
    */

    // <expression name> ::= <identifier>
    public static boolean expressionName() {
        if (tokenCheck("IDENTIFIER")) {
            return true;
        }
        return false;
    }

    // <expression> :: INT_LITERAL | FLOAT_LITERAL | CHAR_LITERAL | BOOL_LITERAL | STRING_LITERAL
    public static boolean literal() {
        String [] literals = {"INT_LITERAL", "FLOAT_LITERAL", "BOOL_LITERAL"};
        for (String literal : literals) {
            if (tokenCheck(literal)) {
                return true;
            }
        }

        if (stringLiteral()) {
            return true;
        } else if (charLiteral()) {
            return true;
        }
            return false;
        }

    /*
     <char literal> := SINGLE_QUOTE_ESC | NEWLINE_ESC | HORIZONAL_TAB_ESC | CHAR_LITERAL
     */
    public static boolean charLiteral() {
        String[] streams = {"SINGLE_QUOTE_ESC", "NEWLINE_ESC", "HORIZONAL_TAB_ESC", "CHAR_LITERAL"};
        if (tokenCheck("OPEN_SINGLE_QUOTE_DEL")) {
            for (String s: streams) {
                if (tokenCheck(s)) {
                    if (tokenCheck("CLOSE_SINGLE_QUOTE_DEL")) {
                        return true;
                    }
                }
            }
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

/*
 To do: <method invocation> found in <primary no new array>
 */