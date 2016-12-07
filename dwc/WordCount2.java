import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
// import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.StringUtils;


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
            return readPubKey("pub.key");
        }

        static PublicKey readPubKey(String dir) {
            FileReader fr = null;
            try {
                fr = new FileReader(dir);
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


public class WordCount2 {

    public static class TokenizerMapper
        extends Mapper<Object, Text, Text, Text>{

        static enum CountersEnum { INPUT_WORDS }

        // private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        private boolean caseSensitive;
        private Set<String> patternsToSkip = new HashSet<String>();

        private Configuration conf;
        private BufferedReader fis;

        @Override
        public void setup(Context context) throws IOException,
        InterruptedException {
            conf = context.getConfiguration();
            caseSensitive = conf.getBoolean("wordcount.case.sensitive", true);
            if (conf.getBoolean("wordcount.skip.patterns", false)) {
                URI[] patternsURIs = Job.getInstance(conf).getCacheFiles();
                for (URI patternsURI : patternsURIs) {
                    Path patternsPath = new Path(patternsURI.getPath());
                    String patternsFileName = patternsPath.getName().toString();
                    parseSkipFile(patternsFileName);
                }
            }
        }

        private void parseSkipFile(String fileName) {
            try {
                fis = new BufferedReader(new FileReader(fileName));
                String pattern = null;
                while ((pattern = fis.readLine()) != null) {
                    patternsToSkip.add(pattern);
                }
            } catch (IOException ioe) {
                System.err.println("Caught exception while parsing the cached file '"
                                   + StringUtils.stringifyException(ioe));
            }
        }

        @Override
        public void map(Object key, Text value, Context context
                       ) throws IOException, InterruptedException {

            Configuration conf = context.getConfiguration();
            BigInteger publicKeyN = new BigInteger(conf.get("Paillier.publicKey"));
            Paillier.PublicKey pk = new Paillier.PublicKey(publicKeyN);

            Text one = new Text(Paillier.encrypt(pk, 1).toString());

            String line = (caseSensitive) ?
            value.toString() : value.toString().toLowerCase();
            for (String pattern : patternsToSkip) {
                line = line.replaceAll(pattern, "");
            }
            StringTokenizer itr = new StringTokenizer(line);
            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken());
                context.write(word, one);
                Counter counter = context.getCounter(CountersEnum.class.getName(),
                                                     CountersEnum.INPUT_WORDS.toString());
                counter.increment(1);
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
        Paillier.PublicKey pk = Paillier.PublicKey.readPubKey("input/pub.key");
        // try {
        //     pk = Paillier.PublicKey.readPubKey("/tmp/.mh-pub.key");
        // }
        // catch (Throwable e) {
        //     pk = Paillier.PublicKey.readPubKey("input/pub.key");
        //     System.out.println("INFO use pub.key in input folder");
        //     e.printStackTrace();
        // }

        BigInteger pub_n = pk.n;
        Configuration conf = new Configuration();
        conf.set("Paillier.publicKey", pub_n.toString());
        GenericOptionsParser optionParser = new GenericOptionsParser(conf, args);
        String[] remainingArgs = optionParser.getRemainingArgs();
        if (!(remainingArgs.length != 2 || remainingArgs.length != 4)) {
            System.err.println("Usage: wordcount <in> <out> [-skip skipPatternFile]");
            System.exit(2);
        }
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(WordCount2.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        List<String> otherArgs = new ArrayList<String>();
        for (int i=0; i < remainingArgs.length; ++i) {
            if ("-skip".equals(remainingArgs[i])) {
                job.addCacheFile(new Path(remainingArgs[++i]).toUri());
                job.getConfiguration().setBoolean("wordcount.skip.patterns", true);
            } else {
                otherArgs.add(remainingArgs[i]);
            }
        }
        FileInputFormat.addInputPath(job, new Path(otherArgs.get(0)));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs.get(1)));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}