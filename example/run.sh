
rm -rf ./output

export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar:/home/stumble/learn/UCLA/CS211/hehe/build/Paillier.jar

/home/stumble/learn/UCLA/CS211/hadoop/hd/bin/hadoop jar wc.jar WordCount ./input/ ./output/