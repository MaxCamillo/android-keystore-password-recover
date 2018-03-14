package AndroidKeystoreBrute;

/** ravensbane
*
* @version 1.0 on 20.2.2016
* @author
*/
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.LinkedTransferQueue;
import java.util.HashMap;
import java.util.Map;

public class SmartWordlistProducer implements Runnable {
  private final LinkedTransferQueue<String> queueRef;
  private static String[] comboToTest;
  private static Map<String, String> words = new HashMap<String, String>();
  private static String wordsFirst = null;
  private static String wordsLast;
  private static ArrayList<String> LetterCombos = new ArrayList<String>(); // for permutations
  static ArrayList<String> resumePoint = new ArrayList<String>();
  static String[] lastComboProduced;

  public SmartWordlistProducer(LinkedTransferQueue<String> queue, String dict) throws Exception {
    this.queueRef = queue;

    // load words from the dictionary with variations and store it in a map for speed
    words = loadWordList(dict);

    // get the resume point if one exists and set up the string array with the first password to test
    resumePoint = SmartWordlistResume.getResumePoint(words);
    comboToTest = new String[Math.max(resumePoint.size(), AndroidKeystoreBrute.minpieces) + 1];

    // the first slot in the string array is reserved for characters specified by the firstchars arg
    if (AndroidKeystoreBrute.firstchars != null) {
      comboToTest[0] = AndroidKeystoreBrute.firstchars;
    } else {
      comboToTest[0] = "";
    }

    if (resumePoint.isEmpty()) {
      // start at the beginning
      for (int i = 1; i <= AndroidKeystoreBrute.minpieces; ++i) {
        comboToTest[i] = wordsFirst;
      }
    } else {
      if (resumePoint.size() < AndroidKeystoreBrute.minpieces) {
        throw new IOException("numpieces arg specifies a larger min number of pieces than resume data");
      } else {
        System.out.println("Resuming where we left off...");
        for (int i = 1; i <= resumePoint.size(); ++i) {
          comboToTest[i] = resumePoint.get(i - 1);
        }
      }
    }

    // set the initial value of last tested based on the resume data (for benchmark display)
    lastComboProduced = comboToTest;
  }

  @Override
  public void run() {
    while (!SmartWordlistPasswd.found && !SmartWordlistPasswd.allPwdsTested) {
      comboToTest = getNextCombo(comboToTest, comboToTest.length - 1);

      // if the next combo to check exceeds our limit, stop (wait a bit for consumers to finish current checks)
      if (comboToTest.length > AndroidKeystoreBrute.maxpieces + 1) {
        SmartWordlistPasswd.allPwdsTested = true;

        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {

        }

        SmartWordlistPasswd.complete("");
        break;
      }

      // array to string
      StringBuilder builder = new StringBuilder();
      for (String str : comboToTest) {
        builder.append(str);
      }
      String strCombo = builder.toString();

      // add a combo to the queue
      try {
        queueRef.transfer(strCombo);
      } catch (InterruptedException e) {

      }
      lastComboProduced = comboToTest;
    }
  }

  public static String[] getNextCombo(String[] combo, int stelle) {
    // if we were on the last word in the dictionary
    if (combo[stelle].equals(wordsLast)) {
      // set it to the first word, then check the previous word
      combo[stelle] = wordsFirst;
      if (stelle > 1) {
        return getNextCombo(combo, stelle - 1);
      } else {
        // if all words in the combo were the last word, extend the combo length by one word
        String[] longerCombo = new String[combo.length + 1];
        System.arraycopy(combo, 0, longerCombo, 0, combo.length);
        longerCombo[longerCombo.length - 1] = wordsFirst;
        return longerCombo;
      }
    } else {
      // if we aren't on the last word, just move to the next word in the list
      // (in our map, the key is the current word, and the value is the next word)
      // this means that the last entry in our map contains <secondToLastWord, lastWord>
      combo[stelle] = words.get(combo[stelle]);

      return combo;
    }
  }

