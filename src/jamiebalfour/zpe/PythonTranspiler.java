package jamiebalfour.zpe;

import jamiebalfour.zpe.core.IAST;
import jamiebalfour.zpe.core.YASSByteCodes;
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

  public String Transpile(IAST code, String s) {


    yassToPythonFunctionMapping.put("std_in", "print");
    yassToPythonFunctionMapping.put("auto_input", "input");
    yassToPythonFunctionMapping.put("floor", "math.floor");
    yassToPythonFunctionMapping.put("list_get_length", "len");


    pythonImports.put("floor", "math");
    pythonImports.put("ceiling", "math");



    StringBuilder output = new StringBuilder();
    IAST current = code;

    while(current != null){
      output.append(inner_transpile(current)).append(System.lineSeparator());
      current = current.next;
    }

    //Get all imports and add them
    String importStr = "";
    for(String i : imports){
      importStr += "import " + i + System.lineSeparator();
    }

    StringBuilder additionalFuncs = new StringBuilder();

    //This is temporary
    if(usedFunctions.contains("list_swap_elements")){
      additionalFuncs.append(
              "def list_swap_elements(l, a, b):\n" +
              "  tmp = l[a]\n" +
              "  l[a] = l[b]\n" +
              "  l[b] = tmp\n" +
              "  return l").append(System.lineSeparator());
    }

    if(usedFunctions.contains("map_get_keys")){
      additionalFuncs.append(
              "def map_get_keys(m):\n" +
                      "  return m.keys()").append(System.lineSeparator());
    }

    if(usedFunctions.contains("throw_error")){
      additionalFuncs.append(
              "def throw_error(msg):\n" +
                      "  raise Exception(msg)").append(System.lineSeparator());
    }



    return importStr + additionalFuncs + output;

  }

  private void addImport(String i){
    if(!imports.contains(i)){
      imports.add(i);
    }
  }

  private String addIndentation(){
    if(indentation > 0){
      StringBuilder out = new StringBuilder();
      for (int i = 0; i < indentation; i++){
        out.append("  ");
      }
      return out.toString();
    }
    return "";
  }

  private String inner_transpile(IAST n){
    switch (n.type){
      case YASSByteCodes.IDENTIFIER: {
        return transpile_identifier(n);
      }
      case YASSByteCodes.VAR:
      case YASSByteCodes.VAR_BY_REF:
      case YASSByteCodes.CONST:{
        return transpile_var(n);
      }
      case YASSByteCodes.FUNCTION:{
        return transpile_function(n);
      }
      case YASSByteCodes.BOOL:{
        //Deal with Python's uppercase boolean values
        return (n.value.toString().substring(0, 1)).toUpperCase() + n.value.toString().substring(1);
      }
      case YASSByteCodes.STRUCTURE:{
        return transpile_structure(n);
      }
      case YASSByteCodes.OBJECT_POINTER:{
        return inner_transpile(n.middle) + "." + inner_transpile((IAST) n.value);
      }
      case YASSByteCodes.THIS:{
        return "self";
      }
      case YASSByteCodes.NEW:{
        return n.id + "("+generateParameters((IAST) n.value)+")";
      }
      case YASSByteCodes.CONCAT:{
        return "str(" + inner_transpile(n.left) + ") + str(" + inner_transpile(n.next) + ")";
      }
      case YASSByteCodes.NULL:{
        return "None";
      }
      case YASSByteCodes.COUNT:{
        return "len(" + generateParameters(n.left) + ")";
      }
      case YASSByteCodes.ASSIGN:{
        return transpile_assign(n);
      }
      case YASSByteCodes.EXPRESSION:{
        return transpile_expression(n);
      }
      case YASSByteCodes.MATCH:{
        return transpile_match(n);
      }
      case YASSByteCodes.EQUAL:{
        return transpile_equal_to(n);
      }
      case YASSByteCodes.NEQUAL:{
        return transpile_not_equal_to(n);
      }
      case YASSByteCodes.GT:{
        return transpile_greater_than(n);
      }
      case YASSByteCodes.GTE:{
        return transpile_greater_than_or_equal(n);
      }
      case YASSByteCodes.LT:{
        return transpile_less_than(n);
      }
      case YASSByteCodes.LTE:{
        return transpile_less_than_or_equal(n);
      }
      case YASSByteCodes.LAND:{
        return transpile_and(n);
      }
      case YASSByteCodes.LOR:{
        return transpile_or(n);
      }
      case YASSByteCodes.MODULO:{
        return transpile_modulo(n);
      }
      case YASSByteCodes.PLUS:{
        return transpile_addition(n);
      }
      case YASSByteCodes.MINUS:{
        return transpile_subtraction(n);
      }
      case YASSByteCodes.MULT:{
        return transpile_multiplication(n);
      }
      case YASSByteCodes.DIVIDE:{
        return transpile_division(n);
      }
      case YASSByteCodes.STRING:{
        return "\"" + n.value.toString() + "\"";
      }
      case YASSByteCodes.LIST:{
        return "[" + generateParameters((IAST)n.value) + "]";
      }
      case YASSByteCodes.ASSOCIATION:{
        return transpile_map(n);
      }
      case YASSByteCodes.NEGATIVE: {
        return "-" + inner_transpile((IAST)n.value);
      }
      case YASSByteCodes.INT:
      case YASSByteCodes.DOUBLE:{
        return n.value.toString();
      }
      case YASSByteCodes.FOR:{
        return transpile_for(n);
      }
      case YASSByteCodes.FOR_TO:{
        return transpile_for_to(n);
      }
      case YASSByteCodes.IF:{
        return transpile_if(n);
      }
      case YASSByteCodes.WHILE:{
        return transpile_while(n);
      }
      case YASSByteCodes.TYPED_PARAMETER:{
        return inner_transpile(n.left);
      }
      case YASSByteCodes.INDEX_ACCESSOR:{
        return inner_transpile((IAST) n.left) + "[" + inner_transpile((IAST) n.value) + "]";
      }
      case YASSByteCodes.LBRA:{
        return "(" + inner_transpile((IAST) n.value) + ")";
      }
      case YASSByteCodes.RETURN:{
        return "return " + inner_transpile(n.left);
      }

    }
    return "";
  }

  private String generateParameters(IAST n){
    StringBuilder output = new StringBuilder();
    IAST current = n;
    while(current != null){
      output.append(inner_transpile(current));
      current = current.next;
      if(current != null){
        output.append(", ");
      }
    }
    return output.toString();
  }

  private String transpile_identifier(IAST n){
    //Transpilation of a function call or whatever (anything with an identification)

    String output = "";

    if(yassToPythonFunctionMapping.containsKey(n.id)){
      output += yassToPythonFunctionMapping.get(n.id);
    } else{
      output += n.id;
    }

    if(pythonImports.containsKey(n.id)){
      addImport(pythonImports.get(n.id));
    }

    if(ZPEKit.internalFunctionExists(n.id)){
      usedFunctions.add(n.id);
    }

    output += "(" + generateParameters((IAST) n.value) + ")";

    return output;
  }

  private String transpile_var(IAST n){
    //Transpilation of a variable

    if(n.id.startsWith("$")){
      return n.id.substring(1);
    }

    return n.id;
  }

  private String transpile_map(IAST n){

    StringBuilder output = new StringBuilder();

    output.append("{");

    IAST current = (IAST) n.value;
    while(current != null){
      output.append(inner_transpile(current));
      current = current.next;
      output.append(" : ");
      output.append(inner_transpile(current));
      current = current.next;
      if(current != null){
        output.append(", ");
      }
    }

    output.append("}");

    return output.toString();


  }

  public String transpile_match(IAST n){
    StringBuilder output = new StringBuilder();

    output.append("{");

    IAST current = n.left;
    while(current != null){
      output.append(inner_transpile(current.left));

      output.append(" : ");
      output.append(inner_transpile(current.middle));
      current = current.next;
      if(current != null){
        output.append(", ");
      }
    }

    output.append("} " + "[" + inner_transpile((IAST) n.value) + "]");

    return output.toString();
  }

  private String transpile_function(IAST n){
    //Transpilation of a function
    String params = generateParameters((IAST)n.value);
    //Stupid Python
    if(inClassDef){
      if(params.length() > 0){
        params = "self, " + params;
      } else{
        params = "self";
      }

    }

    StringBuilder output = new StringBuilder("def " + n.id + "(" + params + ")" + ":" + System.lineSeparator());
    indentation++;

    IAST current = n.left;
    while(current != null){
      output.append(addIndentation()).append(inner_transpile(current)).append(System.lineSeparator());
      current = current.next;
    }

    indentation--;

    return output.toString();
  }

  private String transpile_structure(IAST n){
    //Transpilation of a function
    inClassDef = true;
    StringBuilder output = new StringBuilder("class " + n.id + ":" + System.lineSeparator());
    indentation++;

    IAST current = (IAST) n.value;
    while(current != null){
      output.append(addIndentation()).append(inner_transpile(current)).append(System.lineSeparator());
      current = current.next;
    }

    indentation--;

    inClassDef = false;
    return output.toString();

  }


  private String transpile_assign(IAST n){
    return inner_transpile(n.middle) + " = " + inner_transpile((IAST) n.value);
  }

  private String transpile_expression(IAST n){
    //Transpilation of an expression
    IAST current = (IAST) n.value;
    return inner_transpile(current);
  }

  private String transpile_equal_to(IAST n){
    return inner_transpile(n.left) + " == " + inner_transpile(n.middle);
  }

  private String transpile_not_equal_to(IAST n){
    return inner_transpile(n.left) + " != " + inner_transpile(n.middle);
  }

  private String transpile_greater_than(IAST n){
    return inner_transpile(n.left) + " > " + inner_transpile(n.middle);
  }

  private String transpile_greater_than_or_equal(IAST n){
    return inner_transpile(n.left) + " >= " + inner_transpile(n.middle);
  }

  private String transpile_less_than(IAST n){
    return inner_transpile(n.left) + " < " + inner_transpile(n.middle);
  }

  private String transpile_less_than_or_equal(IAST n){
    return inner_transpile(n.left) + " <= " + inner_transpile(n.middle);
  }

  private String transpile_modulo(IAST n){
    return inner_transpile(n.left) + " % " + inner_transpile(n.next);
  }

  private String transpile_addition(IAST n){
    return inner_transpile(n.left) + " + " + inner_transpile(n.next);
  }

  private String transpile_subtraction(IAST n){
    return inner_transpile(n.left) + " - " + inner_transpile(n.next);
  }

  private String transpile_multiplication(IAST n){
    return inner_transpile(n.left) + " * " + inner_transpile(n.next);
  }

  private String transpile_division(IAST n){
    return inner_transpile(n.left) + " / " + inner_transpile(n.next);
  }

  private String transpile_and(IAST n){
    return inner_transpile(n.left) + " and " + inner_transpile(n.next);
  }

  private String transpile_or(IAST n){
    return inner_transpile(n.left) + " or " + inner_transpile(n.next);
  }

  private String transpile_for_to(IAST n){
    StringBuilder output = new StringBuilder("for " + inner_transpile(n.middle.left.middle) + " in range(" + inner_transpile((IAST) n.middle.left.value) + ", " + inner_transpile((IAST) ((IAST) n.value).value) + "):" + System.lineSeparator());

    indentation++;

    IAST current = n.left.next;
    while(current != null){
      output.append(addIndentation()).append(inner_transpile(current)).append(System.lineSeparator());
      current = current.next;
    }

    indentation--;


    return output.toString();
  }

  private String transpile_for(IAST n){
    String steps = "";
    if (n.middle.value instanceof String) {
      // Increasing the number of steps
      steps = ", " + n.middle.value;
    }
    StringBuilder output = new StringBuilder("for " + inner_transpile(n.middle.left.middle) + " in range(" + inner_transpile((IAST) n.middle.left.value) + ", " + inner_transpile(((IAST) ((IAST) n.value).value).middle) + steps + "):" + System.lineSeparator());

    indentation++;

    IAST current = n.left.next;
    while(current != null){
      output.append(addIndentation()).append(inner_transpile(current)).append(System.lineSeparator());
      current = current.next;
    }

    indentation--;


    return output.toString();
  }

  private String transpile_if(IAST n){
    StringBuilder output = new StringBuilder("if " + inner_transpile((IAST)n.value) + ":" + System.lineSeparator());

    indentation++;

    IAST current = n.left;
    while(current != null){
      output.append(addIndentation()).append(inner_transpile(current)).append(System.lineSeparator());
      current = current.next;
    }

    indentation--;

    //It could be else or else if
    if(n.middle != null){
      if(n.middle.type != YASSByteCodes.ELSEIF){

        //We need to ensure that the world else is also indented
        output.append(addIndentation()).append("else:").append(System.lineSeparator());

        indentation++;

        current = n.middle;
        while(current != null){
          output.append(addIndentation()).append(inner_transpile(current)).append(System.lineSeparator());
          current = current.next;
        }

        indentation--;
      }
    }


    return output.toString();
  }

  private String transpile_while(IAST n){
    StringBuilder output = new StringBuilder("while " + inner_transpile((IAST)n.value) + ":" + System.lineSeparator());

    indentation++;

    IAST current = n.left;
    while(current != null){
      output.append(addIndentation()).append(inner_transpile(current)).append(System.lineSeparator());
      current = current.next;
    }

    indentation--;


    return output.toString();
  }
}