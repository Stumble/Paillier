// import java.util.Random;

import java.math.BigInteger;
import java.util.*;

class Paillier
{
    public static long powpow(long base, long times, long mods)
    {
//        long ans = 1;
//        for (long i = 0; i < times; i++) {
//            ans *= base;
//            ans %= mods;
//        }
        BigInteger num = BigInteger.valueOf(base);
        BigInteger exponent = BigInteger.valueOf(times);
        BigInteger modulus = BigInteger.valueOf(mods);
        BigInteger result = num.modPow(exponent, modulus);

        // I guess we might need BigInteger everywhere
        long ans = result.longValue();

        return ans;
    }

    public static long invMod(long a, long p) throws Exception
    {
//        long maxiter = 10000000;
//        if (a == 0) {
//            throw new Exception("ERROR: 0 has no inverse mod");
//            // System.out.println();
//        }
//        long r = a;
//        long d = 1;
//        boolean flag = false;
//        for (long i = 0; i < Math.min(p, maxiter); i++) {
//            d = (long) (((Math.floor(p / r) + 1) * d) % p);
//            r = (d * a) % p;
//            if (r == 1) {
//                flag = true;
//                break;
//            }
//        }
//        if (!flag) {
//            throw new Exception("ERROR: a has no inverse mod p, a is:" + a);
//        }
//        return d;
        BigInteger num = BigInteger.valueOf(a);
        BigInteger modulus = BigInteger.valueOf(p);
        BigInteger invResult = num.modInverse(modulus);
        return invResult.longValue();
    }

    // public static long invMod(long a, long p)
    // {
    //     long rtn = 0;
    //     try {
    //          rtn = invMod(a, p, 10000000);
    //     }
    //     catch (Throwable e) {
    //         System.out.println("Error " + e.getMessage());
    //         e.printStackTrace();
    //     }
    //     return rtn;
    // }

    public static long modPow(long base, long exp, long mod)
    {
        long result = 1;
        while (exp > 0) {
            if ((exp & 1) == 1) {
                result = (result * base) % mod;
            }
            exp = exp >> 1;
            base = (base * base) % mod;
        }
        return result;
    }

    public static KeyPair generateKeyPair(long bits) {
        try {
            long p = Primes.generatePrime(bits / 2);
            long q = Primes.generatePrime(bits / 2);
            long n = p * q;

            System.out.println("P generated is: " + p);
            System.out.println("Q generated is: " + q);
            System.out.println("N generated is: " + n);
            PrivateKey priv = new PrivateKey(p, q, n);
            PublicKey pub = new PublicKey(n);
            KeyPair kp = new KeyPair(priv, pub);
            // System.err.println("this is p" + p);
            // System.err.println("this is q" + p);
            return kp;
        }
        catch (Throwable e) {
            return generateKeyPair(bits);
            // System.out.println("Error " + e.getMessage());
            // e.printStackTrace();
        }

        // return KeyPair()
    }

    public static long encrypt(PublicKey pub, long plain)
    {
        // System.err.println((pub.n));
        // System.err.println(Math.log(pub.n));
        // System.err.println(Math.log(pub.n) / Math.log(2));
        // System.err.println(Math.round((long)(Math.log(pub.n) / Math.log(2))));

        long r;
        while(true) {
            r = Primes.generatePrime((long) Math.round((Math.log(pub.n) / Math.log(2))));
            if (r > 0 && r < pub.n) {
                break;
            }
        }
        System.out.println("R is: " + r);
        // System.err.println("r is :" + r);
        // System.err.println("pub.n is :" + pub.n);
        // System.err.println("pub.nSq is :" + pub.nSq);

        long x = powpow(r, pub.n, pub.nSq);
        System.out.println("X = R ^ pub.n % pub.nSq = " + x);

        // System.err.println("x is :" + x);
        long cipher = (powpow(pub.g, plain, pub.nSq) * x) % pub.nSq;
        System.out.println("cipher = ((pub.g ^ plaintext % pub.nSq) * x) % pub.nSq = " + cipher);
        return cipher;
    }

    public static long eAdd(PublicKey pub, long a,long b)
    {
        return a * b % pub.nSq;
    }

    public static long eAddConst(PublicKey pub, long a,long n)
    {
        return a * modPow(pub.g, n, pub.nSq) % pub.nSq;
    }

    public static long eMulConst(PublicKey pub, long a,long n)
    {
        return modPow(a, n, pub.nSq);
    }

