
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
  this.value = value;
};

var entry = new Entry(1, 2, 3, 4);
print(entry.hash);
print(entry.key);
print(entry.getKey());
print(entry.value);
print(entry.getValue());
print(entry.next);
