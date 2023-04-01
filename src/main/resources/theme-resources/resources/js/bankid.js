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

function redirectToDone(bankidref, state) {
	window.location.href = "done?bankidref=" + bankidref + "&state=" + state;
}

function redirectToError(errorCode) {
	window.location.href = "error?code=" + errorCode;
}

function redirectToCancel(errorCode, bankidref) {
	window.location.href = "cancel?bankidref=" + bankidref;
}

/* For login form */
function validateForm() {
	let success = true;
	let nin = document.getElementById('nin');
	document.getElementById('error').style.display="none"; 
	
	if (nin == null) {
		document.getElementById('error').style.display="inline"; 
		return false;
	}
	
	if (!/^[0-9]+$/.test(nin.value)) {
		document.getElementById('error').style.display="inline"; 
		success = false;
	}
	
	if (nin.value.length != 12) {
		document.getElementById('error').style.display="inline"; 
		success = false;
	}
	
	if (nin.value.charAt(0) !='1' && nin.value.charAt(0) !='2') {
		document.getElementById('error').style.display="inline"; 
		success = false;
	}
	return success;
}
