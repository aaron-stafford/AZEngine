
function Entry (hash, key, value, next)
{
  this.hash = hash;
  this.key = key;
  this.value = value;
  this.next = next;
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

var entry = new Entry(1, 2, "test", 4);
print(entry.hash);
print(entry.key);
print(entry.getKey());
print(entry.value);
print(entry.getValue());
print(entry.next);
print(entry.setValue(5));
print(entry.getValue());

function MyEntry (hash, key, value, next)
{
  this.hash = hash;
  this.key = key;
  this.value = value;
  this.next = next;
}

var myEntry = Object.create(new Entry(6, 7, "test2", 8));

print(myEntry.getKey());
