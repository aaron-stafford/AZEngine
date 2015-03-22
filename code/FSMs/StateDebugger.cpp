#include "StateDebugger.h"
#include "TCPClient.h"

#define STATE_HIDE "0"
#define STATE_SHOW "1"
#define TRANSITION_HIDE "2"
#define TRANSITION_SHOW "3"

static TCPClient m_GraphicalDebugger;

StateDebugger::StateDebugger() {}
StateDebugger::~StateDebugger() {}

void StateDebugger::StateEntered(const std::string& a_State) const
{
  m_GraphicalDebugger.Send("1 " + a_State + "\n");
}

void StateDebugger::StateExited(const std::string& a_State) const
{
  m_GraphicalDebugger.Send("0 " + a_State + "\n");
}

void StateDebugger::Transition(const std::string& a_PreviousState, const std::string& a_Input, const std::string& a_NewState) const
{
  //StateExited(a_PreviousState);
  m_GraphicalDebugger.Send("3 " + a_PreviousState + " -> " + a_NewState + "\n");
  //m_GraphicalDebugger.Send("2 " + a_PreviousState + " -> " + a_NewState + "\n");
  StateEntered(a_NewState);
}

