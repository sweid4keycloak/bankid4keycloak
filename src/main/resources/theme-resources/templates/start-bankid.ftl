<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=false displayWide=false; section>

<#if section = "form">
    <div id="error" style="color:red; display:none;">${msg("bankid.hint.LOCAL1")}</div>
    <form style="margin-bottom: 20px; padding-bottom: 20px;" action="login" method="post" id="login" onsubmit="return validateForm();">
        <div
            style="box-sizing: border-box; display: flex; align-items: stretch; flex-direction: column; flex-shrink: 0; border-style: solid; border-width: 0px; position: relative; z-index: 0; min-height: 0px; min-width: 0px; border-radius: 0px; background: white none repeat scroll 0% 0%; border-color: transparent;">
            <div style="position: relative;">
                <div
                    style="box-sizing: border-box; display: flex; align-items: stretch; flex-direction: column; flex-shrink: 1; border-style: solid; border-width: 0px; position: relative; min-height: 0px; min-width: 0px; flex-grow: 1;">
                    <label for="nin">
                        <div
                            style="box-sizing: border-box; display: flex; align-items: stretch; flex-direction: row; flex-shrink: 0; border-style: solid; border-width: 1px; position: relative; z-index: 0; min-height: 0px; min-width: 0px; height: 70px; justify-content: space-between; padding: 15px; background-color: rgb(255, 255, 255); border-color: rgb(150, 147, 145); border-radius: 5px;">
                            <div
                                style="box-sizing: border-box; display: flex; align-items: stretch; flex-direction: column; flex-shrink: 1; border-style: solid; border-width: 0px; position: relative; z-index: 0; min-height: 0px; min-width: 0px; flex-grow: 0; top: -16px; width: 100%;">
                                <div
                                    style="box-sizing: border-box; display: flex; align-items: stretch; flex-direction: column; flex-shrink: 0; border-style: solid; border-width: 0px; position: relative; z-index: 0; min-height: 0px; min-width: 0px; background-color: rgba(255, 255, 255, 0); margin-left: 0px; pointer-events: none; padding-left: 0px; width: 100%; transform: translateY(5px);">
                                    <span
                                        style="max-width: 100%; color: rgb(120, 117, 115); font-family: Helvetica, Arial, sans-serif; font-weight: 400; font-size: 14px; line-height: 30px; overflow: hidden; text-align: left; text-overflow: ellipsis; white-space: nowrap; width: 100%; text-rendering: geometricprecision; -moz-text-size-adjust: none;">${msg("bankid.form.nin")}
									</span>
                                </div><input
                                    style="background-color: transparent; border: medium none; box-sizing: border-box; outline: currentcolor none medium; width: 100%; box-shadow: none; caret-color: rgb(72, 123, 148); color: rgb(23, 23, 23); -webkit-text-fill-color: rgb(23, 23, 23); opacity: 1; font-family: Helvetica, Arial, sans-serif; font-weight: 500; font-size: 23px; height: 70px; overflow: hidden; padding-left: 0px; padding-top: 15px; position: absolute; text-overflow: ellipsis; top: 0px; white-space: nowrap; text-rendering: geometricprecision; transform: translateY(0px);"
                                    id="nin" name="nin" autocorrect="off" autocomplete="off" type="text" inputmode="numeric"/>
                            </div>
                        </div>
                    </label></div><img alt="" src="${url.resourcesPath}/img/bankid_vector_rgb.svg" style="position: absolute; z-index: 100; right: 20px; top: 21.5px; width: 29px; height: 27px;">
            </div>
        </div>
        <div
            style="box-sizing: border-box; display: flex; align-items: stretch; flex-direction: column; flex-shrink: 0; border-style: solid; border-width: 0px; position: relative; z-index: 0; min-height: 0px; min-width: 0px; margin-top: 15px;">
            <button
                style="padding: 0px; margin: 0px; background-color: rgba(255, 255, 255, 0); border: medium none; cursor: pointer; outline: currentcolor none medium;"
                type="submit">
                <div
                    style="box-sizing: border-box; display: flex; align-items: stretch; flex-direction: row; flex-shrink: 0; border-style: solid; border-width: 1px; position: relative; z-index: 0; min-height: 0px; min-width: 0px; background-color: rgb(23, 23, 23); border-color: rgb(23, 23, 23); border-radius: 0px; height: 50px; justify-content: center; padding: 19px 24px; transition: background-color 0.2s ease 0s, border-color 0.2s ease 0s, color 0.2s ease 0s;">
                    <div
                        style="box-sizing: border-box; display: flex; align-items: stretch; flex-direction: column; flex-shrink: 0; border-style: solid; border-width: 2px; position: absolute; z-index: 1; min-height: 0px; min-width: 0px; border-radius: 0px; inset: -5px; transition: border-color 0.2s ease 0s; border-color: rgba(255, 255, 255, 0);">
                    </div>
                    <div
                        style="box-sizing: border-box; display: flex; align-items: stretch; flex-direction: column; flex-shrink: 0; border-style: solid; border-width: 0px; position: relative; z-index: 0; min-height: 0px; min-width: 0px; padding-top: 0px; margin-top: -5px; margin-bottom: -6px;">
                        <span
                            style="max-width: 100%; color: rgb(255, 255, 255); font-family: Helvetica, Arial, sans-serif; font-weight: 500; font-size: 16px; opacity: 1; line-height: 20px; transition: color 0.2s ease 0s; visibility: visible; text-rendering: geometricprecision; -moz-text-size-adjust: none;">${msg("bankid.form.submit")}
						</span>
					</div>
                </div>
            </button></div>
            <input type="hidden"  name="state" id="form_state" value="${state}"/> 
    </form>
    <script src="${url.resourcesPath}/js/bankid.js" type="text/javascript"></script>
</#if>
</@layout.registrationLayout>
