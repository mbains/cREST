/**
 * New Features Jan 2011:
 * - can click enter now to submit a request.
 * - much better history now with saving of entire requests.
 * 
 * TODO - i need to break up this code. 
 * 	1) Create an object representing the front end which has references to frontend jQuery objects. This way 
 * 	   we don't have to write the same selectors in different places. 
 *  2) Create a Util.js and get the common junk out of here. 
 *    
 * @param method
 * @param uri
 * @param reqEntity
 * @param reqHeaders
 * @returns {AjaxContext}
 */

function Request(name,ajaxCtx) {
	this.name = name;
	this.ajaxctx = ajaxCtx;
	this.ajaxctx.xhr = null;
}

function RequestStore(name) {
	this.name = name;
		
	this.listRequests = function() {
		return storageObj(this.name).items;
	}
	
	this.listRequestNames = function() {
		return storageObj(this.name+"Items");
	}
	
	this.locateRequest = function(name) {
		var store = storageObj(this.name);
		for(var i = 0; i < store.items.length; i++ ) {
			if( store.items[i].name == name)
				return store.items[i];
		}		
	}
	
	this.storeRequest = function(request) {
		var store = storageObj(this.name);
		store.items.push(request);
		this.storeRequestNames(store);
		storageObj(this.name,store)
		
	}
	
	this.storeRequestNames = function(store) {
		var storeNames = new Array();
		for(var i = 0; i < store.items.length; i++ ) {
			storeNames.push( store.items[i].name );
		}
		storageObj(this.name+"Items",storeNames);
	}
	
	this.init = function() {
		var store = storageObj(this.name); 
		if( store == null ) {
			store = {"items":[]};
			storageObj(this.name, store );
		}

		this.storeRequestNames(store);

	}
}

function AjaxContext(method,uri,reqEntity,reqHeaders) {
	this.xhr = new XMLHttpRequest();
	this.method = method;
	this.uri = uri;
	this.reqEntity = reqEntity;
	this.reqHeaders = reqHeaders;
	this.respHeaders = null;
	this.respEntity = null;
	
	this.hasReqHeaders = function() {
		return $.isArray( this.reqHeaders ) && this.reqHeaders.length > 0;
	};
	this.hasReqEntity = function() {
		return typeof this.reqEntity == "string" && this.reqEntity.length > 0;
	};
}

function cloneAjaxCtx( ajaxCtx ) {
	var headersCopy = [];

	//dang, i forgot why i needed to create a copy of this array!
	for (var i = 0; i < ajaxCtx.reqHeaders.length; i++) {
		headersCopy.push({ name:ajaxCtx.reqHeaders[i].name,
			value:ajaxCtx.reqHeaders[i].value});
	}
	var newCtx = new AjaxContext( ajaxCtx.method, ajaxCtx.uri, ajaxCtx.reqEntity, headersCopy );
	
	return newCtx;
}
function createAjaxCtxFromUI() {
	var method = $("input[name=method_radio]:checked").val();
	var uri = uriAc.val();
	var headerText = $.trim( headersTa.textarea.val() );
	var reqHeaders = [];
	
	if( headerText != "" ) {
		
		var headerTextArray = headerText.split("\n");
	    for (var i = 0; i < headerTextArray.length; i++) {
	    	var name = headerTextArray[i].substring(0,headerTextArray[i].indexOf(":" ));
	    	var value = headerTextArray[i].substring(headerTextArray[i].indexOf(":" )+1);
	    	//TODO - I should probably add some validation logic here
	    	reqHeaders.push({ name:$.trim(name),
	    						value:$.trim(value)});
	    }
	}
	var reqEntity = "";
	if($.inArray( method,["PUT", "POST"] ) != -1) {
		reqEntity = $("textarea#put_post_entity").val();
	}
	
	var ajaxContext = new AjaxContext( method, uri, reqEntity, reqHeaders);
	
//	var ajaxContext = {
//		xhr: new XMLHttpRequest(),
//		method:method,
//		uri:uri,
//		entity:entity,
//		headers: headers
//	};
	return ajaxContext;
}

/**
 * This method is called when user clicks on the submit button for the URI intput.
 * It gathers all input from the UI including uri, http method, http request
 * headers (optional), and an entity body if the request method is PUT or POST. Once
 * gathered xhr is invoked which calls handleResponse "onreadystatechange".
 *
 */

