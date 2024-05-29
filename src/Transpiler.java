import jamiebalfour.zpe.PythonTranspiler;
import jamiebalfour.zpe.core.IAST;
import jamiebalfour.zpe.interfaces.ZPESyntaxTranspiler;

public class Transpiler implements ZPESyntaxTranspiler {

  @Override
  public String transpile(IAST code, String s) {

    return new PythonTranspiler().Transpile(code, s);

  }

  @Override
  public String getLanguageName() {
    return "Python";
  }

  @Override
  public String getFileExtension() {
    return "py";
  }
}