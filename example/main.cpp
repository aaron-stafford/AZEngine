#include "AZExample.h"
#include "stdio.h"

int main(int argc, char** argv)
{
  AZExample example;

  example.AZInit();

  example.AZExecuteCurrentState();
  return 0;
}
