import jamiebalfour.zpe.core.CovertibleFunction;
import jamiebalfour.zpe.core.IAST;
import jamiebalfour.zpe.core.YASSByteCodes;
import jamiebalfour.zpe.interfaces.ZPESyntaxTranspiler;

import java.util.HashMap;

public class Transpiler implements ZPESyntaxTranspiler {

  HashMap<String, String> yassToPythonFunctionMapping = new HashMap<>();

  @Override
  public String Transpile(CovertibleFunction[] covertibleFunctions, String s) {

    yassToPythonFunctionMapping.put("print", "print");
    yassToPythonFunctionMapping.put("input", "input");

    StringBuilder output = new StringBuilder();
    for(CovertibleFunction c : covertibleFunctions){
      IAST current = c.code;
      while(current != null){
        output.append(inner_transpile(current)).append(System.lineSeparator());
        current = current.next;
      }

    }

    return output.toString();

  }

  private String inner_transpile(IAST n){
    switch (n.type){
      case YASSByteCodes.IDENTIFIER: {
        return transpile_identifier(n);
      }
      case YASSByteCodes.VAR: {
        return transpile_var(n);
      }
      case YASSByteCodes.ASSIGN:{
        return transpile_assign(n);
      }
      case YASSByteCodes.EXPRESSION:{
        return transpile_expression(n);
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
    }
    return "";
  }

  private String transpile_identifier(IAST n){
    //Transpilation of a function call or whatever (anything with an identification)

    StringBuilder output = new StringBuilder();

    output.append(yassToPythonFunctionMapping.get(n.id));

    output.append("(");

    IAST current = (IAST) n.value;
    while(current != null){
      output.append(inner_transpile(current));
      current = current.next;
    }

    output.append(")");

    return output.toString();
  }

  private String transpile_var(IAST n){
    //Transpilation of a variable

    if(n.id.startsWith("$")){
      return n.id.substring(1);
    }

    return n.id;
  }

  private String transpile_assign(IAST n){
    return inner_transpile(n.middle) + " = " + inner_transpile((IAST) n.value);
  }

  private String transpile_expression(IAST n){
    //Transpilation of an expression
    IAST current = (IAST) n.value;
    return inner_transpile(current);
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

  @Override
  public String LanguageName() {
    return "Python";
  }
}