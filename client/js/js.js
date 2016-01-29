var map;

function initMap() {
    map = L.map('map')
        .setView([0, 0], 0);
    L.tileLayer('http://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>',
        maxZoom: 19
    }).addTo(map);
}

angular.module('osmm', [])
    .controller('OsmmController', function($scope, $http) {
        $scope.endpoint = "/server";
        $scope.users = {};
        $scope.trackingType = "all";
        $scope.liveTracking = true;

        var liveTrackingTask = null;

        var createPointPopup = function(point) {
            return L.popup().setContent("<pre>" + JSON.stringify(point, null, " ") + "</pre>");
        };

        var createTrackLayer = function(track, color) {
            var latlngs = [];
            var trackLayer = L.layerGroup();
            $.each(track.points, function(pointIndex, point) {
                var latLng = L.latLng(point.lat, point.lon, point.altitude);
                latlngs.push(latLng);
                L.circle(latLng, 0, {color: color, weight: 10})
                    .bindPopup(createPointPopup(point))
                    .addTo(trackLayer);
            });
            L.polyline(latlngs, {color: color, clickable: false}).addTo(trackLayer);
            return trackLayer;
        };

        var randomColor = function() {
            var safeColors = ['00','33','66','99','cc','ff'];
            var rand = function() {
                return Math.floor(Math.random()*6);
            };
            var r = safeColors[rand()];
            var g = safeColors[rand()];
            var b = safeColors[rand()];
            return "#"+r+g+b;
        };

        var loadDataForUrl = function(url) {
            $http.get(url).then(function(response) {
                $.each($scope.users, function(userName, user) {
                    if (user.layer) {
                        map.removeLayer(user.layer);
                        user.layer = null;
                    }
                });
                var data = response.data;
                $.each(data, function(userName, tracks) { // Per user
                    if (!$scope.users.hasOwnProperty(userName)) {
                        $scope.users[userName] = {
                            color: randomColor(),
                            checked: true,
                            userName: userName
                        };
                    }
                    var user = $scope.users[userName];
                    var trackLayers = [];
                    $.each(tracks, function(trackIndex, track) { // Per track
                        var trackLayer = createTrackLayer(track, user.color);
                        trackLayers.push(trackLayer);
                    });
                    var userLayer = L.layerGroup(trackLayers);
                    user.layer = userLayer;
                    if (user.checked) {
                        userLayer.addTo(map);
                    }
                });
            }, function(respose) {
                $scope.liveTracking = false;
                alert("Error:\n" + respose.data);
            });
        };

        var getUrlSuffix = function() {
            switch ($scope.trackingType) {
                case "all":
                    return "/get-all";
                case "last-one":
                    return "get-last-one";
                default:
                    return null;
            }
        };

        $scope.loadDataOnClick = function() {
            loadDataForUrl($scope.endpoint + getUrlSuffix() + "?key=" + $scope.apiKey);
        };
        $scope.userOnSelectedChanged = function(user) {
            if (user.checked) {
                map.addLayer(user.layer);
            } else {
                map.removeLayer(user.layer);
            }
        };

        $scope.$watch("liveTracking", function(newValue) {
            if (liveTrackingTask) {
                clearInterval(liveTrackingTask);
                liveTrackingTask = null;
            }
            if (newValue) {
                liveTrackingTask = setInterval($scope.loadDataOnClick, 1000);
                $scope.loadDataOnClick();
            }
        });
    });

$(document).ready(initMap);
