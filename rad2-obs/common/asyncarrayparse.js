module.exports = function (array, fn, callback, chunk) {
    var index = 0;
    // If chunk size is not given, then run the function synchrnously
    chunk = chunk || array.length;
    function doChunk() {
        var cnt = chunk;
        while (cnt-- && index < array.length) {
            // callback called with args (value, index, array)
            fn(array[index]);
            ++index;
        }
        if (index < array.length) {
            // set Timeout for async iteration
            setImmediate(doChunk);
        } else if(index == array.length) {
        	callback();
        }
    }    

    doChunk();    
}