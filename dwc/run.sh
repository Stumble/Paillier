
hd="/home/stumble/learn/UCLA/CS211/hadoop/hd"

export HADOOP_HOME=${hd}
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
# export PATH=$PATH:$HADOOP_HOME/bin;$HADOOP_HOME/sbin
export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar


rm -rf /tmp/hadoop-stumble

${hd}/bin/hdfs namenode -format

${hd}/sbin/stop-all.sh

${hd}/sbin/start-all.sh

# ${hd}/sbin/start-yarn.sh

${hd}/bin/hdfs dfs -mkdir /user

${hd}/bin/hdfs dfs -mkdir /user/stumble

# ${hd}/bin/hdfs dfs -mkdir /user/<username>

${hd}/bin/hdfs dfs -put input input

${hd}/bin/hdfs dfs -get input/pub.key /tmp/.mh-pub.key

${hd}/bin/hadoop jar wc.jar WordCount2 input output

${hd}/bin/hdfs dfs -get output output

cat output/*

# ${hd}/sbin/stop-dfs.sh

${hd}/sbin/stop-all.sh