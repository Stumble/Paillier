if [ $# -lt 1 ]
then
    echo "need input file"
    exit 1
fi

input="$1"

cp ./raw-input/$input ./enc/

cd ./enc/

java Encryption encrypt $input

cp ./encrypted-$input ../input
cp ./pub.key ../input
cp ./priv.key ../
