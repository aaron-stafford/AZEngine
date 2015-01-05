#include "Automaton.h"
#include "AZ.h"

#define CALL_MEMBER_FN(object,ptrToMember)  ((object).*(ptrToMember))

Automaton::Automaton()
{
  stateMachine = new AZStateMachine();
}

Automaton::~Automaton()
{
  delete stateMachine;
}

void Automaton::AZExecuteCurrentState()
{
  while(CALL_MEMBER_FN(*this, mi[m_CurrentState]) ())
    ;
}

void Automaton::AZProcessInput(int input)
{
  int newState = stateMachine->GetNextState(0, m_CurrentState, input);
  if(newState >= 0)
  {
    m_CurrentState = newState;
  }
}

void Automaton::AZInit()
{
}
