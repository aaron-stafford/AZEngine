/*
 Begin definition of the hash table Entry class
*/

class Entry
{
  constructor(key, value)
  {
    this.key = key;
    this.value = value;
  }
 
  getKey()
  {
    return this.key;
  }

  getValue()
  {
    return this.value;
  }

  setValue(value)
  {
    var oldValue = this.value;
    this.value = value;
    return oldValue;
  }
}


/*
 Begin definition of the HashTable class
*/
class HashTable
{
  constructor(capacity)
  {
    this.capacity = capacity
    this.table = [];
    this.size = 0;
    if(this.capacity == 0)
    {
      this.capacity = 1;
    }
    // Populate the array with empty arrays
    for(var i = 0; i < capacity; i++)
    {
      this.table.push([]);
    }
  }

  getSize()
  {
    return this.size;
  }

  isEmpty()
  {
    return size == 0;
  }

  put(key, value)
  {
   var index = (key & 0x7FFFFFFF) % this.capacity;
   var entryArray = this.table[index];
   for(var i = 0; i < entryArray.length; i++)
   {
     if(entryArray[i].key == key)
     {
       var old = entryArray[i].value;
       entryArray[i].value = value;
       return old;
     }
   }
   // If we are here then there was no existing entry in the table
   // Should check if re-hashing is required, but aren't.
   this.table[index].push(new Entry(key, value));
   this.size++;
   return undefined;
  }

  getValue(key)
  {
    var index = (key & 0x7FFFFFFF) % this.capacity;
    var entryArray = this.table[index];
    for(var i = 0; i < entryArray.length; i++)
    {
      if(entryArray[i].key == key)
      {
        return entryArray[i].value;
      }
    }
    return errorValue;
  }

  setErrorValue(errorValue)
  {
    this.errorValue = errorValue;
  }
}


/*
 Begin definition of the State Machine class 
*/
class StateMachine
{
  constructor()
  {
    var capacity = 11;
    this.hashTable = new HashTable(capacity);
  }

  getNextState(objectID, currentState, input)
  {
    var key = objectID << 20;
    key |= currentState << 10;
    key |= input;
    var nextState = this.hashTable.getValue(key);
    return nextState;
  }

  insertTransition(objectID, currentState, input, newState)
  {
    var key = objectID << 20;
    key |= currentState << 10;
    key |= input;
    this.hashTable.put(key, newState);
  }

  setErrorValue(errorValue)
  {
    this.hashTable.setErrorValue(errorValue);
  }
}


/*
 Begin definition of the state transition class 
*/
class TransitionInfo
{
/*
  constructor(transitionMethod, stateMethod, stateIndex)
  {
    this.transitionMethod = transitionMethod;
    this.stateMethod = stateMethod;
    this.stateIndex = stateIndex;
  }
*/
}


/*
 Begin definition of the Automaton class 
*/
class Automaton
{
  constructor()
  {
    var errorValue = new TransitionInfo();
    errorValue.stateIndex = -1;
    this.stateMachine = new StateMachine();
    this.stateMachine.setErrorValue(errorValue);
  }

  initAutomaton()
  {
  }

  executeCurrentState()
  {
    this.currentInfo.stateMethod();
  }

  processInput(input)
  {
    var newStateInfo = this.stateMachine.getNextState(0, this.currentInfo.stateIndex, input);
    this.previousInfo = this.currentInfo;
    this.currentInfo = newStateInfo;
    this.currentInfo.transitionMethod();
  }

  setInitialStateInfo(initialStateInfo)
  {
    this.initialStateInfo = initialStateInfo;
  }
}

var hashTable = new HashTable(1, 2);
print(hashTable.put(1, 2));
print(hashTable.put(1, 3));
print(hashTable.put(2, 5));

print('get test');
print(hashTable.getValue(1));
print(hashTable.getValue(2));

