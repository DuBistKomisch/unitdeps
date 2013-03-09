JC = javac
JX = java
SRC = src
BIN = bin
LIB = lib
DB = db
JARGS = -cp ${BIN}:${LIB}/*

SOURCES = ${wildcard ${SRC}/*.java}
CLASSES = ${patsubst ${SRC}/%.java,${BIN}/%.class,${SOURCES}}

# compile all the files
all: ${CLASSES}

# how to make a .java into a .class
${CLASSES}: ${BIN}/%.class:${SRC}/%.java
	@mkdir -p ${BIN}
	${JC} ${JARGS} -d ${BIN} ${SRC}/$*.java

edit_rmit: all
	${JX} ${JARGS} Editor ${DB}/rmit.db

# generate rmit.db
scrape_rmit: all
	@mkdir -p ${DB}
	${JX} ${JARGS} Scrape rmit cosc????

# delete compiled files
clean:
	rm -rf ${BIN}

# delete database
cleandb:
	rm -rf ${DB}

# print total line count (for funz)
lines:
	cat ${SOURCES} | wc -l
