/*
 Begin definition of the hash table Entry class
*/
function Entry(key, value)
{
  this.key = key;
  this.value = value;
}
 
Entry.prototype.getKey = function() {
  return this.key;
};

Entry.prototype.getValue = function() {
  return this.value;
};

Entry.prototype.setValue = function(value) {
  var oldValue = this.value;
  this.value = value;
  return oldValue;
};


/*
 Begin definition of the HashTable class
*/
function HashTable(capacity)
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
};

HashTable.prototype.getSize = function() {
  return this.size;
};

HashTable.prototype.isEmpty = function() {
  return size == 0;
};

HashTable.prototype.put = function(key, value)
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

HashTable.prototype.get = function(key)
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
  return undefined;
};


/*
 Begin definition of the State Machine class 
*/
function StateMachine()
{
  var capacity = 11;
  this.hashTable = new HashTable(capacity);
};

StateMachine.prototype.getNextState = function(objectID, currentState, input)
{
  var key = objectID << 20;
  key |= currentState << 10;
  key |= input;
  var nextState = this.hashTable.get(key);
  return nextState;
};

StateMachine.prototype.insertTransition = function(objectID, currentState, input, newState)
{
  var key = objectID << 20;
  key |= currentState << 10;
  key |= input;
  this.hashTable.put(key, newState);
print("key value on insert = " + key);
};

var hashTable = new HashTable(1, 2);
print(hashTable.put(1, 2));
print(hashTable.put(1, 3));
print(hashTable.put(2, 5));

print('get test');
print(hashTable.get(1));
print(hashTable.get(2));

var stateMachine = new StateMachine();
stateMachine.insertTransition(0, 0, 1);
stateMachine.insertTransition(0, 0, 2);
stateMachine.insertTransition(0, 1, 0);
stateMachine.insertTransition(0, 1, 1);
stateMachine.insertTransition(1, 0, 0);
stateMachine.insertTransition(1, 0, 1);
stateMachine.insertTransition(1, 1, 0);
stateMachine.insertTransition(1, 1, 1);



