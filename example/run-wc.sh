
cp -r input ./wc

cd wc

./run.sh

cd ..

rm -rf encrypt-output
mkdir encrypt-output

cp -r wc/output/* ./encrypt-output
