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


import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.spec.SecretKeySpec;


public class BrutePasswd extends Thread {
  static String alias = "";
  static String keystoreFileName;
  static JKS j;
  static volatile boolean found = false;
  
  static int numberOfThreads = 8;
  
  static String passwd = null;  
  static int testedPwds = 0;
  
  static char[] currPass;
  
  static char[] alphabet = {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
    'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
    'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
    'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
    's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2',
    '3', '4', '5', '6', '7', '8', '9',
  };
  
  public BrutePasswd()throws Exception{
    
    FileInputStream in = new FileInputStream(keystoreFileName);
    engineLoad(in, currPass);    
  }
  public void run(){
    char[] str = new char[1];
    while (!found) { 
      str = nextWord(str);
      if(keyIsRight(str)){
        passwd = String.copyValueOf(str);
        found = true;
        //We are lucky
        System.out.println("Got Password!");
        System.out.println("Password is: " + passwd + " for alias " + alias);
        
        try{
          if (AndroidKeystoreBrute.saveNewKeystore) {
            j.engineStore(new FileOutputStream(keystoreFileName+"_recovered"),new String(passwd).toCharArray());
            System.out.println("Saved new keystore to: "+ keystoreFileName+"_recovered");
          } // end of if
          AndroidKeystoreBrute.found = true;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } // end of while
  }
  public static void doit(String keystore) throws Exception {
    
    doit(keystore,"A");
  }
  
  public static void doit(String keystore,String start) throws Exception {
    
    char[] pass = new char[start.length()];  
    pass = start.toCharArray();
    
    InputStream in = new FileInputStream(keystore);
    currPass = pass;
    
    numberOfThreads = Runtime.getRuntime().availableProcessors() * 2;
    
    try {
      j = new JKS();
      j.engineLoad(in, pass);
      System.out.println("Number of keys in keystore: " + j.engineSize());
      
      Enumeration e = j.engineAliases();
      
      while (e.hasMoreElements()) {
        String a = (String) e.nextElement();
        System.out.println("Found alias: " + a);
        System.out.println("Creation Date: " + j.engineGetCreationDate(a));
        alias = a;
      }
      
      in.close();
      
      keystoreFileName = keystore;
      
      for (int i = 0;i< numberOfThreads ;i++ ) {
        Thread t = new BrutePasswd();
        t.start();
      } // end of for
      
      new BruteBenchmark().start();
      
      System.out.println("Fire up " +numberOfThreads+ " Threads");
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  public synchronized static char[] nextWord(char[] str) {
    testedPwds++;
    currPass =  nextWord(currPass, currPass.length-1);
    if (str.length != currPass.length) {
      str = new char[currPass.length];
    } // end of if
    
    System.arraycopy(currPass, 0, str, 0, currPass.length);
    return  str;
  }
  
  public static char[] nextWord(char[] word, int stelle) {
    
    if(word[stelle] == alphabet[alphabet.length - 1]) {
      word[stelle] = alphabet[0];
      if (stelle > 0) {
        return nextWord(word, stelle - 1);
      } else{
        char[] longerWord = new char[word.length +1];
        longerWord[0] = alphabet[0];
        System.arraycopy(word,0,longerWord,1,word.length);
        return longerWord;
      }
    }
    else{
      for (int i = 0; i< alphabet.length; i++){
        if (word[stelle] == alphabet[i]){
          word[stelle] = alphabet[i+1];
          break;
        }
      }
      return word;
    }
  }
  
  //--------------------------------JKS Methods------------------------------------------
  private static final int MAGIC = 0xFEEDFEED;
  
  private byte[] encoded;
  private Certificate[] chain;
  private MessageDigest sha;
  private byte[] key;
  private byte[] keystream;
  private byte[] encr;
  private byte[] check;
  
  private static final int PRIVATE_KEY = 1;
  private static final int TRUSTED_CERT = 2;
  
  public void engineLoad(InputStream in, char[] passwd)
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
  
  public boolean keyIsRight(char[] password) {
    try {
      return decryptKey(charsToBytes(password));
    } catch (Exception x) {
      return false;
    }
  }
  
  private byte[] charsToBytes(char[] passwd) {
    byte[] buf = new byte[passwd.length * 2];
    
    for (int i = 0, j = 0; i < passwd.length; i++) {
      buf[j++] = (byte) (passwd[i] >>> 8);
      buf[j++] = (byte) passwd[i];
    }
    
    return buf;
  }
  
  private boolean decryptKey(byte[] passwd) {
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
  
  private Certificate readCert(DataInputStream in)
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
