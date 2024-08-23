package jamiebalfour.zpe;

import jamiebalfour.HelperFunctions;
import jamiebalfour.zpe.core.IAST;
import jamiebalfour.zpe.core.YASSByteCodes;
import jamiebalfour.zpe.core.ZPE;
import jamiebalfour.zpe.core.ZPEKit;

import java.util.ArrayList;
import java.util.HashMap;

public class PythonTranspiler {

  int indentation = 0;
  boolean inClassDef = false;

  HashMap<String, String> yassToPythonFunctionMapping = new HashMap<>();
  HashMap<String, String> pythonImports = new HashMap<>();

  ArrayList<String> imports = new ArrayList<>();
  ArrayList<String> usedFunctions = new ArrayList<>();
  ArrayList<String> addedFunctions = new ArrayList<>();
  ArrayList<String> builtInFunctions = new ArrayList<>();

  public String Transpile(IAST code, String s) {


    yassToPythonFunctionMapping.put("std_in", "print");
    yassToPythonFunctionMapping.put("floor", "math.floor");
    yassToPythonFunctionMapping.put("factorial", "math.factorial");
    yassToPythonFunctionMapping.put("list_get_length", "len");
    yassToPythonFunctionMapping.put("time", "millitime");
    yassToPythonFunctionMapping.put("map_create_ordered", "");
    yassToPythonFunctionMapping.put("character_to_integer", "ord");
    yassToPythonFunctionMapping.put("integer_to_character", "chr");


    pythonImports.put("floor", "math");
    pythonImports.put("ceiling", "math");
    pythonImports.put("random_number", "random");
    pythonImports.put("time", "datetime");

    try{
      for (String fun : HelperFunctions.getResource("/jamiebalfour/zpe/additional_functions.txt", this.getClass()).split("--")) {
        fun = fun.trim();
        String funcName;
        String[] lines = fun.split(System.lineSeparator());
        funcName = lines[0];
        builtInFunctions.add(funcName);
      }
    } catch(Exception e){
      //Ignore
    }



    StringBuilder output = new StringBuilder();
    IAST current = code;

    while (current != null) {
      output.append(innerTranspile(current)).append(System.lineSeparator());
      current = current.next;
    }

    //Get all imports and add them
    StringBuilder importStr = new StringBuilder();
    for (String i : imports) {
      importStr.append("import ").append(i).append(System.lineSeparator());
    }


    StringBuilder additionalFuncs = new StringBuilder();

    try{
      for (String fun : HelperFunctions.getResource("/jamiebalfour/zpe/additional_functions.txt", this.getClass()).split("--")) {
        fun = fun.trim();
        String funcName;
        String[] lines = fun.split(System.lineSeparator());
        funcName = lines[0];

        StringBuilder funBuilder = new StringBuilder();
        for(int i = 1; i < lines.length; i++){
          funBuilder.append(lines[i]).append(System.lineSeparator());
        }

      /*if (!fun.isEmpty() && fun.charAt(0) == '\n') {
        fun = fun.substring(1);
      }
      int i = 4;
      while (i < fun.length() && fun.charAt(i) != ' ' && fun.charAt(i) != '(') {
        funcName.append(fun.charAt(i));
        i++;
      }*/
        if (usedFunctions.contains(funcName) && !(addedFunctions.contains(funcName))) {
          additionalFuncs.append(funBuilder);
        }
      }
    } catch (Exception e){
      //Ignore
    }


    ZPE.Print(System.lineSeparator());

    if(output.toString().contains("def main")){
      output.append(System.lineSeparator()).append("main()");
    }




    return importStr.toString() + additionalFuncs + output;

  }

  private void addImport(String i) {
    if (!imports.contains(i)) {
      imports.add(i);
    }
  }

  private String addIndentation() {
    if (indentation > 0) {
      StringBuilder out = new StringBuilder();
      for (int i = 0; i < indentation; i++) {
        out.append("  ");
      }
      return out.toString();
    }
    return "";
  }

