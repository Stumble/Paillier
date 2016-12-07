
cp -r input ./wc

cd wc

./simple-run.sh

cd ..

rm -rf encrypt-output
mkdir encrypt-output

cp -r wc/output/* ./encrypt-output