function handleRequest(ajaxCtx) {
    try {
		ajaxCtx.xhr.onreadystatechange = (function(closuredAjaxContext) {
	    	return function() { handleResponse(closuredAjaxContext);};
	    })(ajaxCtx);
    
    	//xhr.open(method, url, async, username, password);
	    ajaxCtx.xhr.open( ajaxCtx.method, ajaxCtx.uri, true );
    	for (var i = 0; i < ajaxCtx.reqHeaders.length; i++) {
    		ajaxCtx.xhr.setRequestHeader(ajaxCtx.reqHeaders[i].name, 
    				ajaxCtx.reqHeaders[i].value);
    	}
    	
        $("div#resp_processing").css("display", "");
        $("#submit_request").button("disable");
        ajaxCtx.xhr.send(ajaxCtx.reqEntity);
    } catch(e) {
    	console.error(e);
    	alert("Error with call, hint: " + e );
    }
}

//I really need to create a chili ext for this!
function formatResponseHeaders( headers ) {
	var headersArr = null;
	
	if( $.isArray(headers) )
		headersArr = headers;
	else
		headersArr = headers.split( "\n" );
	
	var fmt = "<div class=\"respFormatting\">";
	for( var i = 0; i < headersArr.length; i++ ) {
		var name = "";
		var value = "";
		if( typeof headersArr[i] == "string" ) {
			var line = $.trim( headersArr[i] );
			if( line == "" )
				continue;
			var index = headersArr[i].indexOf(":")+1;
			//alert(line);
			name = line.substring(0,index );
			value = line.substring( index );
		} else {
			//it must be a header object then...
			name = headersArr[i].name+": ";
			value = headersArr[i].value;
		}
		fmt += "<span><span class=\"respHeaderName\">" + name + "<span class=\"respHeaderValue\">" + value + "</span><br/>";
	}
	fmt += "</div>";
	return fmt;
}

