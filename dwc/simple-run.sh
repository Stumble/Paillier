
hd="/home/stumble/learn/UCLA/CS211/hadoop/hd"

export HADOOP_HOME=${hd}
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
# export PATH=$PATH:$HADOOP_HOME/bin;$HADOOP_HOME/sbin
export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar


rm -rf output

# mkdir output

${hd}/bin/hadoop jar wc.jar WordCount2 input output -skip input/pub.key

cat output/*
