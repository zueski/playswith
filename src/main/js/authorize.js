'use strict';

const baseAPI = 'https://www.bungie.net/Platform';
const apiKey = '1c9274b342264dd38d42a115d1493479';
const origin = 'https://playswith.tomogara.org';

function authorize(authCode, state)
{
	var authReq = new XMLHttpRequest();
	authReq.open('POST', baseAPI + '/App/GetAccessTokensFromCode/', true);
	authReq.setRequestHeader('Accept','application/json');
	authReq.setRequestHeader('X-API-Key', apiKey);
	authReq.setRequestHeader('Content-Type','application/json; charset=UTF-8;');
	authReq.onload = function() {
		if(authReq.readyState == authReq.DONE)
		{
			if(authReq.status == 200)
			{
				var resp = JSON.parse(authReq.responseText);
				if(resp && resp.ErrorCode == 1)
				{
					var accessToken = resp.Response.accessToken;
					var now = new Date().getTime();
					accessToken.expiresAt = now + (accessToken.expires * 1000);
					localStorage.setItem("accessToken", JSON.stringify(accessToken));
					var refreshToken = resp.Response.refreshToken;
					refreshToken.readyAt = now + (refreshToken.readyin * 1000);
					refreshToken.expiresAt = now + (refreshToken.expires * 1000);
					localStorage.setItem("refreshToken", JSON.stringify(refreshToken));
					refreshAuth();
				} else {
					console.log(authReq.response);
					console.log(authReq.responseText);
				}
			}
		}
	}
	authReq.send('{"code":"' + authCode + '"}');
	sessionStorage.setItem('account', JSON.parse(window.atob(state)));
	localStorage.setItem('authState', window.atob(state));
}

function refreshAuth()
{
	var refreshToken = JSON.parse(localStorage.getItem('refreshToken'));
	if(refreshToken && refreshToken.expiresAt > new Date().getTime())
	{
		var authReq = new XMLHttpRequest();
		authReq.open('POST', baseAPI + '/App/GetAccessTokensFromRefreshToken/', true);
		authReq.setRequestHeader('Accept','application/json');
		authReq.setRequestHeader('X-API-Key', apiKey);
		authReq.setRequestHeader('Content-Type','application/json; charset=UTF-8;');
		authReq.onload = function() {
			if(authReq.readyState == authReq.DONE)
			{
				if(authReq.status == 200)
				{
					var resp = JSON.parse(authReq.responseText);
					if(resp && resp.ErrorCode == 1)
					{
						var accessToken = resp.Response.accessToken;
						var now = new Date().getTime();
						accessToken.expiresAt = now + (accessToken.expires * 1000);
						localStorage.setItem("accessToken", JSON.stringify(accessToken));
						var refreshToken = resp.Response.refreshToken;
						refreshToken.readyAt = now + (refreshToken.readyin * 1000);
						refreshToken.expiresAt = now + (refreshToken.expires * 1000);
						localStorage.setItem("refreshToken", JSON.stringify(refreshToken));
						var authState = JSON.parse(localStorage.getItem('authState'));
						getMembership(authState.platform, authState.user);
					} else {
						console.log(authReq.response);
						console.log(authReq.responseText);
					}
				}
			}
		}
		authReq.send('{"refreshToken":"' + refreshToken.value + '"}');
	} else {
		// redirect to auth
		console.log("Refresh token expired");
	}
}

function bypasslogin()
{
	var t = localStorage.getItem("accessToken");
	if(t != null)
	{
		var accessToken = JSON.parse(t);
		if(accessToken != null && accessToken.expiresAt > new Date().getTime())
		{
			console.log("checking accessToken");
			// try the token
			var authState = JSON.parse(localStorage.getItem('authState'));
			getMembership(authState.platform, authState.user);
		} else {
			console.log("refreshing authorization");
			refreshAuth();
		}
	} else {
		console.log("no access token");
	}
	return true;
}

function getMembership(membershipType, user)
{
	var acctReq = new XMLHttpRequest();
	acctReq.open('GET', baseAPI + '/Destiny/'+membershipType+'/Stats/GetMembershipIdByDisplayName/' + user + '/', true);
	acctReq.setRequestHeader('Accept','application/json');
	acctReq.setRequestHeader('X-API-Key',apiKey);
	acctReq.onload = function() {
		if(acctReq.status == 200)
		{
			var resp = JSON.parse(acctReq.responseText);
			if(resp && resp.ErrorCode == 1)
			{
				sessionStorage.setItem('membership', JSON.stringify(resp.Response));
				localStorage.setItem(user, JSON.stringify(resp.Response));
				console.log("user " + user + ": " + sessionStorage.membership);
				window.location = '/platform/' + membershipType+'/user/'+user+'/';
			} else {
				console.log(resp.response);
				console.log(resp.responseText);
			}
		}
	}
	acctReq.send();
}

