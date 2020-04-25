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
	window.location.href = "done?state=" + getStateValue();
}

function redirectToError(errorCode) {
	window.location.href = "error?state=" +  getStateValue() + "&code=" + errorCode;
}

function redirectToCancel(errorCode) {
	window.location.href = "cancel?state=" +  getStateValue();
}

function getStateValue() {
	 return document.getElementById("form_state").getAttribute("value");
}


/* For login form */
const errorElement = document.getElementById('error')
const nin = document.getElementById('nin')

function validateForm() {
	let messages = [];
	if (nin == null) {
		messages.push('Something is really really wrong');
		errorElement.innerText = messages.join(', ')
		return false;
	}
		
	if (nin.value === '') {
		messages.push('You must type something');
	}
	
	if (nin.value.length < 10) {
		messages.push('At least 10 characters');
	}
	
	if (nin.value.length > 13) {
		messages.push('No more than 13 characters');
	}
	
	if (messages.length > 0) {
		errorElement.innerText = messages.join('\n')
		return false;
	}
}
