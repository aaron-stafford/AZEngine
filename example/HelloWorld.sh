#!/bin/bash

java -jar AZClassGenerator.jar --output-automaton
#java -jar AZClassGenerator.jar --diagram example.scxml --class-name Example --file-type .h --file-type .cpp

g++ -c AZ.cpp
g++ -c Automaton.cpp
g++ -c AZExample.cpp
g++ AZ.o Automaton.o AZExample.o main.cpp

