// for embedded scripts, quoted and modified from https://github.com/swansontec/rfc4648.js by William Swanson
'use strict';
function poll(fn, callback, errback, timeout, interval) {
	    var endTime = Number(new Date()) + (timeout || 2000);
	    interval = interval || 100;

	    (function p() {
	            // If the condition is met, we're done! 
	            if(fn()) {
	                callback();
	            }
	            // If the condition isn't met but the timeout hasn't elapsed, go again
	            else if (Number(new Date()) < endTime) {
	                setTimeout(p, interval);
	            }
	            // Didn't match and too much time, reject!
	            else {
	                errback(new Error('timed out for ' + fn + ': ' + arguments));
	            }
	    })();
}

function redirectToDone() {
	window.location.href = "done?state=" + queryString.state + "&nin=" + queryString.nin;
}

