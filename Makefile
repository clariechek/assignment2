# # Directory for compiled classes
# BIN = ./bin/

# # Directory for source files
# SRC = ./src/

FLAGS = -g -d -cp java-json.jar

COMPILE = javac $(FLAGS)

JAVA_FILES = $(wildcard ./*.java)

CLASS_FILES = $(JAVA_FILES:.java=.class)

all: clean $(addprefix ./, $(notdir $(CLASS_FILES)))

./%.class: ./%.java
	@mkdir -p ./
	$(COMPILE) $<

clean:
	rm -rf ./*

# Compilation: javac -cp first.jar:second.jar:third.jar YourClass.java YourClass2.java
# Run: java -cp json-simple-1.1.jar ContentServer.java csdata.json