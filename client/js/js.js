function initMap() {
    var map = L.map('map')
        .setView([0, 0], 0);
    L.tileLayer('http://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>',
        maxZoom: 19
    }).addTo(map);

    var data = [];
    var latlngs = [];
    for (var i = 0; i < data.length; i++) {
        var point = data[i];
        latlngs.push(L.latLng(point.lat, point.lon, point.altitude));
    }
    var polyline = L.polyline(latlngs, {color: 'red'}).addTo(map);
}
