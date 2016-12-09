
# hadoop jar compile version
# export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar
# /home/stumble/learn/UCLA/CS211/hadoop/hd/bin/hadoop com.sun.tools.javac.Main Paillier.java -d ./build/
# cd build
# jar cvf Paillier.jar Paillier/*

# normal version
rm -rf build
mkdir build
cp Paillier.java ./build/Main.java
javac -d ./build ./build/Main.java
cd build
jar cvf Paillier.jar *
