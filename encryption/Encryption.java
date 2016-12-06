// package com.ucla.Encryption;

import java.math.BigInteger;
import java.util.*;


import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;

class Paillier
{
    public static BigInteger powpow(BigInteger base, BigInteger exponent, BigInteger mods)
    {
        BigInteger result = base.modPow(exponent, mods);

        return result;
    }

    public static BigInteger invMod(BigInteger a, BigInteger p) throws Exception
    {
        BigInteger invResult = a.modInverse(p);
        return invResult;
    }

    public static BigInteger modPow(BigInteger base, BigInteger exp, BigInteger mod)
    {
        return base.modPow(exp, mod);
    }

    public static KeyPair generateKeyPair(long bits) {
        if (bits < 8) {
            bits = 8;
        }
        try {
            BigInteger p = Primes.generatePrime(bits / 2);
            BigInteger q = Primes.generatePrime(bits / 2);
            BigInteger n = p.multiply(q);

            PrivateKey priv = new PrivateKey(p, q, n);
            PublicKey pub = new PublicKey(n);
            KeyPair kp = new KeyPair(priv, pub);
            return kp;
        }
        catch (Throwable e) {
            return generateKeyPair(bits);
        }
    }

    public static BigInteger encrypt(PublicKey pub, long plain)
    {
        BigInteger r;
        double pubNlog = Math.log(pub.n.doubleValue());
        double log2 = Math.log(2);
        double rounded = Math.round(pubNlog / log2);
        while(true) {
            r = Primes.generatePrime((long)rounded);
            if (r.compareTo(BigInteger.ZERO) == 1 && r.compareTo(pub.n) == -1) {
                break;
            }
        }

        BigInteger x = powpow(r, pub.n, pub.nSq);

        BigInteger bigIntPlain = BigInteger.valueOf(plain);

        BigInteger cipher = (powpow(pub.g, bigIntPlain, pub.nSq).multiply(x)).mod(pub.nSq);

        return cipher;
    }

    public static BigInteger eAdd(PublicKey pub,BigInteger a, BigInteger b)
    {
        return a.multiply(b).mod(pub.nSq);
    }

    public static BigInteger eAddConst(PublicKey pub, BigInteger a,long n)
    {
        BigInteger bigIntN = BigInteger.valueOf(n);
        return a.multiply(modPow(pub.g, bigIntN, pub.nSq)).mod(pub.nSq);
    }

    public static BigInteger eMulConst(PublicKey pub, BigInteger a,long n)
    {
        return modPow(a, BigInteger.valueOf(n), pub.nSq);
    }

    public static long decrypt(PrivateKey priv, PublicKey pub, BigInteger cipher)
    {
        BigInteger x = powpow(cipher, priv.l, pub.nSq).subtract(BigInteger.ONE);
        BigInteger plain = x.divide(pub.n).multiply(priv.m).mod(pub.n);

        return plain.longValue();
    }

    static public class PrivateKey {
        PrivateKey(BigInteger p, BigInteger q, BigInteger n) throws Exception {

            l = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
            m = Paillier.invMod(l, n);
        }

        PrivateKey(BigInteger ll, BigInteger mm) {
            l = ll;
            m = mm;
        }

        public BigInteger l;
        public BigInteger m;
    }

    static public class PublicKey {
        PublicKey(BigInteger nx) {
            n = nx;
            nSq = nx.multiply(nx);
            g = nx.add(BigInteger.ONE);
        }

        public BigInteger n;
        public BigInteger nSq;
        public BigInteger g;
    }

    static public class KeyPair {
        KeyPair(PrivateKey privateKey, PublicKey publicKey)
        {
            priv = privateKey;
            pub = publicKey;
        }
        public PrivateKey priv;
        public PublicKey pub;
    }

}

class Primes {

    static public BigInteger generatePrime(long nBit)
    {
        if (nBit < 40) {
            nBit = 40;
        }
        return BigInteger.probablePrime((int)nBit, r);
    }

    static Random r = new Random();
}

class StringEncryption
{
    StringEncryption(String filePath){
        m_file = filePath;
    }

    public String doEncryption()
    {
        String content = null;
        try {
            content = new Scanner(new File(m_file)).useDelimiter("\\Z").next();
        }
        catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }

        // read from a file to StringEncryption

        // split
        String[] words = content.split("\\s+");

        encryptStrings(words);

