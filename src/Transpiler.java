import jamiebalfour.zpe.core.CovertibleFunction;
import jamiebalfour.zpe.core.IAST;
import jamiebalfour.zpe.core.YASSByteCodes;
import jamiebalfour.zpe.interfaces.ZPESyntaxTranspiler;

public class Transpiler implements ZPESyntaxTranspiler {
  @Override
  public String Transpile(CovertibleFunction[] covertibleFunctions, String s) {
    StringBuilder output = new StringBuilder();
    for(CovertibleFunction c : covertibleFunctions){
      IAST current = c.code;
      while(current != null){
        output.append(inner_transpile(current));
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
      case YASSByteCodes.EXPRESSION:{
        return transpile_expression(n);
      }
      case YASSByteCodes.STRING:{
        return "\"" + n.value.toString() + "\"";
      }
    }
    return "";
  }

  private String transpile_identifier(IAST n){
    //Transpilation of a function call or whatever (anything with an identification)

    StringBuilder output = new StringBuilder();

    output.append("print(");


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

  private String transpile_expression(IAST n){
    //Transpilation of an expression
    StringBuilder output = new StringBuilder();
    IAST current = (IAST) n.value;
    while(current != null){

      output.append(inner_transpile(current));
      current = current.next;
    }

    return output.toString();
  }

  @Override
  public String LanguageName() {
    return "Python";
  }
}