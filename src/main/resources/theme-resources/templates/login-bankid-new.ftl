<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Alf Login</title>
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <meta name="description" content="" />
  <link href="${url.resourcesPath}/css/spin.css" rel="stylesheet" />
  <script type="text/javascript" src="${url.resourcesPath}/js/spin.umd.js"></script>
   <script src="${url.resourcesPath}/js/bankid.js" type="text/javascript"></script>
   
   <script>
	var count = 10;
    var opts = {
      lines: 16, // The number of lines to draw
      length: 26, // The length of each line
      width: 13, // The line thickness
      radius: 35, // The radius of the inner circle
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
     function handleClick(){
            console.log('Logging in...')
            location.href = "bankid:///?autostarttoken=${autoStartToken}&redirect=null";
        }
	poll(
	    function() {
	    
	    	const req = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
	    	const url='collect?bankidref=${bankidref}';
	    	req.open("GET", url, false);
	    	req.send();
	    	
	    	if(req.status==200) {
	    	    var response = JSON.parse(req.responseText);
	    	    if ( response.status == 'complete' ) {
    				return true;
				} else if ( response.status == 'pending' 
					&& (response.hintCode == 'started' || response.hintCode == 'userSign')  ) {
					let qrcode = document.getElementById('qrcode');
					if ( qrcode !== null ) {
					  qrcode.style.display = 'none';
					}
					let target = document.getElementById('progress-loader');
					let loading = document.getElementById('loading').style.display = 'flex';
    				if ( target !== null && loading !== null) {
						let spinner = new Spin.Spinner(opts).spin(target);
    					document.getElementById('loading').style.display = 'flex';
					}
				}	
    		}
    		else if( req.status!=200 ) {
    			redirectToError(JSON.parse(req.responseText).hintCode);	
    		}
	        return false;
	    },
	    function() {
	        // Done, success callback
        	redirectToDone('${bankidref}', '${state}');
	    },
	    function() {
	        // Error, failure callback
	        redirectToError('expiredTransaction')
	    },
	    120000, // timeout
	    2000 // interval
	);
	 
</script>
</head>
<body>
    <section class="section-header">
        <img src="${url.resourcesPath}/img/bankid-original.png" class="header-icon" alt="door-icon"></img>
        <h1 class="header-text">Logga in</h1>
    </section>
    <section class="section-body" id="qrcode">
        <span class="text-normal">Scan QR Code below</span>
        <img class="qr-img"  alt="mock-qr" id="qrcode-img" src="qrcode?bankidref=${bankidref}" onload="pollQrCode()"/>
        
    </section>
   
 <div id="loading" style="box-sizing: border-box; display: none; align-items: stretch; flex-direction: column; flex-shrink: 0; border-style: solid; border-width: 0px; position: relative; z-index: 0; min-height: 0px; min-width: 0px; margin-bottom: 40px; width: 100%;">
			<p style="max-width: 100%; color: rgb(23, 23, 23); font-size: 19px; font-family: Helvetica , Arial, sans-serif; font-weight: 400; line-height: 30px; text-align: center; margin-bottom: 0px; margin-top: 0px; padding-bottom: 1px; padding-top: 4px; text-rendering: geometricprecision; -moz-text-size-adjust: none;">${msg("bankid.login.text3")}</p>
			<div style="flex: 1 1 0%; text-align: center;">
				<div style="height: 200px;" id="progress-loader"></div>
			</div>
		</div>
   
    <section class="section-footer">
        <span class="text-normal">Or</span>
        <Button type="button" onclick="handleClick()" class="bankId-login-button">
            <span class="sign-in-btn-text">Open BankId on this device</span>
        </Button>
        <Button class="cancel-btn" type="button">
            <span class="text-normal">cancel</span> 
        </Button>
    </section>
     <input id="bankidref" name="bankidref" autocorrect="off" autocomplete="off" type="hidden" value="${bankidref}" />
</body>
</html>