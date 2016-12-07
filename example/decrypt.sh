# input=""

# if [ $# -lt 0 ]
# then
#     input="$1"
# else
#     echo "use default setting: part-r-00000"
input="part-r-00000"
# fi


cp ./encrypt-output/$input ./enc/

cd ./enc/

java Encryption decrypt $input

cd ..

cp raw-input/part-r-00000 decrypted-part-r-00000


# cp decrypted-$input ../
