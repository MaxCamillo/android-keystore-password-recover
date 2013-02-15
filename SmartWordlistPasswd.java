/**
  *
  * Beschreibung
  *
  * @version 1.0 vom 26.11.2011
  * @author
  */
import java.io.*;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import java.util.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.spec.SecretKeySpec;


public class SmartWordlistPasswd {
  static String alias = "";
  static JKS j;
  static boolean found = false;
  static String currentPass = "a";
  static String passwd = null;
  static int testedPwds = 0;
  static String[] s;
  static ArrayList<String> words = new ArrayList<String>();
  
  //for permutations
  private static ArrayList<String> LetterCombos = new ArrayList<String>();
  
  
  public static void doit(String keystore,String wordlist,boolean permutations ) throws Exception {
    String pass = "a";
    InputStream in = new FileInputStream(keystore);
    int plength = 1;
    
    try {
      j = new JKS();
      j.engineLoad(in, pass.toCharArray() );
      System.out.println("Number of keys in keystore: " + j.engineSize());
      
      Enumeration e = j.engineAliases();
      
      while (e.hasMoreElements()) {
        String a = (String) e.nextElement();
        System.out.println("Found alias: " + a);
        System.out.println("Creation Date: " + j.engineGetCreationDate(a));
        alias = a;
      }
      
      
      in.close();
      
      BufferedReader file = new BufferedReader(new InputStreamReader(new FileInputStream(wordlist)));
      
      in = new FileInputStream(keystore);
      SmartWordlistPasswd.engineLoad(in, pass.toCharArray());
      System.out.println("\r\nStart smart wordlist attack on key!!\r\n");
      if(permutations)
      System.out.println("Use common replacements");
      else
      System.out.println("Capitalize first letter");
      long initTime = System.currentTimeMillis();
      new SmartWordlistBenchmark().start();
      
      String word = "";
      while((word = file.readLine()) != null){
        //skip empty String
        if(word.equals(""))
        continue;
        
        if(!permutations){
          words.add(word);
          //Capitalize first Letter
          char[] stringArray = word.toCharArray();
          //skip if already is uppercase
          if(Character.isUpperCase(stringArray[0]))
          continue;
          stringArray[0] = Character.toUpperCase(stringArray[0]);
          word = new String(stringArray);
          words.add(word);
        }else{
          //use common replacements
          //let's get some common replacement letters
          
          LetterCombos.add("aA@4^");
          LetterCombos.add("bB8");
          LetterCombos.add("cC(");
          LetterCombos.add("dD");
          LetterCombos.add("eE3€");
          LetterCombos.add("fF");
          LetterCombos.add("gG");
          LetterCombos.add("hH");
          LetterCombos.add("iIl1!|");
          LetterCombos.add("jJ");
          LetterCombos.add("kK");
          LetterCombos.add("lL1");
          LetterCombos.add("mM");
          LetterCombos.add("nN");
          LetterCombos.add("oO0");
          LetterCombos.add("pP");
          LetterCombos.add("qQ");
          LetterCombos.add("rR");
          LetterCombos.add("sS5$");
          LetterCombos.add("tT+7");
          LetterCombos.add("uU");
          LetterCombos.add("vV");
          LetterCombos.add("wW");
          LetterCombos.add("xX");
          LetterCombos.add("yY");
          LetterCombos.add("zZ2");
          
          for(String p:getPermutations(word)){
            words.add(p);
          }
        }
        
      }
      
      file.close();
      //add Numbers
      for(int i=1;i<10;i++){
        words.add(String.valueOf(i));
      }
      
      while (!found) {
        
        //make new char[] with specific length
        s = new String[plength];
        
        
        //try all chars with specific length
        recurse(0);
        
        
        //tried all combinations; extend pwd length
        plength++;
      }
      
      if (found) {
        //We are lucky
        System.out.println("Got Password in " +
        ((System.currentTimeMillis() - initTime) / 1000) +
        " seconds");
        System.out.println("Password is: " + passwd + " for alias " + alias);
        if (AndroidKeystoreBrute.saveNewKeystore) {
          j.engineStore(new FileOutputStream(keystore+"_recovered"),new String(passwd).toCharArray());
          
          System.out.println("Saved new keystore to: "+ keystore+"_recovered");
        } // end of if
        AndroidKeystoreBrute.found = true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  //compute all possible combinations by recursion
  private static void recurse(int k) {
    if (!found) {
      if (k == s.length) {
        //System.out.println(s);
        currentPass = sArrayToString(s);
        //System.out.println(currentPass);
        try {
          testedPwds++;
          
          //if this throws an Exception; pwd  is false
          if (keyIsRight(currentPass.toCharArray())) {
            found = true;
            passwd = currentPass;
            
            return;
          }
        } catch (Exception e) {
          //passwd was wrong
          // System.out.println("fail");
        }
      } else {
        int kinc = k + 1;
        
        for (String o : words) {
          s[k] = o;
          recurse(kinc);
        }
      }
    }
  }
  
  private static String sArrayToString(String[] values){
    String str = "";
    for(String temp : values){
      str += temp;
    }
    return str;
  }
  
  
  private static ArrayList<String> getPermutations(String word) {
    ArrayList<String> returnPerms = new ArrayList<String>();
    for(String letter : getLetterPermutations(Character.toString(word.charAt(0))))
    if (word.length()>1)
    for(String permutation : getPermutations(word.substring(1)))
    returnPerms.add(letter + permutation);
    else
    returnPerms.add(letter);
    return returnPerms;
  }
  
  //Word permutaion methods by Jeff Lauder
  
  
  
  private static ArrayList<String> getLetterPermutations(String letter) {
    ArrayList<String> returnLetters = new ArrayList<String>();
    
    
    
    //then we'll apply them
    for(String letterCombo : LetterCombos)
    {
      if(letterCombo.contains(letter))
      {
        for(char returnletter : letterCombo.toCharArray())
        returnLetters.add(Character.toString(returnletter));
        return returnLetters;
      }
    }
    
    //and we'll default to the upper and lower case if not otherwise specified
    returnLetters.add(letter.toLowerCase());
    returnLetters.add(letter.toUpperCase());
    return returnLetters;
  }
  //--------------------------------JKS Methods------------------------------------------
  private static final int MAGIC = 0xFEEDFEED;
  static byte[] encoded;
  static Certificate[] chain;
  static MessageDigest sha;
  static byte[] key;
  static byte[] keystream;
  static byte[] encr;
  static byte[] check;
  private static final int PRIVATE_KEY = 1;
  private static final int TRUSTED_CERT = 2;
  
  public static void engineLoad(InputStream in, char[] passwd)
  throws IOException, NoSuchAlgorithmException,
  CertificateException {
    MessageDigest md = MessageDigest.getInstance("SHA");
    md.update(charsToBytes(passwd));
    md.update("Mighty Aphrodite".getBytes("UTF-8")); // HAR HAR
    
    DataInputStream din = new DataInputStream(new DigestInputStream(in, md));
    
    if (din.readInt() != MAGIC) {
      throw new IOException("not a JavaKeyStore");
    }
    
    din.readInt(); // version no.
    
    final int n = din.readInt();
    
    if (n < 0) {
      throw new IOException("negative entry count");
    }
    
    int type = din.readInt();
    WordlistPasswd.alias = din.readUTF();
    din.readLong(); //Skip Date
    
    switch (type) {
      case PRIVATE_KEY:
      
      int len = din.readInt();
      encoded = new byte[len];
      din.read(encoded);
      
      //privateKeys.put(alias, encoded);
      int count = din.readInt();
      chain = new Certificate[count];
      
      for (int j = 0; j < count; j++)
      chain[j] = readCert(din);
      
      //certChains.put(alias, chain);
      break;
      
      case TRUSTED_CERT:
      
      //trustedCerts.put(alias, readCert(din));
      break;
      
      default:
      throw new IOException("malformed key store");
    }
    
    encr = new EncryptedPrivateKeyInfo(encoded).getEncryptedData();
    keystream = new byte[20];
    System.arraycopy(encr, 0, keystream, 0, 20);
    check = new byte[20];
    System.arraycopy(encr, encr.length - 20, check, 0, 20);
    key = new byte[encr.length - 40];
    sha = MessageDigest.getInstance("SHA1");
    
    byte[] hash = new byte[20];
    din.read(hash);
    
    if (MessageDigest.isEqual(hash, md.digest())) {
      throw new IOException("signature not verified");
    }
  }
  
  public static boolean keyIsRight(char[] password) {
    try {
      return decryptKey(charsToBytes(password));
    } catch (Exception x) {
      return false;
    }
  }
  
  private static byte[] charsToBytes(char[] passwd) {
    byte[] buf = new byte[passwd.length * 2];
    
    for (int i = 0, j = 0; i < passwd.length; i++) {
      buf[j++] = (byte) (passwd[i] >>> 8);
      buf[j++] = (byte) passwd[i];
    }
    
    return buf;
  }
  
  private static boolean decryptKey(byte[] passwd) {
    try {
      System.arraycopy(encr, 0, keystream, 0, 20);
      
      int count = 0;
      
      while (count < key.length) {
        sha.reset();
        sha.update(passwd);
        sha.update(keystream);
        sha.digest(keystream, 0, keystream.length);
        
        for (int i = 0; (i < keystream.length) && (count < key.length); i++) {
          key[count] = (byte) (keystream[i] ^ encr[count + 20]);
          count++;
        }
      }
      
      sha.reset();
      sha.update(passwd);
      sha.update(key);
      
      if (MessageDigest.isEqual(check, sha.digest())) {
        return true;
      }
      
      return false;
    } catch (Exception x) {
      return false;
    }
  }
  
  private static Certificate readCert(DataInputStream in)
  throws IOException, CertificateException,
  NoSuchAlgorithmException {
    String type = in.readUTF();
    int len = in.readInt();
    byte[] encoded = new byte[len];
    in.read(encoded);
    
    CertificateFactory factory = CertificateFactory.getInstance(type);
    
    return factory.generateCertificate(new ByteArrayInputStream(encoded));
  }
}
