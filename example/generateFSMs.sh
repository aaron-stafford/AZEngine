#!/bin/bash

java -jar ../AZClassGenerator.jar --output-automaton
java -jar ../AZClassGenerator.jar --diagram scxml/example.scxml --class-name Example --file-type .h --file-type .cpp