        String joined = String.join(" ", words);
        return joined;
    }

    void encryptStrings(String[] words) {
        for (int i = 0; i < words.length; i++) {
            words[i] = encrypt(words[i]);
        }
    }

    static public String encrypt(String raw)
    {
        StringBuilder sb = new StringBuilder();
        int n = raw.length();
        for (int i = 0; i < n; i++) {
            char c = raw.charAt(i);
            sb.append((char)((int)c + 1));
        }
        return sb.toString();
    }

    static public String decrypt(String cipher)
    {
        StringBuilder sb = new StringBuilder();
        int n = cipher.length();
        for (int i = 0; i < n; i++) {
            char c = cipher.charAt(i);
            sb.append((char)((int)c - 1));
        }
        return sb.toString();
    }

    String m_file;
}

class Encryption
{
    public static void main(String[] args) {
        if (args.length > 0) {
            String act = args[0];
            if (act.equals("encrypt")) {
                String inputFile = args[1];
                doEncryption(inputFile);
            } else if(act.equals("decrypt")) {
                String file = null;
                if (args.length < 2) {
                    System.err.println("use default file: output.txt");
                    file = "output.txt";
                } else {
                    file = args[1];
                }
                // System.err.println("xx");
                doDecryption(readKeyPair(), file);
                // System.err.println("yy");
            } else {
                System.err.println("wrong parameter");
            }
        } else {
            System.err.println("need parameters: [encrypt, decrypt]");
        }
    }

    static void doDecryption(Paillier.KeyPair kp, String inputFile) {

        // System.err.println("do Decryption");

        // System.err.println(Paillier.encrypt(kp.pub, 1));

        String content = null;
        try {
            content = new Scanner(new File(inputFile)).useDelimiter("\\Z").next();
        }
        catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        String[] lines = content.split(System.getProperty("line.separator"));
        for (int i = 0; i < lines.length; i++) {
            String row = lines[i];
            // System.out.println(row);
            String[] words = row.split("\\s+");
            String decryptedWord = StringEncryption.decrypt(words[0]);
            // System.out.println(words.length);
            // System.out.println("toInwork is :" + words[1]);
            long decryptedNum = Paillier.decrypt(kp.priv, kp.pub, new BigInteger(words[1]));
            sb.append(decryptedWord + " " + decryptedNum);
            sb.append("\n");
        }

        // System.out.println(sb.toString());

        try {
            FileWriter fw = new FileWriter("decrypted-" + inputFile);
            fw.write(sb.toString());
            fw.close();
        }
        catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }

    }

    static void doEncryption(String inputFile) {
        // read text file
        StringEncryption se = new StringEncryption(inputFile);

        String encrypted = se.doEncryption();

        // write encrypted version to a new file
        writeFile(encrypted, "encrypted-" + inputFile);

        // generate keyPair
        Paillier.KeyPair kp = Paillier.generateKeyPair(8);

        // write pubKey to pub_key
        writePubKey(kp.pub, "pub.key");

        // write privKey to priv_key
        writePrivKey(kp.priv, "priv.key");
    }

    static void writeFile(String content, String file) {
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(content);
            fw.write("\n");
            fw.close();
        }
        catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void writePubKey(Paillier.PublicKey pub, String file) {
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(pub.n.toString());
            fw.write("\n");
            fw.close();
        }
        catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void writePrivKey(Paillier.PrivateKey priv, String file) {
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(priv.l.toString());
            fw.write("\n");
            fw.write(priv.m.toString());
            fw.write("\n");
            fw.close();
        }
        catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }
    }

    static Paillier.KeyPair readKeyPair() {
        Paillier.PublicKey pub = readPubKey();
        FileReader fr = null;
        try {
            fr = new FileReader("priv.key");
        }
        catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }
        Scanner s = new Scanner(fr);
        BigInteger l = new BigInteger(s.nextLine());
        BigInteger m = new BigInteger(s.nextLine());
        Paillier.PrivateKey priv = new Paillier.PrivateKey(l, m);
        Paillier.KeyPair pk = new Paillier.KeyPair(priv, pub);
        return pk;
    }

    static Paillier.PublicKey readPubKey() {
        FileReader fr = null;
        try {
            fr = new FileReader("pub.key");
        }
        catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }
        Scanner s = new Scanner(fr);
        String line = s.nextLine();
        Paillier.PublicKey pub = new Paillier.PublicKey(new BigInteger(line));
        return pub;
    }
}