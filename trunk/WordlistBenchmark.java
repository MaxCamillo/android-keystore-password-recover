/**
  *
  * Beschreibung
  *
  * @version 1.0 vom 26.11.2011
  * @author
  */

public class WordlistBenchmark extends Thread{
  long lastCall = 0;
  int lastCount = 0;

  public WordlistBenchmark() {

  }

  public void run(){
    while(!WordlistPasswd.found && WordlistPasswd.currentPass != null){
      if((System.nanoTime()- lastCall)> 1000000000){
        System.out.println("Current Pass: "+ String.valueOf(WordlistPasswd.currentPass)+" || est. "+(WordlistPasswd.testedPwds - lastCount)+" Pass/Sec");
        System.out.println();

        lastCall = System.nanoTime();
        lastCount = WordlistPasswd.testedPwds;

        try{
          Thread.sleep(1000);
        }catch(Exception e){

        }
      }else{
        //System.out.println("Too much");
      }
    }
  }
}