  private String innerTranspile(IAST n) {
    switch (n.type) {
      case YASSByteCodes.IDENTIFIER: {
        return transpileIdentifier(n);
      }
      case YASSByteCodes.VAR:
      case YASSByteCodes.VAR_BY_REF:
      case YASSByteCodes.CONST: {
        return transpile_var(n);
      }
      case YASSByteCodes.FUNCTION: {
        return transpileFunction(n);
      }
      case YASSByteCodes.BOOL: {
        //Deal with Python's uppercase boolean values
        return (n.value.toString().substring(0, 1)).toUpperCase() + n.value.toString().substring(1);
      }
      case YASSByteCodes.STRUCTURE: {
        return transpileStructure(n);
      }
      case YASSByteCodes.OBJECT_POINTER: {
        return innerTranspile(n.middle) + "." + innerTranspile((IAST) n.value);
      }
      case YASSByteCodes.TYPE:{
        return transpileType(n);
      }
      case YASSByteCodes.THIS: {
        return "self";
      }
      case YASSByteCodes.NEW: {
        return transpileNew(n);
      }
      case YASSByteCodes.CONCAT: {
        return "str(" + innerTranspile(n.left) + ") + str(" + innerTranspile(n.next) + ")";
      }
      case YASSByteCodes.NULL: {
        return "None";
      }
      case YASSByteCodes.COUNT: {
        return "len(" + generateParameters(n.left) + ")";
      }
      case YASSByteCodes.NEGATION: {
        return "not(" + innerTranspile(((IAST) n.value).next) + ")";
      }
      case YASSByteCodes.ASSIGN: {
        return transpileAssign(n);
      }
      case YASSByteCodes.EXPRESSION: {
        return transpileExpression(n);
      }
      case YASSByteCodes.MATCH: {
        return transpileMatch(n);
      }
      case YASSByteCodes.EQUAL: {
        return transpileEqualTo(n);
      }
      case YASSByteCodes.NEQUAL: {
        return transpileNotEqualTo(n);
      }
      case YASSByteCodes.GT: {
        return transpileGreaterThan(n);
      }
      case YASSByteCodes.GTE: {
        return transpileGreaterThanOrEqual(n);
      }
      case YASSByteCodes.LT: {
        return transpileLessThan(n);
      }
      case YASSByteCodes.LTE: {
        return transpileLessThanOrEqual(n);
      }
      case YASSByteCodes.LAND: {
        return transpileAnd(n);
      }
      case YASSByteCodes.LOR: {
        return transpileOr(n);
      }
      case YASSByteCodes.MODULO: {
        return transpileModulo(n);
      }
      case YASSByteCodes.CIRCUMFLEX: {
        return transpileCircumflex(n);
      }
      case YASSByteCodes.PLUS: {
        return transpileAddition(n);
      }
      case YASSByteCodes.MINUS: {
        return transpileSubtraction(n);
      }
      case YASSByteCodes.MULT: {
        return transpileMultiplication(n);
      }
      case YASSByteCodes.DIVIDE: {
        return transpileDivision(n);
      }
      case YASSByteCodes.STRING: {
        return "\"" + n.value.toString().replace('"', '\'') + "\"";
      }
      case YASSByteCodes.PRE_INCREMENT:
      case YASSByteCodes.POST_INCREMENT: {
        return transpile_var(n) + " += " + "1";
      }
      case YASSByteCodes.PRE_DECREMENT:
      case YASSByteCodes.POST_DECREMENT: {
        return transpile_var(n) + " -= " + "1";
      }
      case YASSByteCodes.DOT: {
        return transpileDotExpression(n);
      }
      case YASSByteCodes.LIST: {
        return "[" + generateParameters((IAST) n.value) + "]";
      }
      case YASSByteCodes.ASSOCIATION: {
        return transpileMap(n);
      }
      case YASSByteCodes.NEGATIVE: {
        return "-" + innerTranspile((IAST) n.value);
      }
      case YASSByteCodes.INT:
      case YASSByteCodes.DOUBLE: {
        return n.value.toString();
      }
      case YASSByteCodes.FOR: {
        return transpileFor(n);
      }
      case YASSByteCodes.FOR_TO: {
        return transpileForTo(n);
      }
      case YASSByteCodes.EACH: {
        return transpileForEach(n);
      }
      case YASSByteCodes.IF: {
        return transpileIf(n);
      }
      case YASSByteCodes.WHILE: {
        return transpileWhile(n);
      }
      case YASSByteCodes.TYPED_PARAMETER: {
        return innerTranspile(n.left);
      }
      case YASSByteCodes.INDEX_ACCESSOR: {
        return innerTranspile((IAST) n.left) + "[" + innerTranspile((IAST) n.value) + "]";
      }
      case YASSByteCodes.LBRA: {
        return "(" + innerTranspile((IAST) n.value) + ")";
      }
      case YASSByteCodes.RETURN: {
        return "return " + innerTranspile(n.left);
      }
      case YASSByteCodes.EMPTY: {
        return "len(" + innerTranspile((IAST) n.left) + ") == 0";
      }

    }
    return "";
  }

