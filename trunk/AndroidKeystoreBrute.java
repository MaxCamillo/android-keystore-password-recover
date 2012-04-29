/**
  *
  *
  *
  * @version 1.01 vom 24.03.2012
  * @author
  */

public class AndroidKeystoreBrute {

  static final int BRUTE = 1;
  static final int WORD = 2;
  static final int SWORD = 3;
  static final String VERSION = "1.02";
  public static boolean found = false;
  public static void main(String[] args) throws Exception{

    int method = 0;
    String keystore = "";
    String dict = "";
    boolean permutations = false;
    if(args.length==0){
      printhelp();
      return;
    }
    for(int i = 0; i<args.length;i++){
      switch(args[i]){
        case "-h":{
          printhelp();
          return;

        }
        case "-m":{
          i++;
          method = Integer.parseInt(args[i]);
          break;
        }
        
        case "-k":{
          i++;
          keystore = args[i];
          break;
        }
        case "-d":{
          i++;
          dict = args[i];
          break;
        }
        
         case "-p":{
          permutations = true;
          break;
        }
      }
      
    }
    
    if(method == 0 || method > 3){
      printhelp();
      return;
    }
    
    if(method == BRUTE){
      BrutePasswd.doit(keystore);
    }
    
    if(method == WORD){
      WordlistPasswd.doit(keystore,dict);
    }
    
    if(method == SWORD){
      SmartWordlistPasswd.doit(keystore,dict,permutations);
    }
    
    System.out.println("\r\nFor updates visit http://code.google.com/p/android-keystore-password-recover/");

  }
  
  static void printhelp(){
      System.out.println("AndroidKeystorePasswordRecoveryTool by M@xiking\r\n");
      System.out.println("Version "+VERSION);
      System.out.println("There are 3 Methods to recover the key for your Keystore:\r\n");
      System.out.println("1: simply bruteforce - good luck");
      System.out.println("2: dictionary attack - your password has to be in the dictionary");
      System.out.println("3: smart dictionary attack - you specify a dictionary with regular pieces you use in your passwords. Numbers are automaticly added and first letter will tested uppercase and lowercase\r\n");
      System.out.println("args:");
      System.out.println("-m <1..3> Method");
      System.out.println("-k <path>  path to your keystore");
      System.out.println("-d <path> dictionary (for method 2 and 3)");
      System.out.println("-p use common replacements like '@' for 'a'(for method 3)\r\n");
      System.out.println("-h prints this helpscreen\r\n");
      
      long maxBytes = Runtime.getRuntime().maxMemory();
      System.out.println("Max memory: " + maxBytes / 1024 / 1024 + "M");
      
      System.out.println("\r\nFor updates visit http://code.google.com/p/android-keystore-password-recover/");
  }
}