    public static long decrypt(PrivateKey priv, PublicKey pub, long cipher)
    {
        long x = powpow(cipher, priv.l, pub.nSq) - 1;
        System.out.println("x = (cipher ^ priv.l % pub.nSq) - 1 = " + x);
        long plain = ((x / pub.n) * priv.m) % pub.n;
        System.out.println("plain = ((x / pub.n) * priv.m) % pub.n = " + plain);
        return plain;
    }



    static public class PrivateKey {
        PrivateKey(long p, long q, long n) throws Exception {


            l = (p - 1) * (q - 1);
            System.out.println("priv.l = (P - 1) * (Q - 1) = " + l);
            m = Paillier.invMod(l, n);
            System.out.println("priv.m = invMod(L,N) = " + m);
        }
        public long l;
        public long m;
    }

    static public class PublicKey {
        PublicKey(long nx) {
            n = nx;
            System.out.println("pub.n is: " + n);
            // might need long long for nSq
            nSq = nx * nx;
            System.out.println("pub.nSq = n * n = " + nSq);
            g = nx + 1;
            System.out.println("pub.g = pub.n + 1 = " + g);
        }

        public long n;
        public long nSq;
        public long g;
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

    static public boolean rabinMillerWitness(long test, long possible)
    {
        long a = test;
        long b = possible - 1;
        long n = possible;
        a = a % n;
        long A = a;
        if (A == 1) {
            return false;
        }

        // TODO: to avoid overflow, might need long long t here.
        long t = 1;
        while (t <= b) t <<= 1;
        t >>= 2;

        while (t > 0) {
            A = (A * A) % n;
            if ((t & b) != 0) {
                A = (A * a) % n;
            }
            if (A == 1) {
                return false;
            }
            t >>= 1;
        }
        return true;
    }

    // return a random long in [beg, end)
    static public long randRange(long beg,long end)
    {
        return r.nextInt((int)(end - beg)) + beg;
    }

    static public boolean isPrime(long possible,long k)
    {
        if (possible == 1) {
            return true;
        }
        for (long i : smallPrimes) {
            if (possible == i) {
                return true;
            }
            if (possible % i == 0) {
                return false;
            }
        }
        // only 30 bits are used.
        for (long i = 0; i <= k; i++) {
            // randdom number in [2,possible - 1)
            long test = randRange(2, possible - 1) | 1;
            if (rabinMillerWitness(test, possible)) {
                return false;
            }
        }
        return true;
    }

    static int[] smallPrimes = {2,3,5,7,11,13,17,19,23,29,31,37,41,43,
                                47,53,59,61,67,71,73,79,83,89,97};

    static public long generatePrime(long nBit)
    {
        long k = Math.max(30, nBit * 2);

        while (true) {
            long possible = randRange(((int)Math.pow(2, (nBit - 1))) + 1, (int)Math.pow(2, nBit)) | 1;
            if (isPrime(possible, k)) {
                return possible;
            }
        }
    }

    static Random r = new Random();
}


class Main
{
    public static void main(String[] args) {
        // for (int i = 0; i <= 100000; i++) {
        //     Paillier.KeyPair kp = Paillier.generateKeyPair(12);
        // }
//        int cnt = 0;
//        for (int i = 0; i < 100; i++) {
//            Paillier.KeyPair kp = Paillier.generateKeyPair(12);
//            long a = 100;
//            long aE = Paillier.encrypt(kp.pub, a);
//            if (Paillier.decrypt(kp.priv, kp.pub, aE) != a) {
//                // System.err.println("??");
//                cnt++;
//            }
//        }
//      System.err.println(cnt);
        Paillier.KeyPair kp = Paillier.generateKeyPair(11);
        long a = 500;
        // long b = 122;
        System.out.println("Plaintext is: " + a);
        System.out.println(">>> start encrypt");
        long aE = Paillier.encrypt(kp.pub, a);
        System.out.println("Ciphertext is: " + aE);
        // long bE = Paillier.encrypt(kp.pub, b);
        System.out.println(">>> start decrypt");
        long aD = Paillier.decrypt(kp.priv, kp.pub, aE);
        System.out.println("Decrypted Plaintext is: " + aD);
        // System.out.println("this is the B:" + Paillier.decrypt(kp.priv, kp.pub, bE));
        // System.out.println("this is the Ae:" + aE);
        // long aE3 = Paillier.eAddConst(kp.pub, aE, 5);
        // long ap3 = Paillier.decrypt(kp.priv, kp.pub, aE3);
        // System.out.println(ap3);
        // long bE = Paillier.encrypt(kp.pub, b);
        // long sumE = Paillier.eAdd(kp.pub, aE, bE);
        // long sum = Paillier.decrypt(kp.priv, kp.pub, sumE);
        // System.out.println("this is the ans:" + sum);
    }
}