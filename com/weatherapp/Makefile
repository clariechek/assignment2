# Directory for compiled classes
BIN = ./bin/

# Directory for source files
SRC = ./src/

FLAGS = -g -d $(BIN) -cp $(SRC):json-simple-1.1.jar

COMPILE = javac $(FLAGS)

JAVA_FILES = $(wildcard $(SRC)*.java)

CLASS_FILES = $(JAVA_FILES:.java=.class)

all: clean $(addprefix $(BIN), $(notdir $(CLASS_FILES)))

$(BIN)%.class: $(SRC)%.java
	@mkdir -p $(BIN)
	$(COMPILE) $<

clean:
	rm -f *.class

# Compilation: javac -cp first.jar:second.jar:third.jar YourClass.java YourClass2.java
# Run: java -cp json-simple-1.1.jar ContentServer.java weather.json