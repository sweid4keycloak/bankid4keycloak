<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=false displayWide=false; section>
<#if section = "form">
	<link href="${url.resourcesPath}/css/spin.css" rel="stylesheet" />
	<script type="text/javascript" src="${url.resourcesPath}/js/spin.umd.js"></script>
        <script src="${url.resourcesPath}/js/bankid.js" type="text/javascript"></script>
					<div style="box-sizing: border-box; display: flex; align-items: center; flex-direction: column; flex-shrink: 0; border-style: solid; border-width: 0px; position: relative; z-index: 0; min-height: 0px; min-width: 0px;">
							<div
								style="box-sizing: border-box; display: flex; align-items: center; flex-direction: column; flex-shrink: 0; border-style: solid; border-width: 0px; position: relative; z-index: 0; min-height: 0px; min-width: 0px; justify-content: center;">
								<img alt="" src="${url.resourcesPath}/img/bankid_vector_rgb.svg" style="margin-bottom: 30px;">
								<div
									style="box-sizing: border-box; display: flex; align-items: stretch; flex-direction: column; flex-shrink: 0; border-style: solid; border-width: 0px; position: relative; z-index: 0; min-height: 0px; min-width: 0px; margin-top: 20px; width: 100%;">
									<h1
										style="max-width: 100%; color: rgb(23, 23, 23); font-family: Helvetica , Arial, sans-serif; font-weight: 700; font-size: 36px; line-height: 40px; letter-spacing: -0.2px; text-align: center; text-rendering: geometricprecision; padding-top: 3px; padding-bottom: 2px; margin-bottom: 0px; margin-top: 0px; -moz-text-size-adjust: none;">Open
										Mobile BankID, BankID on file or card</h1>
								</div>
								<div
									style="box-sizing: border-box; display: flex; align-items: stretch; flex-direction: column; flex-shrink: 0; border-style: solid; border-width: 0px; position: relative; z-index: 0; min-height: 0px; min-width: 0px; margin-bottom: 40px; width: 100%;">
									<p
										style="max-width: 100%; color: rgb(23, 23, 23); font-size: 19px; font-family: Helvetica , Arial, sans-serif; font-weight: 400; line-height: 30px; text-align: center; margin-bottom: 0px; margin-top: 0px; padding-bottom: 1px; padding-top: 4px; text-rendering: geometricprecision; -moz-text-size-adjust: none;">To
										login, start the BankID application on your mobile or
										computer.</p>
								</div>
								<div
									style="box-sizing: border-box; display: flex; align-items: stretch; flex-direction: column; flex-shrink: 0; border-style: solid; border-width: 0px; position: relative; z-index: 0; min-height: 0px; min-width: 0px; margin-bottom: 40px; width: 100%;">
									<p
										style="max-width: 100%; color: rgb(23, 23, 23); font-size: 19px; font-family: Helvetica , Arial, sans-serif; font-weight: 400; line-height: 30px; text-align: center; margin-bottom: 0px; margin-top: 0px; padding-bottom: 1px; padding-top: 4px; text-rendering: geometricprecision; -moz-text-size-adjust: none;">Confirming
										your details</p>
									<div style="flex: 1 1 0%; text-align: center;">
										<div style="height: 250px;" id="progress-loader"></div>
									</div>
								</div>
							</div>
							<form novalidate="" action="cancel">
							<button
								style="padding: 0px; margin: 0px; background-color: rgba(255, 255, 255, 0); border: medium none; cursor: pointer; outline: currentcolor none medium;">
								<div
									style="box-sizing: border-box; display: flex; align-items: stretch; flex-direction: row; flex-shrink: 0; border-style: solid; border-width: 1px; position: relative; z-index: 0; min-height: 0px; min-width: 0px; background-color: rgb(255, 255, 255); border-color: rgb(255, 255, 255); border-radius: 0px; height: 50px; justify-content: center; padding: 19px 24px; transition: background-color 0.2s ease 0s, border-color 0.2s ease 0s, color 0.2s ease 0s;">
									<div
										style="box-sizing: border-box; display: flex; align-items: stretch; flex-direction: column; flex-shrink: 0; border-style: solid; border-width: 2px; position: absolute; z-index: 1; min-height: 0px; min-width: 0px; border-radius: 0px; bottom: -5px; left: -5px; right: -5px; top: -5px; transition: border-color 0.2s ease 0s; border-color: rgba(255, 255, 255, 0);"></div>
									<div
										style="box-sizing: border-box; display: flex; align-items: stretch; flex-direction: column; flex-shrink: 0; border-style: solid; border-width: 0px; position: relative; z-index: 0; min-height: 0px; min-width: 0px; padding-top: 0px; margin-top: -5px; margin-bottom: -6px;">
										<span
											style="max-width: 100%; color: rgb(23, 23, 23); font-family: Helvetica , Arial, sans-serif; font-weight: 500; font-size: 16px; opacity: 1; line-height: 20px; transition: color 0.2s ease 0s; visibility: visible; text-rendering: geometricprecision; -moz-text-size-adjust: none;">Cancel</span>
									</div>
								</div>
							</button>
							<input type="hidden"  name="state" id="form_state" value="${state}"/> 
							</form>
						</div>
<script>
	var count = 10;
        var opts = {
          lines: 16, // The number of lines to draw
          length: 36, // The length of each line
          width: 13, // The line thickness
          radius: 45, // The radius of the inner circle
          scale: 0.85, // Scales overall size of the spinner
          corners: 1, // Corner roundness (0..1)
          color: '#848080', // CSS color or array of colors
          fadeColor: 'transparent', // CSS color or array of colors
          speed: 0.5, // Rounds per second
          rotate: 0, // The rotation offset
          animation: 'spinner-line-shrink', // The CSS animation name for the lines
          direction: 1, // 1: clockwise, -1: counterclockwise
          zIndex: 2e9, // The z-index (defaults to 2000000000)
          className: 'mySpinner', // The CSS class to assign to the spinner
          top: '51%', // Top position relative to parent
          left: '50%', // Left position relative to parent
          shadow: '0 0 1px transparent', // Box-shadow for the lines
          position: 'absolute' // Element positioning
        };
	    
        var target = document.getElementById('progress-loader');
        var spinner = new Spin.Spinner(opts).spin(target);
	
	poll(
	    function() {
	    
	    	const req = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
	    	const url='collect';
	    	req.open("GET", url, false);
	    	req.send();
	    	if(req.status==200 && JSON.parse(req.responseText).status == 'complete') {
    			return true;	
    		}
    		else if( req.status!=200 ) {
    			redirectToError(JSON.parse(req.responseText).hintCode);	
    		}
	        return false;
	    },
	    function() {
	        // Done, success callback
        	redirectToDone();
	    },
	    function() {
	        // Error, failure callback
	        redirectToError('timeout')    
	    },
	    120000, // timeout
	    2000 // interval
	);
</script>
</#if>
</@layout.registrationLayout>
