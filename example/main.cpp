#include "AZExample.h"
#include <iostream>
#include <unistd.h>

int main(int argc, char** argv)
{
  AZExample example;
  example.AZInit();

  for(int i = 0; i < 100; i++)
  {
    example.AZProcessInput(AZExample::A);
    sleep(2);
  }
  std::cout << "Done:" << std::endl;
  return 0;
}
