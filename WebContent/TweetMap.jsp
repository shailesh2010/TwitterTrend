<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="Model.TweetModel"%>
<%@page import="Model.DatabaseLayer"%>
<%@page import="java.util.ArrayList"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Twitter On the Map</title>
<script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?sensor=false&libraries=visualization"></script>
<script type="text/javascript" src="markerclusterer.js"></script>
<script src="http://code.jquery.com/jquery-latest.min.js"></script>
<script type="text/javascript">

	var markerCluster = null;
	var heatmap = null;
	var latlng = [];
	var markers = [];
	var map = null;
	var heaton = true;
	var olddata = null;
	var data = null;
	
	setInterval(livetweets, 2000);
	
	function initMap() {
		map = new google.maps.Map(document.getElementById('map'), {
			center : {
				lat : 40.0,
				lng : 0.0
			},
			zoom : 2,
			mapTypeId : google.maps.MapTypeId.ROADMAP,
			mapTypeControl : false
		});

		$.get('GetInitTweets', {
			initval : "yes"
		}, function(newdata) {
			if (newdata != "No Data") {
				data = newdata;
				PushData();
				toggleheat();
			}
		});

	}	
	
	function PushData() {
		markers = [];
		latlng = [];
		olddata = data;
		for (var i = 0; i < data.length; i++) {
			var latlong = new google.maps.LatLng(data[i].Latitude,
					data[i].Longitude);
			var marker = new google.maps.Marker({
				position : latlong,
				icon: 'images/singletweet.png'
			});
			latlng.push(latlong);
			markers.push(marker);
		}
	}

	function toggleheat() {
		if (heaton) {
			if (markerCluster == null) {
				markerCluster = new MarkerClusterer(map, markers);
			}
			markerCluster.resetViewport();
			markerCluster.clearMarkers();
			markerCluster.addMarkers(markers);
			if (heatmap != null) {
				heatmap.setMap(null);
			}
			heaton = !heaton;
			var heatcontrol = document.getElementById('heattoggle');
			heatcontrol.value = "Heat Map";
		} else {
			for ( var i in markers) {
				markers[i].setMap(null);
			}
			if(heatmap != null){
				heatmap.setMap(null);
			}			
			heatmap = new google.maps.visualization.HeatmapLayer({
				data : latlng,
				radius : 30,
			});
			heatmap.setMap(map);
			markerCluster.clearMarkers();
			heaton = !heaton;
			var heatcontrol = document.getElementById('heattoggle');
			heatcontrol.value = "Cluster Map";
		}
	}
	
	function livetweets() {
		var selection = document.getElementById('topic');
		if (selection != null) {
			var query = selection.options[selection.selectedIndex].text;			
			$.get('GetInitTweets', {
				initval : "live",
				queryval : query
			}, function(newdata) {				
				if (newdata != "No Data" && !heaton) {
					var tempdata = [];
					for (var i = 0; i < newdata.length; i++) {
						if (olddata != null) {
							var flag = true;
							for (var j = 0; j < olddata.length; j++) {
								if (olddata[j].TweetId == newdata[i].TweetId) {
									flag = false;									
									break;
								}
							}
							if (flag) {
								tempdata.push(newdata[i]);															
							}
						}
					}
					if (olddata == null) {
						tempdata = newdata;
					}
					for (var i = 0; i < tempdata.length; i++) {
						var latlong = new google.maps.LatLng(
								tempdata[i].Latitude, tempdata[i].Longitude);
						var marker = new google.maps.Marker({	
							animation: google.maps.Animation.DROP,
							position : latlong,
							icon: 'images/bullet.gif'
						});
						BlinkMarker(marker);
					}
					olddata = newdata.concat();
				}
			});
		}
	}

	function BlinkMarker(marker) {
		markers.push(marker);
		marker.setMap(map);
		setTimeout(Blinked, 1500, marker);
	}

	function Blinked(marker) {
		marker.setMap(null);
		markerCluster.addMarker(marker);
	}	

	function filtermarker() {
		var selection = document.getElementById('topic');
		var query = selection.options[selection.selectedIndex].text;
		$.get('GetInitTweets', {
			initval : "no",
			queryval : query
		}, function(newdata) {
			if (newdata != "No Data") {
				data = newdata;
 				for ( var i in markers) {
 					markers[i].setMap(null);
 				}
				PushData();
				heaton = !heaton;
				toggleheat();
			}
		});
	}
</script>
<style type="text/css">
      html, body { height: 100%; margin: 0; padding: 0; }
      #map { height: 100%; width:88.5%; float: right; padding: 2px;  border: thick; border-color: black; border-style: double; border-radius: 10px }
      #control {height: 100%; width: 10%; float: left; border: thick; border-color: black; border-style: double; border-radius: 10px; text-align: center;}
      #Page {height: 100%; width: 100%; margin-left: 5px; margin-top: 5px; margin-right: 5px }
      #heattoggle {position: relative; top: 50%}
      #topic {position: relative; top: 50%}
</style>
</head>
<body onload="initMap()" >
	<div id="Page">
		<div id="control" >		
			<select id="topic" onchange="filtermarker()">
				<option value="All" selected="selected">All</option>
				<option value="tv">TV</option>
				<option value="health">Health</option>				
				<option value="tech">Tech</option>				
				<option value="sport">Sport</option>
				<option value="travel">Travel</option>
				<option value="style">Style</option>
				<option value="shopping">Shopping</option>
				<option value="weather">Weather</option>
				<option value="news">News</option>
				<option value="beauty">Beauty</option>
				<option value="finance">Finance</option>
				<option value="movie">Movie</option>
				<option value="dating">Dating</option>
				<option value="politics">Politics</option>
			</select>
			<input id="heattoggle" type="button" value="Heat Map" onclick="toggleheat()" width="100px" height="50px" >		
		</div>
		<div id="map"></div>
	</div>	
</body>
</html>