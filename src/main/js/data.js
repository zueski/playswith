'use strict';


window.indexedDB = window.indexedDB || window.mozIndexedDB || window.webkitIndexedDB || window.msIndexedDB;
 
window.IDBTransaction = window.IDBTransaction || window.webkitIDBTransaction || window.msIDBTransaction;
window.IDBKeyRange = window.IDBKeyRange || window.webkitIDBKeyRange || window.msIDBKeyRange
 
if (!window.indexedDB) { window.alert("Your browser does not support this app, required features missing (IndexDB).") }

var db = {};


function openDB()
{
	var request = indexedDB.open("playswith", 2);
	request.onerror = function(e) { console.log("error: ", e); };
	 
	request.onsuccess = function(e) {
		db = request.result;
		console.log("success: "+ db);
	};

	request.onupgradeneeded = function(event) {
		console.log ("Going to upgrade our DB from version: "+ event.oldVersion + " to " + event.newVersion);
		db = event.target.result;
		var actsummStore = db.createObjectStore("actsumm", { keyPath: "instanceId" });
		actsummStore.createIndex("referenceId", "referenceId", { unique: false });
		actsummStore.createIndex("mode", "mode", { unique: false });
	}
}

function storeActivity(activity)
{
	var transaction = db.transaction(["actsumm"], "readwrite").objectStore("actsumm");
	transaction.onsuccess = function(e) { console.log("Activities stored: " + e); };
	transaction.onerror = function(e) { console.log("Unable to store activities:" + e); }
	for(var i = 0, len = activity.length; i < len; ++i)
	{
		activity[i].instanceId = activity[i].activityDetails.instanceId;
		activity[i].referenceId = activity[i].activityDetails.referenceId;
		activity[i].mode = activity[i].activityDetails.mode;
		transaction.put(activity[i]);
	}
}
