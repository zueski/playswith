'use strict';

var membership = null;
var platform = null;
var user = null;
var currchar = null;
var chars = null;
var inventory = null;
var grimoireScopre = null;

function bootstrapUser()
{
	var authState = JSON.parse(localStorage.getItem('authState'));
	if(authState === null || authState.platform === null)
	{	window.location = window.location + "?doAuth=true";  return true; }
	platform = authState.platform;
	user = authState.user
	membership = localStorage.getItem(user).replace(/"/g, "");
	if(sessionStorage.getItem('characters') == null)
	{
		console.log("getting account for " +platform+" for " + membership);
		var acctReq = new XMLHttpRequest();
		acctReq.open('GET', baseAPI + '/Destiny/'+platform+'/Account/'+membership+'/Summary/', true);
		acctReq.setRequestHeader('Accept','application/json');
		acctReq.setRequestHeader('X-API-Key',apiKey);
		acctReq.onload = function() {
			if(acctReq.status == 200)
			{
				var resp = JSON.parse(acctReq.responseText);
				if(resp && resp.ErrorCode == 1)
				{
					chars = resp.Response.data.characters;
					inventory = resp.Response.data.inventory;
					grimoireScopre = resp.Response.data.grimoireScore;
					sessionStorage.setItem('characters',JSON.stringify(resp.Response.data.characters));
					sessionStorage.setItem('inventory', JSON.stringify(resp.Response.data.inventory));
					sessionStorage.setItem('grimoireScore', JSON.stringify(resp.Response.data.grimoireScore));
					setupPage();
				} else {
					console.log(acctReq.response);
					console.log(acctReq.responseText);
				}
			}
		}
		acctReq.send();
	} else {
		console.log("reading stored session");
		chars = JSON.parse(sessionStorage.getItem('characters'));
		inventory = JSON.parse(sessionStorage.getItem('inventory'));
		grimoireScopre = JSON.parse(sessionStorage.getItem('grimoireScore'));
		setupPage();
	}
	return true;
}

function setupPage()
{
	console.log("loading page values");
	var userspan = document.getElementById("user");
	userspan.innerHTML = user;
	userspan.style = 'background-image:url(https://www.bungie.net' + chars[0].emblemPath + ')';
	
	var charselect = document.getElementById("chars");
	charselect.onchange = function() {
		var userspan = document.getElementById("user");
		var selectedchar = charselect.options[charselect.selectedIndex].id;
		for(var i = 0, len=chars.lenth; i < len; i++)
		{
			if(selectedchar == chars[i].characterBase.characterId)
			{	userspan.style.background-image = 'url(https://www.bungie.net' + chars[i].emblemPath + ')'; }
		}
	}
	for(var i = 0, len = chars.length; i < len; ++i)
	{
		var opt = document.createElement("option");
		opt.id = chars[i].characterBase.characterId;
		opt.value = chars[i].characterBase.characterId;
		opt.text = chars[i].characterBase.classType;
		charselect.appendChild(opt);
	}
	return true;
}

function getActivities()
{
	console.log("getting activites for " + chars.length + " character");
	for(var i = 0, len=chars.length; i < len; i++)
	{
		console.log("Getting char datat for " + chars[i].characterBase.characterId);
		var acctReq = new XMLHttpRequest();
		acctReq.open('GET', baseAPI + '/Destiny/Stats/ActivityHistory/'+platform+'/'+membership+'/'+chars[i].characterBase.characterId+'/?mode=none', true);
		acctReq.setRequestHeader('Accept','application/json');
		acctReq.setRequestHeader('X-API-Key',apiKey);
		acctReq.onload = function() {
			var resp = JSON.parse(acctReq.responseText);
			if(resp && resp.ErrorCode == 1)
			{
				console.log(acctReq.response);
				console.log(acctReq.responseText);
				chars = resp.Response.data.characters;
				inventory = resp.Response.data.inventory;
				grimoireScopre = resp.Response.data.grimoireScore;
				sessionStorage.setItem('characters',JSON.stringify(resp.Response.data.characters));
				sessionStorage.setItem('inventory', JSON.stringify(resp.Response.data.inventory));
				sessionStorage.setItem('grimoireScore', JSON.stringify(resp.Response.data.grimoireScore));
				setupPage();
			} else {
				console.log(acctReq.response);
				console.log(acctReq.responseText);
			}
		};
		acctReq.send();
	}
}

