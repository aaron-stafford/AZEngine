#include "StateDebugger.h"
#include "TCPClient.h"

static TCPClient m_GraphicalDebugger;

StateDebugger::StateDebugger() {}
StateDebugger::~StateDebugger() {}

void StateDebugger::StateEntered(const std::string& a_State) const
{
  m_GraphicalDebugger.Send("5 " + m_DiagramName + " " + a_State + "\n");
}

void StateDebugger::StateExited(const std::string& a_State) const
{
  m_GraphicalDebugger.Send("4 " + m_DiagramName + " " + a_State + "\n");
}

void StateDebugger::Transition(const std::string& a_PreviousState, const std::string& a_Input, const std::string& a_NewState) const
{
  StateExited(a_PreviousState);
  m_GraphicalDebugger.Send("7 " + m_DiagramName + " " + a_PreviousState + " -> " + a_NewState + "\n");
  m_GraphicalDebugger.Send("6 " + m_DiagramName + " " + a_PreviousState + " -> " + a_NewState + "\n");
  StateEntered(a_NewState);
}

void StateDebugger::SetDiagramName(const std::string& a_DiagramName)
{
  m_DiagramName = a_DiagramName;
}