  private String generateParameters(IAST n) {
    StringBuilder output = new StringBuilder();
    IAST current = n;

    while (current != null) {
      if(current.type == YASSByteCodes.INFINITE_PARAMETERS){
        output.append("*arg");
      }
      output.append(innerTranspile(current));
      current = current.next;
      if (current != null) {
        output.append(", ");
      }
    }
    return output.toString();
  }

  private String transpileIdentifier(IAST n) {
    //Transpilation of a function call or whatever (anything with an identification)

    String output = "";
    String properName = n.id;

    if (yassToPythonFunctionMapping.containsKey(n.id)) {
      output += yassToPythonFunctionMapping.get(n.id);
      properName = yassToPythonFunctionMapping.get(n.id);
    } else {
      output += n.id;
    }

    if (pythonImports.containsKey(n.id)) {
      addImport(pythonImports.get(n.id));
    }

    if (ZPEKit.internalFunctionExists(properName) || builtInFunctions.contains(properName)) {
      usedFunctions.add(properName);
    }

    output += "(" + generateParameters((IAST) n.value) + ")";

    return output;
  }

  private String checkId(String id) {
    if (id.equals("len")) {
      id = "leng";
    }
    return id;
  }

  private String transpile_var(IAST n) {
    //Transpilation of a variable

    String id = n.id;

    if(n.id.equals(argsReplacement)){
      id = "arg";
    }

    if (id.startsWith("$")) {
      id = id.substring(1);
    }

    id = checkId(id);


    return id;
  }

  private String transpileDotExpression(IAST n) {

    if (((IAST) n.value).id.equals("put")) {
      usedFunctions.add("_put");
      return "_put(" + innerTranspile(n.left) + ", " + generateParameters((IAST) ((IAST) n.value).value) + ")";
    } else if (((IAST) n.value).id.equals("length")) {
      return "len(" + innerTranspile(n.left) + ")";
    } else {
      return innerTranspile(n.left) + "." + innerTranspile((IAST) n.value);
    }


  }

  private String transpileType(IAST n){

    usedFunctions.add("typeOf");

    return "typeOf (" + innerTranspile((IAST) n.value) + ")";

  }

  private String transpileMap(IAST n) {

    StringBuilder output = new StringBuilder();

    output.append("{");

    IAST current = (IAST) n.value;
    while (current != null) {
      output.append(innerTranspile(current));
      current = current.next;
      output.append(" : ");
      output.append(innerTranspile(current));
      current = current.next;
      if (current != null) {
        output.append(", ");
      }
    }

    output.append("}");

    return output.toString();


  }

  public String transpileMatch(IAST n) {
    StringBuilder output = new StringBuilder();

    output.append("{");

    IAST current = n.left;
    while (current != null) {
      output.append(innerTranspile(current.left));

      output.append(" : ");
      output.append(innerTranspile(current.middle));
      current = current.next;
      if (current != null) {
        output.append(", ");
      }
    }

    output.append("} " + "[").append(innerTranspile((IAST) n.value)).append("]");

    return output.toString();
  }