  private static Map<String, String> loadWordList(String dict) throws Exception {
    Map<String, String> localWords = new HashMap<String, String>();
    BufferedReader file = new BufferedReader(new InputStreamReader(new FileInputStream(dict)));

    String word = "";
    String previousWord = null;
    float maxBytes = (float) Runtime.getRuntime().maxMemory();
    boolean memoryWarning = false;

    while ((word = file.readLine()) != null) {
      // skip empty String
      if (word.equals(""))
        continue;

      if (!memoryWarning && Runtime.getRuntime().totalMemory() / maxBytes > 0.45) {
        System.out.println("Warning: Available memory low. Application may not be able to allocate enough memory "
            + "to process wordlist. If application hangs try not using the -p argument or use a "
            + "smaller wordlist.\r\n");
        memoryWarning = true;
      }

      // don't add duplicate words; they will screw up our map
      if (localWords.containsKey(word)) {
        continue;
      }

      // store the first key in the map
      if (wordsFirst == null) {
        wordsFirst = word;
      }

      if (previousWord == null) {
        previousWord = word;
      }

      if (!AndroidKeystoreBrute.permutations) {
        localWords.put(previousWord, word);
        previousWord = word;

        if (AndroidKeystoreBrute.onlyLowerCase == false) {
          // Capitalize first Letter
          char[] stringArray = word.toCharArray();

          // skip if already uppercase
          if (Character.isUpperCase(stringArray[0]))
            continue;
          stringArray[0] = Character.toUpperCase(stringArray[0]);
          word = new String(stringArray);
          localWords.put(previousWord, word);
          previousWord = word;
        }

      } else {
        // permutations don't work on single character words
        if (word.length() < 2)
          continue;

        // use common replacements
        // let's get some common replacement letters
        LetterCombos.add("aA@4^Ã¡Ã?");
        LetterCombos.add("bB8");
        LetterCombos.add("cC(");
        LetterCombos.add("dD");
        LetterCombos.add("eE3?Ã©Ã‰");
        LetterCombos.add("fF");
        LetterCombos.add("gG");
        LetterCombos.add("hH");
        LetterCombos.add("iIl1!|Ã­Ã?");
        LetterCombos.add("jJ");
        LetterCombos.add("kK");
        LetterCombos.add("lL1");
        LetterCombos.add("mM");
        LetterCombos.add("nNÃ±Ã‘");
        LetterCombos.add("oO0Ã³Ã“");
        LetterCombos.add("pP");
        LetterCombos.add("qQ");
        LetterCombos.add("rR");
        LetterCombos.add("sS5$");
        LetterCombos.add("tT+7");
        LetterCombos.add("uUÃºÃš");
        LetterCombos.add("vV");
        LetterCombos.add("wW");
        LetterCombos.add("xX");
        LetterCombos.add("yY");
        LetterCombos.add("zZ2");

        for (String p : getPermutations(word)) {
          localWords.put(previousWord, p);
          previousWord = p;
        }
      }

    }

    file.close();

    // add Numbers
    for (int i = 1; i < 10; i++) {
      String number = String.valueOf(i);
      localWords.put(previousWord, number);
      previousWord = number;
    }

    // store the last key in the map
    wordsLast = "9";

    // add special chars
    if (AndroidKeystoreBrute.disableSpecialChars == false) {
      char[] specialChars = {
          '!', '"', '@', '#', '$', '%', '&', '/', '{', '>',
          '}', '(', ')', '[', ']', '=', '?', '+', '`', '|',
          '^', '~', '*', '-', '_', '.', ':', ',', ';', '<',
          '\'', '\\',
      };

      for (int i = 0; i < specialChars.length; i++) {
        String character = String.valueOf(specialChars[i]);
        localWords.put(previousWord, character);
        previousWord = character;
      }

      wordsLast = "\\";
    }

    return localWords;
  }

  // Word permutation methods by Jeff Lauder
  private static ArrayList<String> getPermutations(String word) {
    ArrayList<String> returnPerms = new ArrayList<String>();
    for (String letter : getLetterPermutations(Character.toString(word.charAt(0))))
      if (word.length() > 1)
        for (String permutation : getPermutations(word.substring(1)))
          returnPerms.add(letter + permutation);
      else
        returnPerms.add(letter);
    return returnPerms;
  }

  private static ArrayList<String> getLetterPermutations(String letter) {
    ArrayList<String> returnLetters = new ArrayList<String>();

    // then we'll apply them
    for (String letterCombo : LetterCombos) {
      if (letterCombo.contains(letter)) {
        for (char returnletter : letterCombo.toCharArray())
          returnLetters.add(Character.toString(returnletter));
        return returnLetters;
      }
    }

    // and we'll default to the upper and lower case if not otherwise specified
    returnLetters.add(letter.toLowerCase());
    returnLetters.add(letter.toUpperCase());
    return returnLetters;
  }
}