var stateMachine = new StateMachine();
stateMachine.insertTransition(0, 0, 1);
stateMachine.insertTransition(0, 0, 2);
stateMachine.insertTransition(0, 1, 0);
stateMachine.insertTransition(0, 1, 1);
stateMachine.insertTransition(1, 0, 0);
stateMachine.insertTransition(1, 0, 1);
stateMachine.insertTransition(1, 1, 0);
stateMachine.insertTransition(1, 1, 1);

//var automaton = new Automaton();

class SplashScreen extends Automaton
{
  constructor()
  {
    super();
    // State indexes
    this.FADE_IN = 2;
    this.FADE_OUT = 3;
    this.FINISHED = 5;
    this.IDLE = 4;
    this.START_STATE = 1;

    // Input indexes
    this.FadeInComplete = 2;
    this.FadeOutComplete = 3;
    this.StartFadeIn = 1;
    this.StartFadeOut = 4;
  }

  // State methods
  AZ_FADE_IN()
  {
    print("AZ_FADE_IN");
  }

  AZ_FADE_OUT()
  {
    print("AZ_FADE_OUT");
  }

  AZ_FINISHED()
  {
    print("AZ_FINISHED");
  }

  AZ_IDLE()
  {
    print("AZ_IDLE");
  }

  AZ_START_STATE()
  {
    print("AZ_START_STATE");
  }

  // Transition methods
  AZ_START_STATE_ON_StartFadeIn()
  {
    print("AZ_START_STATE_ON_StartFadeIn");
  }

  AZ_FADE_IN_ON_FadeInComplete()
  {
    print("AZ_FADE_IN_ON_FadeInoComplete");
  }

  AZ_FADE_OUT_ON_FadeOutComplete()
  {
    print("AZ_FADE_OUT_ON_FadeOutComplete");
  }

  AZ_IDLE_ON_StartFadeOut()
  {
    print("AZ_IDLE_ON_StartFadeOut");
  }

  initAutomaton()
  {
    {
      var info = new TransitionInfo();
      info.transitionMethod = this.AZ_START_STATE_ON_StartFadeIn;
      info.stateMethod = this.AZ_FADE_IN;
      info.stateIndex = this.FADE_IN;
      this.stateMachine.insertTransition(0, this.START_STATE, this.StartFadeIn, info);
    }

    {
      var info = new TransitionInfo();
      info.transitionMethod = this.AZ_FADE_IN_ON_FadeInComplete;
      info.stateMethod = this.AZ_IDLE;
      info.stateIndex = this.IDLE;
      this.stateMachine.insertTransition(0, this.FADE_IN, this.FadeInComplete, info);
    }
    
    {
      var info = new TransitionInfo();
      info.transitionMethod = this.AZ_FADE_OUT_ON_FadeOutComplete;
      info.stateMethod = this.AZ_FINISHED;
      info.stateIndex = this.FINISHED;
      this.stateMachine.insertTransition(0, this.FADE_OUT, this.FadeOutComplete, info);
    }

    {
      var info = new TransitionInfo();
      info.transitionMethod = this.AZ_IDLE_ON_StartFadeOut;
      info.stateMethod = this.AZ_FADE_OUT;
      info.stateIndex = this.FADE_OUT;
      this.stateMachine.insertTransition(0, this.IDLE, this.StartFadeOut, info);
    }

    {
      var info = new TransitionInfo();
      info.transitionMethod = undefined;
      info.stateMethod = this.AZ_START_STATE;
      info.stateIndex = this.START_STATE;
      this.currentInfo = info;
      this.previousInfo = info;
      this.setInitialStateInfo(info);
    }
  }
}


var splashScreen = new SplashScreen();
splashScreen.initAutomaton();
splashScreen.executeCurrentState();
splashScreen.processInput(splashScreen.StartFadeIn);
splashScreen.executeCurrentState();
splashScreen.processInput(splashScreen.FadeInComplete);
splashScreen.executeCurrentState();
splashScreen.processInput(splashScreen.StartFadeOut);
splashScreen.executeCurrentState();
splashScreen.processInput(splashScreen.FadeOutComplete);
splashScreen.executeCurrentState();
