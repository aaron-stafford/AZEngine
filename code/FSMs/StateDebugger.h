#ifndef __STATEDEBUGGER_h__
#define __STATEDEBUGGER_h__

#include <string>

class StateDebugger 
{
public:
  StateDebugger();
  ~StateDebugger();

  void StateEntered(const std::string& a_State) const;
  void StateExited(const std::string& a_State) const;
  void Transition(const std::string& a_PreviousState, const std::string& a_Input, const std::string& a_NewState) const;
  void SetDiagramName(const std::string& a_DiagramName);
private:
  std::string m_DiagramName;
};

#endif // __STATEDEBUGGER_h__