  String argsReplacement = null;
  private String transpileFunction(IAST n) {

    IAST current = (IAST) n.value;

    //Search for any infinite params and tell the transpiler to sort them later
    while (current != null) {
      if(current.type == YASSByteCodes.INFINITE_PARAMETERS){
        argsReplacement = current.id;
        current.id = "*arg";
      }
      current = current.next;
    }

    //Transpilation of a function
    String params = generateParameters((IAST) n.value);
    //Stupid Python
    if (inClassDef) {
      if (!params.isEmpty()) {
        params = "self, " + params;
      } else {
        params = "self";
      }

    }

    String id = n.id;
    //Remove namespaces
    if (n.id.contains("/")) {
      id = n.id.replace("/", "_");
    }

    if (id.equals("_construct")) {
      id = "__init__";
    }
    /*if (id.equals("_output")) {
      id = "__str__";
    }*/

    addedFunctions.add(id);
    StringBuilder output = new StringBuilder("def " + id + "(" + params + ")" + ":" + System.lineSeparator());
    indentation++;

    current = n.left;
    while (current != null) {
      output.append(addIndentation()).append(innerTranspile(current)).append(System.lineSeparator());
      current = current.next;
    }

    indentation--;
    argsReplacement = null;

    return output.toString();
  }

  private String transpileStructure(IAST n) {
    //Transpilation of a function
    inClassDef = true;

    String id = n.id;
    //Remove namespaces
    if (n.id.contains("/")) {
      id = n.id.replace("/", "_");
    }

    StringBuilder output = new StringBuilder("class " + id + ":" + System.lineSeparator());
    indentation++;


    IAST current = (IAST) n.value;
    while (current != null) {
      output.append(addIndentation()).append(innerTranspile(current)).append(System.lineSeparator());
      current = current.next;
    }

    indentation--;

    inClassDef = false;
    return output.toString();

  }

  private String transpileNew(IAST n) {
    String id = n.id;
    //Remove namespaces
    if (n.id.contains("/")) {
      id = n.id.replace("/", "_");
    }

    return id + "(" + generateParameters((IAST) n.value) + ")";
  }

  private String transpileAssign(IAST n) {
    return innerTranspile(n.middle) + " = " + innerTranspile((IAST) n.value);
  }

  private String transpileExpression(IAST n) {
    //Transpilation of an expression
    IAST current = (IAST) n.value;
    return innerTranspile(current);
  }

  private String transpileEqualTo(IAST n) {
    return innerTranspile(n.left) + " == " + innerTranspile(n.middle);
  }

  private String transpileNotEqualTo(IAST n) {
    return innerTranspile(n.left) + " != " + innerTranspile(n.middle);
  }

  private String transpileGreaterThan(IAST n) {
    return innerTranspile(n.left) + " > " + innerTranspile(n.middle);
  }

  private String transpileGreaterThanOrEqual(IAST n) {
    return innerTranspile(n.left) + " >= " + innerTranspile(n.middle);
  }

  private String transpileLessThan(IAST n) {
    return innerTranspile(n.left) + " < " + innerTranspile(n.middle);
  }

  private String transpileLessThanOrEqual(IAST n) {
    return innerTranspile(n.left) + " <= " + innerTranspile(n.middle);
  }

  private String transpileModulo(IAST n) {
    return innerTranspile(n.left) + " % " + innerTranspile(n.next);
  }

  private String transpileCircumflex(IAST n) {
    return "pow (" + innerTranspile(n.left) + ", " + innerTranspile(n.next) + ")";
  }

  private String transpileAddition(IAST n) {
    return innerTranspile(n.left) + " + " + innerTranspile(n.next);
  }

  private String transpileSubtraction(IAST n) {
    return innerTranspile(n.left) + " - " + innerTranspile(n.next);
  }

  private String transpileMultiplication(IAST n) {
    return innerTranspile(n.left) + " * " + innerTranspile(n.next);
  }