function handleResponse( ajaxContext ) {
	
	if( ajaxContext.xhr.readyState != 4 ) 
		return;
	
	var xhr = ajaxContext.xhr;
	var method = ajaxContext.method;
	var uri = ajaxContext.uri;
	var reqHeaders = ajaxContext.reqHeaders;

	//chrome on leopard needs the removeClass
	$("#submit_request").button( "enable" ).removeClass( "ui-state-hover" );
	$("div#resp_processing").css("display", "none");
	if( xhr.status == 0 ) {
		alert( "Please double check your URI, there was no response." );
		return;
	}
	if(xhr.status < 300 ) {
		var storeIt = false;
		var uriHist = storageObj("uriHistory");
		if( uriHist.length == 0 || $.inArray(uri,uriHist) == -1 ) {
			storeIt = true;
			uriHist.push(uri);
		}
		if(storeIt)
			storageObj("uriHistory",uriHist.sort());
		
		storeIt = false;
		var headerHist = storageObj("headerHistory");
		for( var i = 0; i < reqHeaders.length; i++ ) {//for each user specified header
			var header = reqHeaders[i].name + ": " + reqHeaders[i].value;//create the header string
			if( headerHist.length == 0 || $.inArray(header,headerHist) == -1 ) {//and see if we should push it
				storeIt = true;
				headerHist.push( header );
			}
		}
		if(storeIt)
			storageObj("headerHistory",headerHist.sort());
	}
		
	
	var responsesHeader = $("div#main_response_toolbar");
	if( responsesHeader.css("display") == "none" )
		responsesHeader.slideToggle("fast");

//alert( JSON.stringify(newCtx) );
	//create new response div and setup buttons & such
	var newResp = $("div#response_cloner").clone();
	newResp.attr("id","response");
	
	$("#responses").prepend( newResp );
	
	
	newResp.find( "div#title" ).html( xhr.status + " " + method + " " + uri );
	
	var lock = newResp.find("button#lock").button({
		text: false,
		icons: {
			label:"lock",
			primary: "ui-icon-unlocked"
		}
	}).click( function(event) {
		var options;
		
		if( $(this).text() == "lock" ) {
			options = {
				label:"unlock",
				icons: {
					primary: "ui-icon-locked"
				}
			}
		} else {
			options = {
				label:"lock",
				icons: {
					primary: "ui-icon-unlocked"
				}
			}
		}
		$(this).button("option",options);
		return false;
	});
	
	newResp.find('button#expand_collapse_response').button({
		text: false,
		icons: {
			primary: "ui-icon-minus"
		}
	}).click( function(event) {
			var moreInfoButton = $(this).parent().find("button#more");
			var header = $(this).parents("h3#responseHeader");
			var options;
			
			if( $(this).text() == "collapse" ) {
				moreInfoButton.button("disable");
				header.removeClass("ui-corner-top");
				header.addClass("ui-corner-all");
				//header.attr("state", "closed");
				options = {
					label:"expand",
					icons: { 
						primary: "ui-icon-plus"
					}	
				};
			} else {
				moreInfoButton.button( "enable" );
				header.removeClass("ui-corner-all");
				header.addClass("ui-corner-top");
				//header.attr("state", "open");
				options = {
						label:"collapse",
						icons: { 
							primary: "ui-icon-minus"
						}	
					};
			}
			header.next().slideToggle("fast");
			//alert( JSON.stringify(options) );
			$(this).button("option", options);
			return false;
		}
	);
	newResp.find("button#replay").button({
		text: false,
		icons: {
			primary: 'ui-icon-play'
		}
	}).click( function(event) {
			var newCtx = cloneAjaxCtx(ajaxContext);
			handleRequest( newCtx );
		}	
	);
	newResp.find("button#trashResponse").button({
		text: false,
		icons: {
			primary: 'ui-icon-trash'
		}
	}).click( function(event) {
			event.stopImmediatePropagation();
			var locked = $(this).siblings("button#lock").text() == "unlock";
			if( locked ) {
				alert( "This response is locked. Unlock it if you'd like to trash it." );
			} else {
				var respDiv = $(this).parents("div#response");
				respDiv.remove();
			}
		}	
	);
	newResp.find('button#newwinResponse').button({
		text: false,
		icons: {
			primary: 'ui-icon-newwin'
		}
	}).click( function(event) {
		$(this).parents("div#response").find("div#responseDataDiv").clone().dialog({
			autoOpen: true,
			width: 600,
			height: 400,
			title:  $(this).parents("div#response").find("div#title").text()
		});
	});
	

	 
	//headers response
	//now let's gather the request info for the response formatResponseHeaders( xhr.getAllResponseHeaders() )
	var hasMoreInfo = ajaxContext.hasReqHeaders() || ajaxContext.hasReqEntity();
	if( hasMoreInfo ) {
		newResp.find('button#more').button({
			text: false,
			icons: {
				primary: 'ui-icon-zoomin'
			}
		}).click( function(event) {
				var moreInfo = $(this).parents("div#response").find("div#moreInfoDiv");
				var options;
				
				if( $(this).text() == "more info" ) {
					
//					var expColButton = $(this).parent().find("button#expand_collapse_response");
//					
//					
//					if( expColButton.text() == "expand" ) {
//						expColButton.trigger("click");	
//					}
					
					options = {
						label:"less info",
						icons: { 
							primary: "ui-icon-zoomout"
						}	
					};
				} else {
					options = {
							label:"more info",
							icons: { 
								primary: "ui-icon-zoomin"
							}	
					};
				}
				moreInfo.slideToggle("fast");
				$(this).button("option", options);
				//	return false;
			}
		).css("display", "");
		if( ajaxContext.hasReqHeaders() ) {
			newResp.find( "pre#reqHeadersPre" ).css("display", "");
			newResp.find( "code#reqHeadersCode" ).html( formatResponseHeaders( reqHeaders ) );
		}
		if( ajaxContext.hasReqEntity() ) {
			newResp.find( "pre#reqEntityPre" ).css("display", "");
			newResp.find( "code#reqEntityCode" ).html( htmlify( ajaxContext.reqEntity ) );
		}
		 
		newResp.find('button#save_request_scenario').button({
				text: false,
				icons: {
					primary: 'ui-icon-disk'
				}
				}).click(
				function(event) {
					var saveInput = $("div#save-request-input-cloner").clone();
					saveInput.attr("id","new-save-request-input-cloner");//ensures we only do things with the clone
					var saveButton = $(this);


					//saveInput.find("input#save-request-input-name").val(method+" "+uri);
					saveInput.find("input#save-request-input-name").val("Name me");
					$(saveInput).dialog(
							{
								autoOpen: true,
								/*modal: true,*/
								width: 600,
								height:130,
								/*title: "Name this request",*/
								buttons: { "Save" :
											   function() {
													console.log( "save on dialog clicked, disable." );
													saveButton.button("disable");
													
													
													var req = new Request( 
															saveInput.find("input#save-request-input-name").val(),
															ajaxContext);
													requestStore.storeRequest(req);
													
//													var clone = cloneAjaxCtx(ajaxContext);
//													clone.xhr = null;
//													var reqSuitcase = storageObj("reqSuitcase");
//													reqSuitcase.items.push({
//															"name":saveInput.find("input#save-request-input-name").val(),
//															"ajaxctx":clone
//													});
//													
//													storageObj("reqSuitcase", reqSuitcase);

													saveInput.dialog("close");
											   },
											"Cancel":function() {
												saveInput.dialog("close");
											}
										 }
							}					
					);
									
					saveInput.siblings(".ui-dialog-titlebar").hide();

					
				}).css("display", "");
	}//done with more info code. 
	
	//do the select buttons.
	newResp.find("div#reqSelectButtons").buttonset();
	newResp.find("button#selectReqHeaders").button().click(
			function() {
				newResp.find( "pre#reqHeadersPre" ).selectText();
			}
	);
	newResp.find("button#selectReqEntity").button().click(
			function() {
				newResp.find( "pre#reqEntityPre" ).selectText();
			}
	);
	
	newResp.find("div#respSelectButtons").buttonset();
	newResp.find("button#selectRespHeaders").button().click(
			function() {
				newResp.find( "pre#respHeadersPre" ).selectText();
			}
	);
	newResp.find("button#selectRespEntity").button().click(
			function() {
				newResp.find( "pre#respEntityPre" ).selectText();
			}
	);
	
	//now let's handle the actual response...
	newResp.find( "code#respHeadersCode" ).html( formatResponseHeaders( xhr.getAllResponseHeaders() ) );
	
	var contentType = xhr.getResponseHeader("Content-Type");
	var entity = xhr.responseText;
    var fmt;
    var chiliClass;
    if(entity != null && entity != "" ) {
    	
    	var entityCode = newResp.find( "code#respEntityCode" );
    	
        try {
    		if (contentType.indexOf("json") != -1) {
    			fmt = JSON.stringify(JSON.parse(entity), null, "\t");
    			chiliClass = "chili-lang-javascript";
    		} else if (contentType.indexOf("xml") != -1) {
    			fmt = formatXml(entity);
    			chiliClass = "chili-lang-xml";
    		} else if (contentType.indexOf("html") != -1) {
    			fmt = entity;
    			chiliClass = "chili-lang-html";
    		} else {
    			fmt = entity;
    		}
    		fmt = htmlify( $.trim( fmt ) );
    		if( typeof chiliClass !== "undefined" ) {
    			entityCode.addClass( chiliClass ).html( fmt ).chili();
    		} else {
    			entityCode.html( fmt );
    		}
    		//newResp.find( "code#respEntityCode" ).addClass( chiliClass ).html( fmt ).chili();
        } catch (e) {
    		console.error( "There was an error trying to format your response entity of content type \"" + contentType + 
    				"\" for response entity \"" + xhr.responseText + "\". Perhaps the server specified a Content-Type doesn't match " +
    				"the response entity? Or, maybe I have a bug!?");
    		entityCode.html( htmlify( $.trim( entity ) ) );
    	}    	
    } else {
    	newResp.find( "code#respEntityCode,#entityHeader" ).css("display", "none");
    }
    newResp.slideToggle("fast");
	
}//end handleResponse

