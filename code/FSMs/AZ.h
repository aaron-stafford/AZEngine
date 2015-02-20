//  Created by Aaron Stafford on 22/05/2014.

#ifndef __AZStateMachine__
#define __AZStateMachine__

// Types of Enumerations/Iterations
static int KEYS = 0;
static int VALUES = 1;
static int ENTRIES = 2;

template <class T>
class Entry
{
public:
  Entry();
  Entry(int hash, int key, T value, Entry* next);
  ~Entry();
  int GetKey();
  T GetValue();
  T SetValue(T value);
  //int Equals(Object o);
  //int HashCode();
  
  Entry* m_Next;
  T m_Value;
  int m_Hash;
  int m_Key;
};

template <class T>
class AZHashtable
{
public:
  AZHashtable();
  AZHashtable(int initialCapacity);
  AZHashtable(int initialCapacity, float loadFactor);
  ~AZHashtable();
  
  int Size();
  bool IsEmpty();
  bool Contains(T value);
  T Get(int key);
  T Put(int key, T value);
  void SetErrorValue(T value);
private:
  Entry<T>** m_Table;
  int m_TableLength;
  int m_NumEntriesInTable;
  int m_RehashThreshold;
  float m_LoadFactor;
  int m_ModCount;
  T m_ErrorValue;
  
  void Init(int initialCapacity, float loadFactor);
};

template <class T>
class AZStateMachine
{
public:
  AZStateMachine();
  ~AZStateMachine();
  T GetNextState(int objectID, int currentState, int input);
  void InsertTransition(int objectID, int currentState, int input, T newState);
  void SetErrorValue(T value);
private:
  AZHashtable<T>* hashtable;
};

#endif /* defined(__AZStateMachine__) */


