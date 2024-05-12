
import java.io.IOException;

public class Main {
  public static void main(String[] args) {
    System.out.println("YASS to Python transpiler for ZPE");
    test();
  }

  private static void test(){

    try {
      String s = jamiebalfour.zpe.core.ZPEKit.convertCode(jamiebalfour.HelperFunctions.ReadFileAsString("/Users/jamiebalfour/Documents/stdLib.txt"), "output", new Transpiler());
      System.out.println(s);
      jamiebalfour.HelperFunctions.WriteFile("/Users/jamiebalfour/print.py", s, false);
      System.out.println(jamiebalfour.HelperFunctions.ShellExec("python3 /Users/jamiebalfour/print.py"));
    } catch (jamiebalfour.zpe.core.errors.CompileError e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
