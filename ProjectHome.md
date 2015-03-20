# Android Keystore Password Recovery #


A few weeks ago I just forgot the password for my android keystore, so I couldnt update my app for the market. So I decided to code a little bruteforcing tool to recovery my password.


**Java 7 is required to run this tool!!**

download [here](http://www.oracle.com/technetwork/java/javase/downloads/java-se-jre-7-download-432155.html)

**Download now on gdrive, because gcode has deactivated downloads :(**
https://drive.google.com/file/d/0B_Rw7kNjv3BATldrLXMwcVRud2c/edit?usp=sharing



# Details #
The tool recovers the key for your alias. By default this is the same like the keystore password. ~~I will try to add an option to recover both passwords if they are not equal.~~ Now there is an option to save the key in a new keystore with the same password than the key! You can use this, to sign your apk and update your app in the Playstore.

There are 3 Methods to recover your keystore password:

  1. - Simply Bruteforce
  1. - Dictionary Attack
  1. - Smart Wordlist Attack

In my opinion the last option is the best. You specify some password segments in a textfile. All the segments will be permute and mixed together with numbers.

for example:

your password is: got2loveYa123
in your dictionary should be:
```
got
love
ya
```
Numbers are added automatically. Each word will be added twice, once like you wrote and once with the first letter capitalized, so you just have to write your words once if your are not shure if you first letter was uppercase or lowercase

Good luck

Thanks to Casey Marshall <rsdio@metastatic.org> for JKS API http://metastatic.org/source/JKS.html


---


# TODO #

  * Possibility to specify chars used for bruteforce (regex)
  * Maybe multithreading to be faster on multicore systems
  * ~~Recover both passwords (keystore and key) if they are not equal~~



---

# Changelog #

**version 1.05**
  * Now you can set the start String for Brute Force, so you can continue if you had to terminate the tool

**version 1.04**
  * Now with MultiThreading for BruteForcing. Should give you a good chance for Passwords that are 6 or 7 chars long

**version 1.03**
  * Now there is an option to save the key in a new keystore with the same password than the key!

  * New option to specify the minimum length of the password (for brute force)

**version 1.02**

  * added new option for smart wordlist attack. With the parameter '-p' you activate the common replacements permutation mode. Thanks to Jeff Lauder, who wrote the code for this. More Information are in the wiki

**version 1.01**
  * small fixes

---



## P.S. ##

If I helped you, you may want donate a few cents, so i can buy me a beer :D

### Thanks for Donation: ###

  * SebyFactory
  * Martin Harvey
  * Joshua Slauson
  * Roman Marak
  * Orestis Anavaloglou
  * Jan Teluch
  * Martin Sander
  * Fabrice Marchal
  * Louis Moga
  * Ole Jørgen Brønner
  * Yasith Vidanaarachchi
  * Marcus Honnacker
  * Kidinov Andrey
  * michael sweeney
  * Ali Karabalci
  * Karthik Venkatesh
  * Musaqil Musabeyli
  * Benjamin Allison

[Donate](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=46BBKZZLLZXV4)