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

import Paillier.*;

public class WordCount {

    public static class TokenizerMapper
        extends Mapper<Object, Text, Text, Text>{

        private final static Text one = new Text("1");
        private Text word = new Text();

        public void map(Object key, Text value, Context context
                       ) throws IOException, InterruptedException {

            Configuration conf = context.getConfiguration();
            BigInteger publicKeyN = new BigInteger(conf.get("Paillier.publicKey"));
            Paillier.PublicKey pk = Paillier.genPublicKey(publicKeyN);

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
        Paillier.KeyPair kp = Paillier.generateKeyPair(8);
        BigInteger pub_n = kp.pub.n;


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