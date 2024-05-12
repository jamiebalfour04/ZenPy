import jamiebalfour.zpe.core.CovertibleFunction;
import jamiebalfour.zpe.core.IAST;
import jamiebalfour.zpe.core.YASSByteCodes;
import jamiebalfour.zpe.interfaces.ZPESyntaxTranspiler;

import java.util.HashMap;

public class Transpiler implements ZPESyntaxTranspiler {

  int indentation = 0;

  HashMap<String, String> yassToPythonFunctionMapping = new HashMap<>();

  @Override
  public String Transpile(IAST code, String s) {


    yassToPythonFunctionMapping.put("print", "print");
    yassToPythonFunctionMapping.put("input", "input");

    StringBuilder output = new StringBuilder();
    IAST current = code;
    while(current != null){
      output.append(inner_transpile(current)).append(System.lineSeparator());
      current = current.next;
    }



    return output.toString();

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
      case YASSByteCodes.VAR: {
        return transpile_var(n);
      }
      case YASSByteCodes.FUNCTION:{
        return transpile_function(n);
      }
      case YASSByteCodes.ASSIGN:{
        return transpile_assign(n);
      }
      case YASSByteCodes.EXPRESSION:{
        return transpile_expression(n);
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
      case YASSByteCodes.INT:
      case YASSByteCodes.DOUBLE:{
        return n.value.toString();
      }
      case YASSByteCodes.FOR_TO:{
        return transpile_for(n);
      }
      case YASSByteCodes.IF:{
        return transpile_if(n);
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

    output += " (" + generateParameters((IAST) n.value) + ")";

    return output;
  }

  private String transpile_var(IAST n){
    //Transpilation of a variable

    if(n.id.startsWith("$")){
      return n.id.substring(1);
    }

    return n.id;
  }

  private String transpile_function(IAST n){
    //Transpilation of a function
    StringBuilder output = new StringBuilder("def " + n.id + "(" + generateParameters((IAST)n.value) + ")" + ":" + System.lineSeparator());
    indentation++;

    IAST current = n.left;
    while(current != null){
      output.append(addIndentation()).append(inner_transpile(current)).append(System.lineSeparator());
      current = current.next;
    }

    indentation--;

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

  private String transpile_for(IAST n){
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

  private String transpile_if(IAST n){
    StringBuilder output = new StringBuilder("if " + inner_transpile((IAST)n.value) + ":" + System.lineSeparator());

    indentation++;

    IAST current = n.left;
    while(current != null){
      output.append(addIndentation()).append(inner_transpile(current)).append(System.lineSeparator());
      current = current.next;
    }

    indentation--;


    return output.toString();
  }
  @Override
  public String LanguageName() {
    return "Python";
  }
}