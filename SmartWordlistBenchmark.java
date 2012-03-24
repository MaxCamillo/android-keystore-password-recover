/**
  *
  * Beschreibung
  *
  * @version 1.0 vom 26.11.2011
  * @author
  */

public class SmartWordlistBenchmark extends Thread{
  long lastCall = 0;
  int lastCount = 0;

  public void run(){
    while(!SmartWordlistPasswd.found){
      if((System.nanoTime()- lastCall)> 1000000000){
        System.out.println("Current Pass: "+ String.valueOf(SmartWordlistPasswd.currentPass)+" || est. "+(SmartWordlistPasswd.testedPwds - lastCount)+" Pass/Sec");
        System.out.println();

        lastCall = System.nanoTime();
        lastCount = SmartWordlistPasswd.testedPwds;

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
