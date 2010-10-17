/**
 * Look for "var devMode =" in crest.js. If it's true, this js will
 * be executed. 
 */

$("<div>You're in dev mode!!!</div>").dialog({
	autoOpen: true,
	title:  "Dev Mode Warning",
	width: 200,
	height: 150
});
console.log("in devmode");


function debugPopup( obj, title ) {
	//popup with some chili stuff...
	var pre =  $(document.createElement('pre'));
	var code = $(document.createElement('code')).text(JSON.stringify( obj, null, "\t" )).addClass("chili-lang-javascript").chili();
	pre.append( code ).dialog({
		autoOpen: true,
		width: 600,
		height: 400,
		title: title
	});
	
}

var devHeaderHistory =	[
	"Content-Type: application/xml", 
	"Content-Type: application/json",
	"Accept: application/json", 
	"Accept: text/html", 
	"Accept: application/xml",
	"Accept: application/vnd.wap.wmlscriptc",
	"Accept: text/vnd.wap.wml",
	"Accept: application/vnd.wap.xhtml+xml",
	"Accept: application/xhtml+xml",
	"Accept: multipart/mixed",
	"Accept: */*"
];
devHeaderHistory.sort();
var devUriHistory = [
	"http://www.jonnyi.com/somehtml.html", 
	"http://localhost:8088/crest/test/pause/{pause-time}", 	
	"http://localhost:8088/crest/application.wadl", 
	"http://localhost:8088/crest/parents",
	"http://localhost:8088/crest/parents/{parent-id}",
	"http://localhost:8088/crest/parents/{parent-id}/children",
	"http://localhost:8088/crest/parents/{parent-id}/children/{child-id}",
	"http://repo1.maven.org/maven2/log4j/log4j/1.2.15/log4j-1.2.15.pom" 
];
devUriHistory.sort();
