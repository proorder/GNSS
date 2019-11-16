import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class LocationScreen extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _LocationScreen();
  }
}

class _LocationScreen extends State<LocationScreen> {
  double latitude;
  double longitude;
  double altitude;
  Timer timer;

  void updateState() {
    timer = Timer.periodic(Duration(milliseconds: 500), (Timer t) async {
      const platform = const MethodChannel('flutter.native/helper');
      try {
        var response = await platform.invokeMethod('getGeolocation');
        response = jsonDecode(response);
        setState(() {
          latitude = response['latitude'];
          longitude = response['longitude'];
          altitude = response['altitude'];
        });
      } on PlatformException catch (e) {}
    });
  }

  @override
  void initState() {
    super.initState();
    updateState();
  }

  @override
  void dispose() {
    super.dispose();
    timer.cancel();
  }

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: EdgeInsets.symmetric(vertical: 12, horizontal: 8),
      children: <Widget>[
        Container(
          padding: EdgeInsets.symmetric(horizontal: 4, vertical: 4),
          color: Colors.white,
          child: Row(
            children: <Widget>[
              Text('Широта: '),
              Text(latitude.toString(),
                  style: TextStyle(fontWeight: FontWeight.bold)),
            ],
          ),
        ),
        Container(
          padding: EdgeInsets.symmetric(horizontal: 4, vertical: 4),
          color: Colors.white,
          child: Row(
            children: <Widget>[
              Text('Долгота: '),
              Text(longitude.toString(),
                  style: TextStyle(fontWeight: FontWeight.bold)),
            ],
          ),
        ),
        Container(
          padding: EdgeInsets.symmetric(horizontal: 4, vertical: 4),
          color: Colors.white,
          child: Row(
            children: <Widget>[
              Text('Высота: '),
              Text(altitude.toString(),
                  style: TextStyle(fontWeight: FontWeight.bold)),
            ],
          ),
        ),
      ],
    );
  }
}
