import java.io.IOException;
import java.util.StringTokenizer;
import java.math.BigInteger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
// import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


import java.math.BigInteger;
import java.util.*;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;

// Paillier class

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

        public void writePrivKey(String file) {
            try {
                FileWriter fw = new FileWriter(file);
                fw.write(l.toString());
                fw.write("\n");
                fw.write(m.toString());
                fw.write("\n");
                fw.close();
            }
            catch (Throwable e) {
                System.out.println("Write PrivKey: Error " + e.getMessage());
                e.printStackTrace();
            }
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

        public void writePubKey(String file) {
            try {
                FileWriter fw = new FileWriter(file);
                fw.write(n.toString());
                fw.write("\n");
                fw.close();
            }
            catch (Throwable e) {
                System.out.println("WritePubKey: Error " + e.getMessage());
                e.printStackTrace();
            }
        }

        static PublicKey readPubKey() {
            return readPubKey("");
        }

        static PublicKey readPubKey(String dir) {
            FileReader fr = null;
            try {
                fr = new FileReader(dir + "pub.key");
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

        static KeyPair readKeyPair() {
            PublicKey pub = PublicKey.readPubKey();
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
            PrivateKey priv = new PrivateKey(l, m);
            KeyPair pk = new KeyPair(priv, pub);
            return pk;
        }

        public PrivateKey priv;
        public PublicKey pub;
    }


    static public class Primes {
        static public BigInteger generatePrime(long nBit)
        {
            if (nBit < 40) {
                nBit = 40;
            }
            return BigInteger.probablePrime((int)nBit, r);
        }

        static Random r = new Random();
    }

}


// Paillier class end




public class WordCount {

    public static class TokenizerMapper
        extends Mapper<Object, Text, Text, Text>{

        private final static Text one = new Text("1");
        private Text word = new Text();

        public void map(Object key, Text value, Context context
                       ) throws IOException, InterruptedException {

            Configuration conf = context.getConfiguration();
            BigInteger publicKeyN = new BigInteger(conf.get("Paillier.publicKey"));
            Paillier.PublicKey pk = new Paillier.PublicKey(publicKeyN);

            Text one = new Text(Paillier.encrypt(pk, 1).toString());

            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken());
                context.write(word, one);
            }
        }
    }

    public static class IntSumReducer
        extends Reducer<Text,Text,Text,Text> {
        private Text result = new Text();

        public void reduce(Text key, Iterable<Text> values,
                           Context context
                          ) throws IOException, InterruptedException {
            // BigInteger sumE = Paillier.encrypt(0);
            BigInteger sum = BigInteger.ZERO;
            // int sum = 0;
            for (Text textVal : values) {
                BigInteger val = new BigInteger(textVal.toString());
                sum = sum.add(val);
                // sum += val.get();
            }
            Text rtn = new Text(sum.toString());
            result.set(rtn);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {

        // generate keyPairs
        // this keygen should be done in another program
        // then every program get the pub_key
        // only the decrypt program got priv_key
        // Paillier.KeyPair kp = Paillier.generateKeyPair(8);
        // Paillier.PublicKey pk = kp.pub;
        Paillier.PublicKey pk = Paillier.PublicKey.readPubKey(args[0]);
        BigInteger pub_n = pk.n;

        Configuration conf = new Configuration();
        conf.set("Paillier.publicKey", pub_n.toString());

        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(WordCount.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}