import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:flutter/foundation.dart';

import 'locations.dart' as locations;

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final Map<String, Marker> _markers = {};
  GoogleMapController controller;

  Future<void> _onMapCreated(GoogleMapController controller) async {
    this.controller = controller;

    final googleOffices = await locations.getGoogleOffices();
    setState(() {
      _markers.clear();
      for (final office in googleOffices.offices) {
        final marker = Marker(
            markerId: MarkerId(office.name),
            position: LatLng(office.lat, office.lng),
            infoWindow: InfoWindow(
              title: office.name,
              snippet: office.address,
            ));
        _markers[office.name] = marker;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final orderedMarkers = _markers.values.toList();
    orderedMarkers.sort((a, b) => a.markerId.value.compareTo(b.markerId.value));

    var googleMap = GoogleMap(
      myLocationEnabled: false,
      myLocationButtonEnabled: false,
      onMapCreated: _onMapCreated,
      initialCameraPosition: CameraPosition(
        target: const LatLng(0, 0),
        zoom: 2,
      ),
      markers: _markers.values.toSet(),
    );

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Google Office Locations'),
          backgroundColor: Colors.green[700],
        ),
        body: Column(
          children: [
            Expanded(
              flex: 2,
              child: googleMap,
            ),
            Expanded(
              flex: 1,
              child: ListView.separated(
                padding: const EdgeInsets.all(8),
                itemCount: orderedMarkers.length,
                itemBuilder: (context, index) {
                  var marker = orderedMarkers[index];
                  return ListTile(
                    title: Text(marker.infoWindow.title),
                    subtitle: Text(marker.infoWindow.snippet),
                    onTap: () {
                      controller
                          .animateCamera(
                              CameraUpdate.newLatLngZoom(marker.position, 6))
                          .then((value) => debugPrint('done animating'));
                    },
                  );
                },
                separatorBuilder: (BuildContext context, int index) =>
                    const Divider(),
              ),
            )
          ],
        ),
      ),
    );
  }
}
