// need ramdom


class Paillier {
    public int invMod(int a, int p, int maxiter=1000000)
    {
        if (a == 0) {
            // throw exception
            // 0 has no inverse mod
        }
        int r = a;
        int d = 1;
        boolean flag = false;
        for (int i = 0; i < min(p, maxiter); i++) {
            d = ((p / r + 1) * d) % p;
            r = (d * a) % p;
            if (r == 1) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            // throw excepetion, a has no inverse mod p
        }
        return d;
    }

    public int modPow(int base, int exp, int mod)
    {
        int result = 1;
        while (exp > 0) {
            if (exp & 1 == 1) {
                result = (result * base) % mod;
            }
            exp = exp >> 1;
            base = (base * base) % mod;
        }
        return result;
    }

    class PrivateKey {
        PrivateKey(int p, int q, int n) {
            l = (p - 1) * (q - 1);
            m =invMod(l, n);
        }
        int l;
        int m;
    }

    class PublicKey {
        PublicKey(int n) {
            n = n;
            // might need long long for nSq
            nSq = n * n;
            g = n + 1;
        }

        static void fromN(int n) {
            // ..
            return PrivateKey(n);
        }

        int n;
        int n_sq;
        int g;
    }

    static pair<PrivateKey, PublicKey> generateKeyPair(int bits) {
        int p = primes.generatePrime(bits / 2);
        int q = primes.generatePrime(bits / 2);
        int n = p * q;
        return ....;
    }

    static int encrypt(PublicKey pub, int plain)
    {
        while(true) {
            r = primes.generatePrime(round(math.log(pub.n, 2)));
            if (r > 0 && r < pub.n) {
                break;
            }
        }
        int x = pow(r, pub.n, pub.nSq);
        int cipher = (pow(pub.g, plain, pub.nSq) * x) % pub.nSq;
        return cipher;
    }

    static int eAdd(PublicKey pub, int a,int b)
    {
        return a * b % pub.nSq;
    }

    static int eAddConst(PublicKey pub, int a,int b)
    {
        return a * modpow(pub.g, n, pub.n_sq) % pub.n_sq;
    }

    static int eMulConst(PublicKey pub, int a,int b)
    {
        return modpow(a, n, pub.n_sq);
    }

    static int decrypt(PrivateKey priv, PublicKey pub, int cipher)
    {
        int x = pow(cipher, priv.l, pub.nSq) - 1;
        int plain = ((x / pub.n) * priv.m) % pub.n;
        return plain;
    }
}



class Primes {
    public boolean rabinMillerWitness(int test, int possible)
    {
        int a = test;
        int b = possible - 1;
        int n = possible;
        a = a % n;
        int A = a;
        if (A == 1) {
            return false;
        }

        // TODO: to avoid overflow, might need long long t here.
        int t = 1;
        while (t <= b) t <<= 1;
        t >>= 2;

        while (t > 0) {
            A = (A * A) % n;
            if (t & b) {
                A = (A * a) % n;
            }
            if (A == 1) {
                return false;
            }
            t >>= 1;
        }
        return true;
    }

    // return a random int in [beg, end)
    public int randRange(int beg,int end)
    {
        return r.nextInt(end - beg) + beg;
    }

    public boolean isPrime(int possible,int k)
    {
        if (possible == 1) {
            return true;
        }
        for (int i : smallPrimes) {
            if (possible == i) {
                return true;
            }
            if (possible % i == 0) {
                return false;
            }
        }
        // only 30 bits are used.
        for (itn i = 0; i <= k; i++) {
            // randdom number in [2,possible - 1)
            int test = randRange(2, possible - 1) | 1;
            if (rabinMillerWitness(test, possible)) {
                return false;
            }
        }
        return true;
    }

    static Int[] smallPrimes = {2,3,5,7,11,13,17,19,23,29,31,37,41,43,
                                47,53,59,61,67,71,73,79,83,89,97};

    public int generatePrime(int nBit)
    {
        int k = max(30, nBit * 2);

        while (true) {
            int possible = randRange(2 ^ (nBit - 1) + 1, 2 ^ nBit) | 1;
            if (isPrime(possible, k)) {
                return possible;
            }
        }
    }

}