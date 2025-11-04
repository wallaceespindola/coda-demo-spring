#!/bin/bash

echo "========================================="
echo "Running CODA Writer Tests"
echo "========================================="

cd /home/aiyrh/git3/coda-demo-spring

# Set Java 21
export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
export PATH=$JAVA_HOME/bin:$PATH

echo "Java version:"
java -version

echo ""
echo "Compiling project..."
mvn clean compile test-compile -DskipTests -q

echo ""
echo "Running tests..."
mvn surefire:test -Dtest=CodaWriterTest

echo ""
echo "Test execution completed!"
echo "========================================="