  private String transpileDivision(IAST n) {
    return innerTranspile(n.left) + " / " + innerTranspile(n.next);
  }

  private String transpileAnd(IAST n) {
    return innerTranspile(n.left) + " and " + innerTranspile(n.next);
  }

  private String transpileOr(IAST n) {
    return innerTranspile(n.left) + " or " + innerTranspile(n.next);
  }

  private String transpileForTo(IAST n) {
    StringBuilder output = new StringBuilder("for " + innerTranspile(n.middle.left.middle) + " in range(" + innerTranspile((IAST) n.middle.left.value) + ", " + innerTranspile((IAST) ((IAST) n.value).value) + "):" + System.lineSeparator());

    indentation++;

    IAST current = n.left.next;
    while (current != null) {
      output.append(addIndentation()).append(innerTranspile(current)).append(System.lineSeparator());
      current = current.next;
    }

    indentation--;


    return output.toString();
  }

  private String transpileForEach(IAST n) {
    String mid;
    if (n.left.middle.type == YASSByteCodes.VAR) {
      mid = n.left.middle.value.toString().replace("$", "");
    } else {
      mid = innerTranspile(n.left.middle);
    }
    StringBuilder output = new StringBuilder("for " + mid + " in " + innerTranspile(n.left.left) + ":" + System.lineSeparator());

    indentation++;

    IAST current = n.middle;
    while (current != null) {
      output.append(addIndentation()).append(innerTranspile(current)).append(System.lineSeparator());
      current = current.next;
    }

    indentation--;


    return output.toString();
  }

  private String transpileFor(IAST n) {
    String steps = "";
    if (n.middle.value instanceof String) {
      // Increasing the number of steps
      steps = ", " + n.middle.value;
    }
    StringBuilder output = new StringBuilder("for " + innerTranspile(n.middle.left.middle) + " in range(" + innerTranspile((IAST) n.middle.left.value) + ", " + innerTranspile(((IAST) ((IAST) n.value).value).middle) + steps + "):" + System.lineSeparator());

    indentation++;

    IAST current = n.left.next;
    while (current != null) {
      output.append(addIndentation()).append(innerTranspile(current)).append(System.lineSeparator());
      current = current.next;
    }

    indentation--;


    return output.toString();
  }

  private String transpileIf(IAST n) {
    StringBuilder output = new StringBuilder("if " + innerTranspile((IAST) n.value) + ":" + System.lineSeparator());

    indentation++;

    IAST current = n.left;
    while (current != null) {
      output.append(addIndentation()).append(innerTranspile(current)).append(System.lineSeparator());
      current = current.next;
    }

    indentation--;

    //It could be else or else if
    if (n.middle != null) {
      IAST currentInner = n.middle;
      while(currentInner != null){
        if (currentInner.type == YASSByteCodes.ELSE) {

          //We need to ensure that the world else is also indented
          output.append(addIndentation()).append("else:").append(System.lineSeparator());

          indentation++;

          current = currentInner.left;
          while (current != null) {
            output.append(addIndentation()).append(innerTranspile(current)).append(System.lineSeparator());
            current = current.next;
          }

          indentation--;
        } else if(currentInner.type == YASSByteCodes.ELSEIF){

          //We need to ensure that the world else is also indented
          output.append(addIndentation()).append("elif ").append(innerTranspile((IAST) currentInner.value)).append(":").append(System.lineSeparator());

          indentation++;

          current = currentInner.left;
          while (current != null) {
            output.append(addIndentation()).append(innerTranspile(current)).append(System.lineSeparator());
            current = current.next;
          }

          indentation--;
        }
        currentInner = currentInner.next;
      }

    }


    return output.toString();
  }

  private String transpileWhile(IAST n) {
    StringBuilder output = new StringBuilder("while " + innerTranspile((IAST) n.value) + ":" + System.lineSeparator());

    indentation++;

    IAST current = n.left;
    while (current != null) {
      output.append(addIndentation()).append(innerTranspile(current)).append(System.lineSeparator());
      current = current.next;
    }

    indentation--;


    return output.toString();
  }
}