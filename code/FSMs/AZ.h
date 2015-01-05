//
//  AZStateMachine.h
//  Jumpman
//
//  Created by Aaron Stafford on 22/05/2014.
//
//

#ifndef __AZStateMachine__
#define __AZStateMachine__
class AZHashtable;

class AZStateMachine
{
public:
  AZStateMachine();
  ~AZStateMachine();
  int GetNextState(int objectID, int currentState, int input);
  void InsertTransition(int objectID, int currentState, int input, int newState);
private:
  AZHashtable* hashtable;
};

// Types of Enumerations/Iterations
static int KEYS = 0;
static int VALUES = 1;
static int ENTRIES = 2;

class Entry
{
public:
  Entry();
  Entry(int hash, int key, int value, Entry* next);
  ~Entry();
  int GetKey();
  int GetValue();
  int SetValue(int value);
  //int Equals(Object o);
  //int HashCode();
  
  Entry* m_Next;
  int m_Value;
  int m_Hash;
  int m_Key;
};

class AZHashtable
{
public:
  AZHashtable();
  AZHashtable(int initialCapacity);
  AZHashtable(int initialCapacity, float loadFactor);
  ~AZHashtable();
  
  int Size();
  bool IsEmpty();
  bool Contains(int value);
  int Get(int key);
  int Put(int key, int value);
private:
  Entry** m_Table;
  int m_TableLength;
  int m_NumEntriesInTable;
  int m_RehashThreshold;
  float m_LoadFactor;
  int m_ModCount = 0;
  
  void Init(int initialCapacity, float loadFactor);
};
#endif /* defined(__AZStateMachine__) */
