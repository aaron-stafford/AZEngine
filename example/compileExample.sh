#!/bin/bash

DEFINES=STATE_DEBUGGING

g++ -c -D $DEFINES AZ.cpp
g++ -c -D $DEFINES TCPClient.cpp
g++ -c -D $DEFINES StateDebugger.cpp
g++ -c -D $DEFINES Automaton.cpp
g++ -c -D $DEFINES AZExample.cpp
g++ -c -D $DEFINES main.cpp
g++ AZ.o Automaton.o AZExample.o StateDebugger.o TCPClient.o main.o
