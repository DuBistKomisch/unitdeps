JC = javac
JX = java
SRC = src
BIN = bin
LIB = lib
JARGS = -cp ${BIN}:${LIB}/*

SOURCES = ${wildcard ${SRC}/*.java}
CLASSES = ${patsubst ${SRC}/%.java,${BIN}/%.class,${SOURCES}}

# compile all the files
all: ${CLASSES}

# how to make a .java into a .class
${CLASSES}: ${BIN}/%.class:${SRC}/%.java
	@mkdir -p ${BIN}
	${JC} ${JARGS} -d ${BIN} ${SRC}/$*.java

# generate ptv.db
rmit: all
	${JX} ${JARGS} Scrape rmit cosc????

# delete compiled files
clean:
	rm -rf ${BIN}

# delete database
cleandb:
	rm -f *.db

# print total line count (for funz)
lines:
	cat ${SOURCES} | wc -l
