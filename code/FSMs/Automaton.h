#ifndef __Automaton__
#define __Automaton__

#ifdef STATE_DEBUGGING
#include <string>
#include "StateDebugger.h"
#endif

#include <list>
#include "AZ.h"
#include "AZ.t.hpp"

class Automaton
{
public:
  Automaton();
  ~Automaton();
  void ExecuteCurrentState();
  void ProcessInput(int input);

  /**
   * Sets the current state to the initial state.
   */
  void Reset();
  virtual void InitAutomaton();
  virtual void PreExecuteCurrentState();
  virtual void PostExecuteCurrentState();
  void AddChild(Automaton* a_Child, bool a_IsPreExecute, bool a_IsPostExecute);
  void SetParent(Automaton* a_Parent);
#ifdef STATE_DEBUGGING
  virtual std::string GetStateAsText(int StateIndex) = 0;
  virtual std::string GetInputAsText(int InputIndex) = 0;
  virtual std::string GetTemplateName() = 0;
#endif
protected:
  typedef void(Automaton::*MethodIndex)();
  Automaton* m_Parent;

  struct transition_info_t
  {
    MethodIndex transitionMethod;
    MethodIndex stateMethod;
    int stateIndex;
  };

  AZStateMachine<transition_info_t> stateMachine;
  std::list<Automaton*> m_PreExecuteStateChildren;
  std::list<Automaton*> m_PostExecuteStateChildren;

  transition_info_t m_CurrentInfo;
  transition_info_t m_PreviousInfo;
  transition_info_t m_InitialInfo;

  void SetInitialInfo(transition_info_t a_InitialInfo);
#ifdef STATE_DEBUGGING
  StateDebugger m_StateDebugger;
#endif
};
#endif
