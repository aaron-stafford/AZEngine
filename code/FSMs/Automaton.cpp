#include "Automaton.h"
#include "AZ.h"

#ifdef STATE_DEBUGGING
#include <iostream>
#endif

#define CALL_MEMBER_FN(object,ptrToMember)  ((object).*(ptrToMember))

Automaton::Automaton()
{
  m_Parent = 0;

  transition_info_t errorInfo; 
  errorInfo.stateIndex = -1;
  stateMachine.SetErrorValue(errorInfo);
}

Automaton::~Automaton()
{
  m_PreExecuteStateChildren.clear();
  m_PostExecuteStateChildren.clear();
}

void Automaton::AZExecuteCurrentState()
{
  PreExecuteCurrentState();

#ifdef STATE_DEBUGGING
  std::cout << GetStateAsText(m_CurrentInfo.stateIndex) << std::endl;
#endif

  while(CALL_MEMBER_FN(*this, m_CurrentInfo.stateMethod) ())
    ;

  PostExecuteCurrentState();
}

void Automaton::AZProcessInput(int a_Input)
{
  transition_info_t newState = stateMachine.GetNextState(0, m_CurrentInfo.stateIndex, a_Input);

  if(newState.stateIndex >= 0)
  { 
#ifdef STATE_DEBUGGING
    m_PreviousInfo = m_CurrentInfo;
#endif
    m_CurrentInfo = newState;
  }

#ifdef STATE_DEBUGGING
  std::cout << GetInputAsText(a_Input) << std::endl;
#endif

  // Call the transition method
  CALL_MEMBER_FN(*this, m_CurrentInfo.transitionMethod) ();
}

void Automaton::AZInit()
{
}

void Automaton::PreExecuteCurrentState()
{
    for(std::list<Automaton*>::const_iterator iterator = m_PreExecuteStateChildren.begin(); iterator != m_PreExecuteStateChildren.end(); ++iterator)
    {
      ((Automaton*)*iterator)->AZExecuteCurrentState();
    }
}

void Automaton::PostExecuteCurrentState()
{
    for(std::list<Automaton*>::const_iterator iterator = m_PostExecuteStateChildren.begin(); iterator != m_PostExecuteStateChildren.end(); ++iterator)
    {
      ((Automaton*)*iterator)->AZExecuteCurrentState();
    }
}

void Automaton::AddChild(Automaton* a_Child, bool a_IsPreExecute, bool a_IsPostExecute, bool a_IsProcessInput)
{
  if(a_IsPreExecute)
  {
    m_PreExecuteStateChildren.push_back(a_Child);
  }

  if(a_IsPostExecute)
  {
    m_PostExecuteStateChildren.push_back(a_Child);
  }
}

void Automaton::SetParent(Automaton* a_Parent)
{
  m_Parent = a_Parent;
}
