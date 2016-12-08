if [ $# -lt 1 ]
then
    echo "need input file"
    exit 1
fi

rm -rf input/
mkdir input

input="$1"

cd enc

rm *.key
rm $input

cd ..

cp ./raw-input/$input ./enc/

cd ./enc/

java Encryption encrypt $input

cp ./encrypted-$input ../input
cp ./pub.key ../input
cp ./priv.key ../
