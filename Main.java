// import java.util.Random;

import java.math.BigInteger;
import java.util.*;

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

class Main
{
    private static void testDecryption()
    {
        int cnt = 0;
        for (int i = 0; i < 100; i++) {
            Paillier.KeyPair kp = Paillier.generateKeyPair(8);
            long a = 10000000;
            // System.err.println("???");
            BigInteger aE = Paillier.encrypt(kp.pub, a);
            long fuckedA = Paillier.decrypt(kp.priv, kp.pub, aE);
            if (fuckedA != a) {
                cnt++;
            }
        }
        System.err.println("Decryption test: " + cnt + " errors out of " + 100);
    }

    private static void testAdd()
    {
        Paillier.KeyPair kp = Paillier.generateKeyPair(8);
        long a = 123;
        long b = 456;
        BigInteger aE = Paillier.encrypt(kp.pub, a);
        BigInteger bE = Paillier.encrypt(kp.pub, b);
        BigInteger sumE = Paillier.eAdd(kp.pub, aE, bE);
        long sum = Paillier.decrypt(kp.priv, kp.pub, sumE);
        if (sum == a + b) {
            System.err.println("eAdd is OK");
        } else {
            System.err.println("eAdd is shit");
        }

        BigInteger aPlusConst5E = Paillier.eAddConst(kp.pub, aE, 5);
        long aPlusConst5 = Paillier.decrypt(kp.priv, kp.pub, aPlusConst5E);
        if (aPlusConst5 == a + 5) {
            System.err.println("eAddConst is OK");
        } else {
            System.err.println("eAddConst is shit");
        }
    }

    private static void testMulConst()
    {
        Paillier.KeyPair kp = Paillier.generateKeyPair(8);
        long a = 123;
        BigInteger aE = Paillier.encrypt(kp.pub, a);
        BigInteger a5E = Paillier.eMulConst(kp.pub, aE, 5);
        long rtn = Paillier.decrypt(kp.priv, kp.pub, a5E);
        if (rtn == a * 5) {
            System.err.println("eMulConst is OK");
        } else {
            System.err.println("eMulConst is shit");
        }
    }

    public static void main(String[] args) {
        testDecryption();
        testAdd();
        testMulConst();
    }
}