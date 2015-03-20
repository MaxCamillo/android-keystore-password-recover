# Android Keystore Password Recovery #


A few weeks ago I just forgot the password for my android keystore, so I cant update my app for the market. So I decided to code a little bruteforcing tool to recovery my password.

# Details #

There are 3 Methods to recover your keystore password:

  1. - Simply Bruteforce
  1. - Dictionary Attack
  1. - Smart Wordlist Attack

In my opinion the last option is the best. You specify some password segments in a textfile. All the segments will be permute and mixed together with numbers. e.g:

your password is: got2loveYa11
in your dictionary should be:
```
got
love
ya
```
Numbers are added automatically. Each word will be added twice, once like you wrote and once with the first letter capitalized, so you just have to write your words once if your are not shure if you first letter was uppercase or lowercase

Good luck