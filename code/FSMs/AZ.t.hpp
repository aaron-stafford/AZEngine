//
//  Created by Aaron Stafford on 22/05/2014.
//

#include "AZ.h"

template <class T>
AZStateMachine<T>::AZStateMachine()
{
  hashtable = new AZHashtable<T>();
}

template <class T>
AZStateMachine<T>::~AZStateMachine()
{
  delete hashtable;
}

template <class T>
T AZStateMachine<T>::GetNextState(int objectID, int currentState, int input)
{
  int key = objectID << 20;
  key |= currentState << 10;
  key |= input;
  
  T nextState = hashtable->Get(key);
  
  return nextState;
}

template <class T>
void AZStateMachine<T>::InsertTransition(int objectID, int currentState, int input, T newState)
{
  int key = objectID << 20;
  key |= currentState << 10;
  key |= input;
  
  T existingState = hashtable->Put(key, newState);
  
//  if(existingState >= 0)
//  {
//    // transition already exists. Should check for this in debug, not so interested in release.
//  }
}

template <class T>
void AZStateMachine<T>::SetErrorValue(T value)
{
  hashtable->SetErrorValue(value);
}

template <class T>
Entry<T>::Entry()
{
  
}

template <class T>
Entry<T>::Entry(int hash, int key, T value, Entry* next)
{
  m_Hash = hash;
  m_Key = key;
  m_Value = value;
  m_Next = next;
}

template <class T>
Entry<T>::~Entry()
{
  
}

template <class T>
int Entry<T>::GetKey()
{
  return m_Key;
}

template <class T>
T Entry<T>::GetValue()
{
  return m_Value;
}

template <class T>
T Entry<T>::SetValue(T value)
{
  T oldValue = m_Value;
  m_Value = value;
  return oldValue;
}

template <class T>
AZHashtable<T>::AZHashtable()
{
  Init(11, 0.75f);
}

template <class T>
AZHashtable<T>::AZHashtable(int initialCapacity)
{
  Init(initialCapacity, 0.75f);
}

template <class T>
AZHashtable<T>::AZHashtable(int initialCapacity, float loadFactor)
{
  Init(initialCapacity, loadFactor);
}

template <class T>
AZHashtable<T>::~AZHashtable()
{
  delete [] m_Table;
}

template <class T>
void AZHashtable<T>::Init(int initialCapacity, float loadFactor)
{
  m_ModCount = 0;
  m_LoadFactor = loadFactor;
  m_TableLength = initialCapacity;
  
  if (m_TableLength==0)
  {
    m_TableLength = 1;
  }
  
  m_Table = new Entry<T>*[m_TableLength];
  
  for (int i = 0; i < m_TableLength; i++)
  {
    m_Table[i] = 0;
  }
  
  m_RehashThreshold = (int)(m_TableLength * m_LoadFactor);
}

template <class T>
int AZHashtable<T>::Size()
{
  return m_NumEntriesInTable;
}

template <class T>
bool AZHashtable<T>::IsEmpty()
{
  return m_NumEntriesInTable == 0;
}

template <class T>
bool AZHashtable<T>::Contains(T value)
{
  Entry<T>** tab = m_Table;
  for (int i = m_TableLength ; i-- > 0 ;)
  {
    for (Entry<T>* e = tab[i] ; e != 0 ; e = e->m_Next)
    {
      if (e->m_Value == value)
      {
        return true;
      }
    }
  }
  return false;
}

template <class T>
T AZHashtable<T>::Get(int key)
{
  Entry<T>** tab = m_Table;
  int hash = key;
  int index = (hash & 0x7FFFFFFF) % m_TableLength;
  for (Entry<T>* e = tab[index] ; e != 0 ; e = e->m_Next)
  {
    if ((e->m_Hash == hash) && e->m_Key == key)
    {
      return e->m_Value;
    }
  }

  return m_ErrorValue;
}

template <class T>
void AZHashtable<T>::SetErrorValue(T value)
{
  m_ErrorValue = value;
}

template <class T>
T AZHashtable<T>::Put(int key, T value)
{
  // Makes sure the key is not already in the hashtable.
  Entry<T>** tab = m_Table;
  int hash = key;
  int index = (hash & 0x7FFFFFFF) % m_TableLength;
  for (Entry<T>* e = tab[index] ; e != 0 ; e = e->m_Next)
  {
    if ((e->m_Hash == hash) && e->m_Key == key)
    {
      T old = e->m_Value;
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
  Entry<T>* e = tab[index];
  tab[index] = new Entry<T>(hash, key, value, e);
  m_NumEntriesInTable++;
  return value;
}