function htmlify( str ) {
	return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function awesomeBar( items, request, response) {
	var terms = $.trim( request.term ).split( " " );
	var filteredItems = 
		$.map(items, function(item) {
			if( item.label )
				var label = item.label;
			else 
				var label = item;//must be a string
			
//			if(! item.label ) {//item must be a string so make it an object.
//				item = { label: item, value: item };
//			}
			
		
			for( var i = 0; i < terms.length; i++ ) {
				if( label.toLowerCase().indexOf( terms[i].toLowerCase() ) == -1  )
					return;
			}
			//this could return a string or a { label: "label", value: "val" }
			return item;
			
		});
		
		//causes suggestions not to show because what the user typed already 
		//matches the single suggestion left, so why show?
		if( filteredItems.length == 1 && filteredItems[0] == uriAc.val() )
			filteredItems = new Array();
		
		response(filteredItems);
}
/**
 * I got this code from... 
 * 
 * http://stackoverflow.com/questions/376373/pretty-printing-xml-with-javascript
 * 
 * ...the updated version posted by "Dan BROOKS"
 * 
 * @param xml
 * @returns {String}
 */
function formatXml(xml) {
    var reg = /(>)(<)(\/*)/g;
    var wsexp = / *(.*) +\n/g;
    var contexp = /(<.+>)(.+\n)/g;
    xml = xml.replace(reg, '$1\n$2$3').replace(wsexp, '$1\n').replace(contexp, '$1\n$2');
    var pad = 0;
    var formatted = '';
    var lines = xml.split('\n');
    var indent = 0;
    var lastType = 'other';
    // 4 types of tags - single, closing, opening, other (text, doctype, comment) - 4*4 = 16 transitions 
    var transitions = {
        'single->single': 0,
        'single->closing': -1,
        'single->opening': 0,
        'single->other': 0,
        'closing->single': 0,
        'closing->closing': -1,
        'closing->opening': 0,
        'closing->other': 0,
        'opening->single': 1,
        'opening->closing': 0,
        'opening->opening': 1,
        'opening->other': 1,
        'other->single': 0,
        'other->closing': -1,
        'other->opening': 0,
        'other->other': 0
    };

    for (var i = 0; i < lines.length; i++) {
        var ln = lines[i];
        var single = Boolean(ln.match(/<.+\/>/)); // is this line a single tag? ex. <br />
        var closing = Boolean(ln.match(/<\/.+>/)); // is this a closing tag? ex. </a>
        var opening = Boolean(ln.match(/<[^!].*>/)); // is this even a tag (that's not <!something>)
        var type = single ? 'single' : closing ? 'closing' : opening ? 'opening' : 'other';
        var fromTo = lastType + '->' + type;
        lastType = type;
        var padding = '';

        indent += transitions[fromTo];
        for (var j = 0; j < indent; j++) {
            padding += '\t';
        }
        if (fromTo == 'opening->closing')
            formatted = formatted.substr(0, formatted.length - 1) + ln + '\n'; // substr removes line break (\n) from prev loop
        else
            formatted += padding + ln + '\n';
    }

    return formatted;
}
/**
 * gets or saves an object from/to storage. 
 */
function storageObj( name, value ) {
	if( typeof value === "undefined" ) {
		//console.log( "storageObj("+name+","+value+")" );
		return JSON.parse( storage(name) );
	}
		
		//return eval(storage(name));
	else
		storage(name,JSON.stringify(value));
}

/**
 * gets or saves a string from/to storage. 
 */
function storage( name, value ) {
	if( typeof value === "undefined" )//51oojs
		return localStorage.getItem( name );
	try {
		 localStorage.setItem(name, value); //saves to the database, "key", "value"
	} catch (e) {
		 if (e == QUOTA_EXCEEDED_ERR) {
		 	 alert("Sorry, local storage has exceeded it's quota!");
		}
	}
}
function selectedText() {
	var selected = "";
	if (window.getSelection) {
		selected = window.getSelection();
	} else if (document.getSelection) {
		selected = document.getSelection();
	} else if (document.selection) {
		selected = document.selection.createRange().text;
	} else {
		return selected;
	}
	return selected;
}

function editHistory( id, title ) {
	var histDialog = $("#edit-div-history-cloner").clone().attr("id", id );
	var contents = histDialog.find("#history");
	var hist = storageObj(id);
	var histString = "";
	for( var i = 0; i < hist.length; i++ ) {
		histString += hist[i]+"\n";
	}
	//alert(histString);
	contents.val(histString);
	histDialog.dialog({
		autoOpen: true,
		modal: true,
		width: 700,
		height: 500,
		title: title,
		buttons: { "Save" :
					   //todo, refactor so header history can reuse some of this code
					   //do some clean up like ensuring that a real URI is entered and
					   //that no duplicates are stored
					   function() {
							//not sure why i have to do $(contents).text() and not just
							//contents.text() like above where i set it. w/out the $ the
							//the .text() call is empty
							var newText = contents.val();
							var newTextArray = newText.split("\n");
							//alert(hist);
							var newHistory = $.map( newTextArray, function(item) {
								item = $.trim(item);
								//should probably check for valid HTTP uri.. will do later
								if( item != "")
									return item;
							});
							
							storageObj(id, newHistory.sort());
							histDialog.dialog("close");
					   },
					"Cancel":function() {histDialog.dialog("close");}
				 }
	});
}

function loadSavedRequestInUI( name ) {

	var suitcaseItem = requestStore.locateRequest(name);
	
//	var reqSuitcase = storageObj("reqSuitcase");
//	
//	for(var i = 0; i < reqSuitcase.items.length; i++ ) {
//		if( reqSuitcase.items[i].name == name ) {
//			var suitcaseItem = reqSuitcase.items[i]; 
//			break;
//		}
//	}
	
	
	
	console.log( "loadSavedRequestInUI item: " + suitcaseItem + " with name: '" +name+ "'" );
	var ajaxCtx = suitcaseItem.ajaxctx;

	
	//do put/post entity stuff.
	if( ajaxCtx.reqEntity && $.inArray( ajaxCtx.method,["PUT", "POST"] ) != -1 ) {
		$("textarea#put_post_entity").val(ajaxCtx.reqEntity);
	}

	//do method button stuff
	var methods = $("input[name=method_radio]");

	for(var i = 0; i < methods.length; i++) {
		if( methods[i].value == ajaxCtx.method) {
			console.log( methods[i].value + " == " + ajaxCtx.method );
			$(methods[i]).button().trigger("click");
			$(methods[i]).button().trigger("change");
			$(methods[i]).button("refresh");
			break;
		}
	}
	//alert( "setting uri " + ajaxCtx.uri );
	uriAc.val(ajaxCtx.uri);
	

	
	//TODO - test when there's no headers.
	//do header stuff
	var headers = "";
	if( ajaxCtx.reqHeaders.length > 0 ) {
		for(var i = 0; i < ajaxCtx.reqHeaders.length; i++) {
			headers = headers+ajaxCtx.reqHeaders[i].name+": "+ajaxCtx.reqHeaders[i].value+"\n";
		}
		headersTa.textarea.val(headers);
		
		//only toggle (click) if it's not already open
		console.log($("input[id=modify_headers]:checked"));
		if($("input[id=modify_headers]:checked").length==0) {
			$("#modify_headers").button().trigger("click");
			$("#modify_headers").button().trigger("change");
			$("#modify_headers").button("refresh");				
		}

	}
	return suitcaseItem;
}
var uriAc;
var headerAc;
var headersTa;
var putPostEntityTa
var savedReqURIOverride;
var requestStore = new RequestStore("reqSuitcase");
requestStore.init();

function init(){
	//to make things easier, let's just ensure there's always a uriHistory and
	//headerHistory even if the arrays are empty.
	if(! $.isArray(storageObj("uriHistory")) ) {
		storage("uriHistory","[]");
	}
	if(! $.isArray(storageObj("headerHistory")) ) {
		storage("headerHistory","[]");
	}
	
	//setup uri fields...
	$("div#submit_request_buttonset").buttonset();
	
	/*
	 * Sup with all the bind/unbind in the focus event? I only want to capture mouse up
	 * once per focus. So i bind a handler in focus event. To prevent
	 * multiple mouseup handlers on focus event, the mouseup handler
	 * needs to unbind itself. What i aim to do here is select all text
	 * in the text field on the initial focus only if the user has not
	 * selected text.
	 * 
	 * 09/25/10 - also adding support for {params}
	 * 
	 * 01/31/11 - updated autocomplete to include suitcase items enabling a more complex history. Suitcase itmes
	 * are just objects that remember everything about a request. We'll use 'em to prepopulate everything in
	 * the gui. 
	 * 
	 * 
	 * NOTE: see http://api.jquery.com/unbind/ and http://api.jquery.com/bind on using namespaces for events. Calling unbind("mouseup") will
	 * remove all mouseup events, but if i bound it with a namesapce - like mouseup.focus - i can remove just the one mouseup handler using
	 * its namespace
	 */
	uriAc = $("#uri_autocomplete").autocomplete( {
			source: function(req, resp){
					var uris = storageObj("uriHistory");
//					
//					var suitcaseItems = storageObj("reqSuitcase").items;
//					//console.log( suitcaseItems );
//					var names = new Array();
//					for(var i = 0; i < suitcaseItems.length; i++) {
//						names[i] = "req suitcase: " + suitcaseItems[i].name;
//					}
					
//					awesomeBar(uris.concat(storageObj("reqSuitcase").items), req, resp);
					//TODO - change this to concat listRequestNames, need update to awesome bar too.
					
					//{ label: lab, value: val };
					var names = requestStore.listRequestNames();
					var nameObjs = [];
					for(var i = 0; i < names.length; i++) {
						nameObjs[i] = { label: names[i], value: names[i], crestType:"savedReq" }
					}
					
					awesomeBar(uris.concat(nameObjs), req, resp);
			},
			select: function(event, ui){
				if(ui.item && ui.item.crestType == "savedReq") {
					console.log( "new load with crest type" );
					var suitcaseItem = loadSavedRequestInUI( ui.item.value );
					savedReqURIOverride = suitcaseItem.ajaxctx.uri;		
				}
			},
			close: function(event, ui) {
				if( savedReqURIOverride ) {
					console.log( "URI OVER RIDE" );
					$(this).val(savedReqURIOverride);
					savedReqURIOverride=null;
				}
			},
			minLength: 2} 
		).bind( "mouseup.uriparamcheck", function() {
				
				var range = $(this).getSelection();
				var val = $(this).val();
				
				if( range.start != range.end )
					return;//only apply if user clicked w/out selecting text
//				
//				var left = val.substring( 0,range.start ).lastIndexOf("{");
//				var right = val.substring( range.start ).indexOf("}");
//				if( left == -1 || right == -1 )
//					return;
//				
//				//okay, now we know we need to select a range case we have a { to the left of the cursor and a } to the right.
				var selStart = -1;
				for( i = range.start; i > -1; i-- )
					if( val.charAt(i) == "/" )
						break;
					else if( val.charAt(i) == "{" ) {
						selStart = i;
						break;
					}
				var selEnd = -1;
				for( i = range.start; i < val.length; i++ )
					if( val.charAt(i) == "/" )
						break;
					else if( val.charAt(i) == "}" ) {
						selEnd = i;
						break;
					}	
				
				if( selStart != -1 && selEnd != -1 )
					$(this).selectRange(selStart,selEnd+1);
				
				//console.log( ">>>" + JSON.stringify(range));
			}
		).focus(function(){
			$(this).bind( "mouseup.focus",function(){
				
				if( selectedText() == "" )
					$(this).select();
				$(this).unbind( "mouseup.focus" );
			});
			
		}).keypress(function(e) {
		    if(e.keyCode == 13) {
		    	handleRequest(createAjaxCtxFromUI());
		    }
		});
	
	
	$("#submit_request").button({
		text: true,
		icons: {
			//primary: 'ui-icon-play',
			//secondary: 'ui-icon-play'
		}
	}).click(
			function(){
				handleRequest(createAjaxCtxFromUI());
				return false;
			}
	  );
	$('button#edit_uri_history').button({
		text: false,
		icons: {
			primary: 'ui-icon-pencil'
		}
	}).click(function() {
		editHistory( "uriHistory", "Edit URI History" );
	});//click
	
	$("button#load_request_scenario").button({
		text: false,
		icons: {
			primary: 'ui-icon-suitcase'
		}
	}).click(function() {
		var suitcaseDiv = $("div#suitcase-dialog-cloner").clone();
		suitcaseDiv.attr("id","new-suitcase-dialog-cloner");
		
		//var reqSuitcase = storageObj("reqSuitcase");
		var names = requestStore.listRequestNames();
		
		var nameList = suitcaseDiv.find( "ol#selectable" );
		for(var i = 0; i < names.length; i++ ) {
			var li = $("<li class='ui-widget-content'>"+names[i]+"</li>");
			console.log("li");
			nameList.append(li);
			console.log(li);
			li.dblclick(function (){
				suitcaseDialog.parent().find(":button:contains('Load')").trigger("click");
			});
		}
		nameList.selectable();
		var selected;
		var suitcaseDialog = suitcaseDiv.dialog({
					autoOpen: true,
					modal: true,
					width: 600,
					height:400,
					title: "Saved Items",
					buttons: {
						"Load":function() {
							selected = $( ".ui-selected", this );
							if(selected.length<1) {
								alert("Please select a saved request.");
								return;
							}
							suitcaseDiv.dialog("close");
						},
						"Edit":function() {
							alert("edit clicked");
						},
						"Cancel":function() {
							suitcaseDiv.dialog("close");
						}
					},
					close: function(event, ui) {
						
						//had this method called on "Load" click, but all the clicks/events to populate
						//the ui didn't work properly like they do in URI input for loading suitcase item. 
						//seems to work find now when we call it after the modal dialog closes.
						if(selected && selected.length>0) {
							loadSavedRequestInUI(selected.text());
						}
							
						//suitcaseDiv.dialog("close");

					}
		});
	});//click
	
	
	
	//setup header fields...
	$("div#header-autocomplete_buttonset").buttonset();
	
	headerAc = $("#header-autocomplete").autocomplete( {
		source: function(req, resp){ 
			var alreadySelected = headersTa.textarea.val();
			var headerHist = storageObj("headerHistory");
			
			//remove items that are already selected.
			var filteredHeaderHist = $.map(headerHist, function(item) {
				if( alreadySelected.indexOf(item) != -1 )//must have already been selected
					return;
				
				return item;//not already selected.
			});
				
			awesomeBar( filteredHeaderHist, req, resp);
		
		},
		minLength: 2, 
		select: function(event,ui) {
			//put selected value in headers text area when it's selected from drop down

			//this one call is all i had before, but for some reasons if you deleted
			//the text from the text area, the append would still include the text that was deleted.
			//headersTa.textarea.append(ui.item.value).append("\n");
			//
			//headersTa.textarea.val() reflected the correct value of the textarea, but headers.text() would
			//always return the deleted text. So to fix the issue, i set the text() to the val() prior
			//to appending. I guess the append call appends to the value of text().
			
//			var headers = $("textarea#request_headers");
//			headers.text( headers.val() ).append(ui.item.value).append("\n");
			
			headersTa.textarea.text( headersTa.textarea.val() ).append(ui.item.value).append("\n");

			
			headersTa.checkExpand();
		},
		close: function(even,ui) {
			//when autocomplete drop down closes, see if the value in the autocomplete field
			//is found in the text area, if so, remove it from the autocomplete since it was
			//already added. If it's not in text area, user must have look at the drop down and
			//not selected anything (like clicking elsewhere on the page or pressing esc).
			//var headersArea = $("textarea#request_headers").val();
			
			var headersArea = headersTa.textarea.val();
			if( headersArea.indexOf($("#header-autocomplete").val()) != -1 )
				$("#header-autocomplete").val("");
		}
	}
	);

	$("#add_header").button().click(
			function(){
				//see comment for "select" event above for why i'm doing text(val()) call
				headersTa.textarea.text( headersTa.textarea.val() ).append($("#header-autocomplete").val()).append("\n");
				$("#header-autocomplete").val("");
				headersTa.checkExpand();
				return false;
			}
	  );
	
	$("button#load_header_scenario").button({
		text: false,
		icons: {
			primary: 'ui-icon-suitcase'
		}
	}).click(function() {
		alert( "Load Header Scenario clicked" );
	});//click
	
	$('button#edit_header_history').button({
		text: false,
		icons: {
			primary: 'ui-icon-pencil'
		}
	}).click(function() {
		editHistory( "headerHistory", "Edit Header History" );
	});//click
	

	//$(".crest-ui-awesome-bar")

	//JPI
	//$("#method_radioset").buttonset();
	
	$("#clear_request_builder").button().click(function() {
		var getButton = $("input#method_get").button();
		console.log( getButton );
		console.log( getButton );
		getButton.trigger("click").trigger("change").trigger("refresh");
		uriAc.val("");
		headerAc.val("");
		headersTa.textarea.val("");
		putPostEntityTa.val("");
		if($(this).is(":checked")) {
			$(this).attr('checked', false).trigger("change").trigger("refresh");
		}
	});
	
	$("#modify_headers").button().click(function() {
		$("div#modify_headers").slideToggle("fast");
	});
	
	$("#requestBuilderToolbar").buttonset();
	$("input[name=method_radio]").change(function() {
		var checked = $("input[name=method_radio]:checked").val();
		putPostDiv = $("#put_post_entity_div");
		var invisible = putPostDiv.css("display") == "none";
		if($.inArray(checked,["PUT","POST"] ) != -1 ) {//if put or post, make it visible if not already
			if(invisible)
				putPostDiv.slideToggle("fast");
		} else if( !invisible )//not put or post, so make invisible if not already
			putPostDiv.slideToggle("fast");
	});
	

	
	$('button#only-responses').button({
		text: false,
		icons: {
			primary: 'ui-icon ui-icon-carat-2-n-s'
		}
	}).click(function() {
		$("#requestBuilder").slideToggle('fast');
	});
	
	
	$('button#collapse-all').button({
		text: false,
		icons: {
			primary: 'ui-icon-minus'
		}
	}).click(function() {
		var buttons = $("button#expand_collapse_response");
		for(var i = 0; i < buttons.length; i++ ) {
			var button = $(buttons[i]);
			if( button.text() == "collapse")
				button.trigger("click");
		}
	});
	$('button#expand-all').button({
		text: false,
		icons: {
			primary: 'ui-icon-plus'
		}
	}).click(function() {
		var buttons = $("button#expand_collapse_response");
		for(var i = 0; i < buttons.length; i++ ) {
			var button = $(buttons[i]);
			if( button.text() == "expand")
				button.trigger("click");
		}
	});
	
	$('button#trash-all').button({
		text: false,
		icons: {
			primary: 'ui-icon-trash'
		}
	}).click(function() {
		//$("div#response").remove();
		//$($("div#response")[0]).find("button#lock")
		
		var responses = $("div#response");
		for( var i = 0; i < responses.length; i++ ) {
			var response = $(responses[i]);
			if( response.find("button#lock").text() == "lock" )
				response.remove();
			
		}
	});
	
	headersTa = $("textarea#request_headers").autogrow();
	putPostEntityTa = $("textarea#put_post_entity");
}

function toUniqueArray(a){
	a.sort();
	for(var i = 1;i < a.length;){
		if(a[i-1] == a[i]){
			a.splice(i, 1);
		} else {
			i++; 
		}
	}
	return a;
}

var devMode = false;
$(window).load(function() {
	init();
	if(devMode) {
		//$.getScript("js/devMode.js"); - uses async so other stuff that depends on it fails!
		$.ajax({
			  url: "js/devMode.js",
			  dataType: "script",
			  async: false
			});
		
		var uriHistory = storageObj("uriHistory");
		var headerHistory = storageObj("headerHistory");
		
		
		uriHistory = uriHistory.concat(devUriHistory);
		headerHistory = headerHistory.concat(devHeaderHistory);
		
		
		toUniqueArray( uriHistory );
		toUniqueArray( headerHistory );
		
		
		storageObj("uriHistory",uriHistory);
		storageObj("headerHistory",headerHistory);
	}
});

//http://stackoverflow.com/questions/985272/jquery-selecting-text-in-an-element-akin-to-highlighting-with-your-mouse
jQuery.fn.selectText = function() {
    var obj = this[0];
    if ($.browser.msie) {
        var range = obj.offsetParent.createTextRange();
        range.moveToElementText(obj);
        range.select();
    } else if ($.browser.mozilla || $.browser.opera) {
        var selection = obj.ownerDocument.defaultView.getSelection();
        var range = obj.ownerDocument.createRange();
        range.selectNodeContents(obj);
        selection.removeAllRanges();
        selection.addRange(range);
    } else if ($.browser.safari) {
        var selection = obj.ownerDocument.defaultView.getSelection();
        selection.setBaseAndExtent(obj, 0, obj, 1);
    }
    return this;
}

//http://stackoverflow.com/questions/2010892/storing-objects-in-html5-localstorage
//Storage.prototype.setObject = function(key, value) {
//    this.setItem(key, JSON.stringify(value));
//}
//
//Storage.prototype.getObject = function(key) {
//    return this.getItem(key) && JSON.parse(this.getItem(key));
//}