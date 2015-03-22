#include "AZExample.h"
#include "AZNewExample.h"
#include <iostream>

int main(int argc, char** argv)
{
  AZNewExample newExample;
  newExample.AZInit();
  newExample.AZExecuteCurrentState();
  newExample.AZProcessInput(AZNewExample::a);
  newExample.AZExecuteCurrentState();
  newExample.AZProcessInput(AZNewExample::b);
  newExample.AZExecuteCurrentState();
/*

  AZExample example;
  example.AZInit();

  example.AZExecuteCurrentState();
  example.AZProcessInput(AZExample::Loading);
  example.AZExecuteCurrentState();
  example.AZProcessInput(AZExample::FadeInited);
  example.AZExecuteCurrentState();
*/
  std::cout << "Done:" << std::endl;
  return 0;
}
