#include "Automaton.h"
#include "AZ.h"

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
  CALL_MEMBER_FN(*this, m_CurrentInfo.stateMethod) ();
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

#ifdef STATE_DEBUGGING
    m_StateDebugger.Transition(GetStateAsText(m_PreviousInfo.stateIndex), GetInputAsText(a_Input), GetStateAsText(m_CurrentInfo.stateIndex));
#endif

    // Call the transition method
    CALL_MEMBER_FN(*this, m_CurrentInfo.transitionMethod) ();
  }
}

void Automaton::Reset()
{
#ifdef STATE_DEBUGGING
    m_StateDebugger.StateExited(GetStateAsText(m_CurrentInfo.stateIndex));
#endif

  m_CurrentInfo = m_InitialInfo;
  m_PreviousInfo = m_InitialInfo;

#ifdef STATE_DEBUGGING
    m_StateDebugger.StateEntered(GetStateAsText(m_CurrentInfo.stateIndex));
#endif
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

void Automaton::AddChild(Automaton* a_Child, bool a_IsPreExecute, bool a_IsPostExecute)
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

void Automaton::SetInitialInfo(transition_info_t a_InitialInfo)
{
  m_InitialInfo = a_InitialInfo;

#ifdef STATE_DEBUGGING
  m_StateDebugger.StateEntered(GetStateAsText(m_InitialInfo.stateIndex));
#endif
}
