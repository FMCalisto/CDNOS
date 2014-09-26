JC = javac
.SUFFIXES: .java .class
.java.class:
			$(JC)	$*.java

CLASSES = \
        user.java \
        SS.java \
        LS.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class