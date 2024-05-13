
import jamiebalfour.zpe.core.ZPE;

import java.io.IOException;

public class Main {
  public static void main(String[] args) {
    System.out.println("YASS to Python transpiler for ZPE");
    if(args.length == 2){
      test(args[0], args[1]);
    }

  }

  private static void test(String input, String output){

    try {
      String s = jamiebalfour.zpe.core.ZPEKit.convertCode(jamiebalfour.HelperFunctions.ReadFileAsString(input), "output", new Transpiler());
      System.out.println(s);
      jamiebalfour.HelperFunctions.WriteFile(output, s, false);
      System.out.println(jamiebalfour.HelperFunctions.ShellExec("python3 " + output));
    } catch (jamiebalfour.zpe.core.errors.CompileError e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
