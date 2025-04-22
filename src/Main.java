
import jamiebalfour.zpe.core.ZPE;
import jamiebalfour.zpe.interfaces.ZPEException;

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
      String s = jamiebalfour.zpe.core.ZPEKit.convertCode(jamiebalfour.HelperFunctions.readFileAsString(input), "output", new Transpiler());
      System.out.println(s);
      jamiebalfour.HelperFunctions.writeFile(output, s, false);
      System.out.println(jamiebalfour.HelperFunctions.shellExec("python3 " + output));
    } catch (ZPEException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
