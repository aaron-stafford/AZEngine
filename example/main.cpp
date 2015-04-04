#include "AZExample.h"
#include <iostream>
#include <unistd.h>

int main(int argc, char** argv)
{
  AZExample example;
  example.InitAutomaton();
  for(int i = 0; i < 100; i++)
  {
    example.ProcessInput(AZExample::A);
    sleep(2);
  }
  std::cout << "Done:" << std::endl;
  return 0;
}
