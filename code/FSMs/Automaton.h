#ifndef __Automaton__
#define __Automaton__

class AZStateMachine;

class Automaton
{
public:
  // Member variables
  int m_CurrentState;
  
  // Init
  Automaton();
  ~Automaton();
  void AZExecuteCurrentState();
  void AZProcessInput(int input);
  virtual void AZInit();
protected:
  AZStateMachine* stateMachine;
  typedef bool(Automaton::*MethodIndex)();
  MethodIndex* mi;
};

#endif

