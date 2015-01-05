//
//  Created by Aaron Stafford on 22/05/2014.
//

#include "AZ.h"

AZStateMachine::AZStateMachine()
{
  hashtable = new AZHashtable();
}

AZStateMachine::~AZStateMachine()
{
  delete hashtable;
}

int AZStateMachine::GetNextState(int objectID, int currentState, int input)
{
  int key = objectID << 20;
  key |= currentState << 10;
  key |= input;
  
  int nextState = hashtable->Get(key);
  
  return nextState;
}

void AZStateMachine::InsertTransition(int objectID, int currentState, int input, int newState)
{
  int key = objectID << 20;
  key |= currentState << 10;
  key |= input;
  
  int existingState = hashtable->Put(key, newState);
  
  if(existingState >= 0)
  {
    // transition already exists. Should check for this in debug, not so interested in release.
  }
}

Entry::Entry()
{
  
}

Entry::Entry(int hash, int key, int value, Entry* next)
{
  m_Hash = hash;
  m_Key = key;
  m_Value = value;
  m_Next = next;
}

Entry::~Entry()
{
  
}

int Entry::GetKey()
{
  return m_Key;
}

int Entry::GetValue()
{
  return m_Value;
}

int Entry::SetValue(int value)
{
  int oldValue = m_Value;
  m_Value = value;
  return oldValue;
}

AZHashtable::AZHashtable()
{
  Init(11, 0.75f);
}

AZHashtable::AZHashtable(int initialCapacity)
{
  Init(initialCapacity, 0.75f);
}

AZHashtable::AZHashtable(int initialCapacity, float loadFactor)
{
  Init(initialCapacity, loadFactor);
}

AZHashtable::~AZHashtable()
{
  delete [] m_Table;
}

void AZHashtable::Init(int initialCapacity, float loadFactor)
{
  m_LoadFactor = loadFactor;
  m_TableLength = initialCapacity;
  
  if (m_TableLength==0)
  {
    m_TableLength = 1;
  }
  
  m_Table = new Entry*[m_TableLength];
  
  for (int i = 0; i < m_TableLength; i++)
  {
    m_Table[i] = 0;
  }
  
  m_RehashThreshold = (int)(m_TableLength * m_LoadFactor);
}

int AZHashtable::Size()
{
  return m_NumEntriesInTable;
}

bool AZHashtable::IsEmpty()
{
  return m_NumEntriesInTable == 0;
}

bool AZHashtable::Contains(int value)
{
  Entry** tab = m_Table;
  for (int i = m_TableLength ; i-- > 0 ;)
  {
    for (Entry* e = tab[i] ; e != 0 ; e = e->m_Next)
    {
      if (e->m_Value == value)
      {
        return true;
      }
    }
  }
  return false;
}

int AZHashtable::Get(int key)
{
  Entry** tab = m_Table;
  int hash = key;
  int index = (hash & 0x7FFFFFFF) % m_TableLength;
  for (Entry* e = tab[index] ; e != 0 ; e = e->m_Next)
  {
    if ((e->m_Hash == hash) && e->m_Key == key)
    {
      return e->m_Value;
    }
  }
  return -1;
}

int AZHashtable::Put(int key, int value)
{
  // Makes sure the key is not already in the hashtable.
  Entry** tab = m_Table;
  int hash = key;
  int index = (hash & 0x7FFFFFFF) % m_TableLength;
  for (Entry* e = tab[index] ; e != 0 ; e = e->m_Next)
  {
    if ((e->m_Hash == hash) && e->m_Key == key)
    {
      int old = e->m_Value;
      e->m_Value = value;
      return old;
    }
  }
  
  m_ModCount++;
  if (m_NumEntriesInTable >= m_RehashThreshold)
  {
    // Rehash the table if the threshold is exceeded
    //rehash();
    
    tab = m_Table;
    index = (hash & 0x7FFFFFFF) % m_TableLength;
  }
  
  // Creates the new entry.
  Entry* e = tab[index];
  tab[index] = new Entry(hash, key, value, e);
  m_NumEntriesInTable++;
  return 0;
}
