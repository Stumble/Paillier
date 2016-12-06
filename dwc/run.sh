
hd="/home/stumble/learn/UCLA/CS211/hadoop/hd"

export HADOOP_HOME=${hd}
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
# export PATH=$PATH:$HADOOP_HOME/bin;$HADOOP_HOME/sbin
export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar


${hd}/bin/hdfs namenode -format

${hd}/sbin/start-dfs.sh

${hd}/bin/hadoop jar wc.jar WordCount2 ./input/ ./output/

${hd}/sbin/stop-dfs.sh