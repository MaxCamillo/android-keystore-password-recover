# How To #


**Java 7 is required to run this tool!!**

download [here](http://www.oracle.com/technetwork/java/javase/downloads/java-se-jre-7-download-432155.html)


### Download the latest version of the Tool ###
https://drive.google.com/file/d/0B_Rw7kNjv3BATldrLXMwcVRud2c/edit?usp=sharing


### execute with ###

```
java -jar AndroidKeystoreBrute.jar <args>
```

### possible args ###
```
-m <1..3> Method
-k <path>  path to your keystore
-d <path> dictionary (for method 2 and 3)
-p use common replacements like '@' for 'a'(for method 3) WARNING - very slow!! 
-start <String> sets the start String for the password (for brute force)
-w writes a new keystore with same password than the key
-h prints helpscreen
```

### example for brute-force attack ###
```
java -jar AndroidKeystoreBrute_v1.05.jar -m 1 -k <...keystore> -start AAAAAA
```

### example for dictionary attack ###
```
java -jar AndroidKeystoreBrute_v1.05.jar -m 2 -k "C:\\mykeystore.keystore" -d "wordlist.txt" 
```

### example for smart wordlist attack (recommend) ###
```
java -jar AndroidKeystoreBrute_v1.05.jar -m 3 -k "C:\\mykeystore.keystore" -d "wordlist.txt" 
```

**If there are any spaces in path or filenames, you have to use quotes for the path!!**


If you want to work on your pc while this is running, just decrease the priority of java.exe (dont't know how to do it in Linux etc..)

If you have to interrupt the brute force you can restart it with adding -start "LASTPWD"