JFLAGS = -g
JC = javac
JAR = jar
CLEAN = clean
MAKE = make

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Main.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
	$(RM) *.jar
	
jar: $(classes)
	$(JAR) cmvf ../../MANIFEST stm2ir.jar Main.class

stm2ir1:
	$(MAKE) $(CLEAN)
	$(RM) stm2ir.jar
	$(MAKE)
	$(MAKE) $(JAR)