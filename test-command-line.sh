home=`pwd`/_Docs

rm -rf $home/result/*

# Sample 1
idx=1
java -jar traprange.lastest.jar -in "$home/sample-$idx.pdf" -out "$home/result/sample-$idx.html" -el "0,1,-1"

# Sample 2
idx=2
java -jar traprange.lastest.jar -in "$home/sample-$idx.pdf" -out "$home/result/sample-$idx.html" -el "0,1"

# Sample 3
idx=3
java -jar traprange.lastest.jar -in "$home/sample-$idx.pdf" -out "$home/result/sample-$idx.html" -ep "0"

# Sample 4
idx=4
java -jar traprange.lastest.jar -in "$home/sample-$idx.pdf" -out "$home/result/sample-$idx.html" -el "0"

# Sample 5
idx=5
java -jar traprange.lastest.jar -in "$home/sample-$idx.pdf" -out "$home/result/sample-$idx.html" -el "0@0,1@0"

