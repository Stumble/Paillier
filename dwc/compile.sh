
export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar

hadoop com.sun.tools.javac.Main WordCount2.java

jar cf wc.jar *

rm *.class
