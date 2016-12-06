
# export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar:/home/stumble/learn/UCLA/CS211/hehe/build/Paillier.jar

export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar

/home/stumble/learn/UCLA/CS211/hadoop/hd/bin/hadoop com.sun.tools.javac.Main WordCount2.java

jar cf wc.jar *

rm *.class
