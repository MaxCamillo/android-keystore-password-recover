public class AndroidKeystoreBrute {
    static final int BRUTE = 1;
    static final int WORD = 2;
    static final int SWORD = 3;
    static final String VERSION = "1.07";
    public static boolean saveNewKeystore = false;
    public static boolean onlyLowerCase = false;
    public static boolean disableSpecialChars = false;
    public static boolean permutations = false;
    public static int minlength = 0;
    public static int minpieces = 1;
    public static int maxpieces = 64;
    public static String firstchars = null;

    public static void main(String[] args) throws Exception {
        String start = "A";
        int method = 0;

        String keystore = "";
        String dict = "";

        if (args.length == 0) {
            printhelp();
            return;
        }
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                    printhelp();
                    return;
                case "-m":
                    i++;
                    method = Integer.parseInt(args[i]);
                    break;
                case "-k":
                    i++;
                    keystore = args[i];
                    break;
                case "-d":
                    i++;
                    dict = args[i];
                    break;
                case "-p":
                    permutations = true;
                    break;
                case "-w":
                    saveNewKeystore = true;
                    break;
                case "-start":
                    i++;
                    start = args[i];
                    break;
                case "-l":
                    i++;
                    minlength = Integer.parseInt(args[i]);
                    break;
                case "-onlylower":
                    onlyLowerCase = true;
                    break;
                case "-nospecials":
                    disableSpecialChars = true;
                    break;
                case "-firstchars":
                    i++;
                    firstchars = args[i];
                    break;
                case "-pieces":
                    i++;
                    minpieces = Integer.parseInt(args[i]);
                    i++;
                    maxpieces = Integer.parseInt(args[i]);
                    break;
                default:
                    System.out.println("Unknown argument: " + args[i]);
                    return;
            }
        }

        // Prevent restart from a when using only lowercase with a defined start string
        if (start == "A" && onlyLowerCase == true)
            start = "a";

        if ((method == 0) || (method > 3)) {
            printhelp();
            return;
        }

        if (method == 1) {
            BrutePasswd.doit(keystore, start);
        }

        if (method == 2) {
            WordlistPasswd.doit(keystore, dict);
        }

        if (method == 3) {
            SmartWordlistPasswd.doit(keystore, dict);
        }
    }

    public static void quit() {
        System.out.println("\r\nFor updates visit http://code.google.com/p/android-keystore-password-recover/");
        System.exit(0);
    }

    static void printhelp() {
        System.out.println("AndroidKeystorePasswordRecoveryTool by M@xiking");
        System.out.println("Version " + VERSION + "\r\n");
        System.out.println("There are 3 Methods to recover the key for your Keystore:\r\n");
        System.out.println("1: simply bruteforce - good luck");
        System.out.println("2: dictionary attack - your password has to be in the dictionary");
        System.out.println("3: smart dictionary attack - you specify a dictionary with regular pieces you use in your passwords. Numbers are automaticly added and first letter will tested uppercase and lowercase. This method can resume when interrupted as long as you specify the same arguments.\r\n");
        System.out.println("args:");
        System.out.println("-m <1..3> Method");
        System.out.println("-k <path> path to your keystore");
        System.out.println("-d <path> dictionary (for method 2 and 3)");
        System.out.println("-l <min> sets min password length in characters (for method 3)");
        System.out.println("-start <String> sets start String of the word (for method 1)");
        System.out.println("-firstchars <String> specify first characters of the password (for method 3)");
        System.out.println("-pieces <min> <max> specify the min and max number of pieces to use when building passwords (for method 3)\r\n");

        System.out.println("-nospecials to not try special characters in password (makes cracking faster for simple passwords)");
        System.out.println("-onlylower for only lowercase letters");
        System.out.println("-w saves the certificate in a new Keystore with same password as key");
        System.out.println("-p use common replacements like '@' for 'a'(for method 3) WARNING: This is very slow. Do not use on dictionaries with more than 250 entries.\r\n");
        System.out.println("-h prints this helpscreen\r\n");

        long maxBytes = Runtime.getRuntime().maxMemory();
        System.out.println("Max memory: " + maxBytes / 1024L / 1024L + "M\r\n");

        System.out.println("v1.06 updated by rafaelwbr; v1.07 updated by ravensbane");
        System.out.println("For updates visit http://code.google.com/p/android-keystore-password-recover/");
    }
}
