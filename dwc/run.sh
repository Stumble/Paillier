
hd="/usr/local/hadoop"

export HADOOP_HOME=${hd}
export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar


rm -rf /tmp/hadoop-stumble

rm /tmp/.mh-pub.key

rm -rf output

mkdir output

${hd}/bin/hdfs namenode -format

${hd}/sbin/stop-all.sh

${hd}/sbin/start-all.sh

# ${hd}/sbin/start-yarn.sh

${hd}/bin/hdfs dfs -mkdir /user

${hd}/bin/hdfs dfs -mkdir /user/stumble

# ${hd}/bin/hdfs dfs -mkdir /user/<username>

${hd}/bin/hdfs dfs -put input input

${hd}/bin/hdfs dfs -get input/pub.key /tmp/.mh-pub.key

${hd}/bin/hadoop jar wc.jar WordCount2 input output -skip input/pub.key

${hd}/bin/hdfs dfs -get output output

cat output/*

# ${hd}/sbin/stop-dfs.sh

${hd}/sbin/stop-all.sh
