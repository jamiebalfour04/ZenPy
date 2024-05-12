import jamiebalfour.zpe.core.IAST;
import jamiebalfour.zpe.interfaces.ZPESyntaxTranspiler;

import java.util.HashMap;

public class Transpiler implements ZPESyntaxTranspiler {

  @Override
  public String Transpile(IAST code, String s) {

    return new PythonTranspiler().Transpile(code, s);

  }

  @Override
  public String LanguageName() {
    return "Python";
  }
}