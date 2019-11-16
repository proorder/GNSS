import 'dart:async';
import 'dart:convert';
import 'dart:math';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:neva_energy/utils.dart';

import 'IconRow.dart';

class DataSourceScreen extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _DataSourceScreen();
  }
}

class _DataSourceScreen extends State<DataSourceScreen> {
  var satsUsed;
  var accuracyXY;
  var accuracyZ;
  var DGPSAge;
  var PDOP;
  var velocity;
  var error;
  var status;
  var nmeaMessages = [];
  bool switcher = true;
  Timer timer;

  void updateState() {
    timer = Timer.periodic(Duration(seconds: 1), (Timer t) async {
      const platform = const MethodChannel('flutter.native/helper');
      try {
        var response = await platform.invokeMethod('getData');
        response = jsonDecode(response);
        setState(() {
          if (response['status'].toString() != null) {
            switch (response['status']) {
              case 0:
                status = 'No position';
                break;
              case 1:
                status = 'Autonom';
                break;
              case 2:
                status = 'DGPS';
                break;
              case 3:
                status = 'GPS PPS';
                break;
              case 4:
                status = 'FIXED RTK';
                break;
              case 5:
                status = 'Float RTK';
                break;
              case 6:
                status = 'Extrapolation';
                break;
              case 7:
                status = 'Manual';
                break;
              case 8:
                status = 'simulation';
                break;
            }
          }
          satsUsed = response['satsUsed'].toString();
          accuracyXY = response['accuracyXY'].toString();
          accuracyZ = response['accuracyZ'].toString();
          DGPSAge = response['DGPSAge'].toString();
          PDOP = response['PDOP'].toString();
          velocity = response['velocity'].toString();
          if (response['nmea'] != null && switcher) {
            nmeaMessages = response['nmea'];
          }
        });
        if (response['error'] != null && error == null) {
          showAlert(context, response['error']);
          error = response['error'];
        }
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
      children: <Widget>[
        Row(
          children: <Widget>[
            Expanded(
              child: Container(
                margin: const EdgeInsets.only(
                    top: 16.0, bottom: 0, left: 8.0, right: 8.0),
                padding: const EdgeInsets.all(16.0),
                color: Colors.white,
                child: Row(
                  children: <Widget>[
                    Expanded(
                      child: Column(
                        children: <Widget>[
                          IconRow(
                              "Sats used",
                              satsUsed != null ? satsUsed : "-",
                              Icon(Icons.satellite)),
                          IconRow(
                              "Accuracy XY",
                              accuracyXY != null ? accuracyXY : "-",
                              Transform.rotate(
                                angle: pi / 180 * 90,
                                child: Icon(Icons.vertical_align_center),
                              )),
                          IconRow(
                              "Accuracy Z",
                              accuracyZ != null ? accuracyZ : "-",
                              Icon(Icons.vertical_align_center)),
                          IconRow("Velocity", velocity != null ? velocity : "-",
                              Icon(Icons.network_check)),
                        ],
                      ),
                    ),
                    Expanded(
                      child: Column(
                        children: <Widget>[
                          IconRow(
                              "GPS Status",
                              status != null ? status : "-",
                              Icon(
                                Icons.my_location,
                                color: Colors.green,
                              )),
                          IconRow("DGPS Age", DGPSAge != null ? DGPSAge : "-",
                              Icon(Icons.history)),
                          IconRow("PDOP", PDOP != null ? PDOP : "-",
                              Icon(Icons.gamepad)),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
        Container(
          margin: EdgeInsets.only(top: 8, left: 8, right: 8),
          child: Row(
            children: <Widget>[
              Expanded(
                child: Text('NMEA сообщения:'),
              ),
              CupertinoSwitch(
                activeColor: Colors.blue,
                value: switcher,
                onChanged: (bool value) {
                  setState(() {
                    switcher = value;
                  });
                },
              ),
            ],
          ),
        ),
        Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: nmeaMessages
              .map(
                (row) => Container(
                  color: Colors.grey[100],
                  margin: EdgeInsets.only(top: 2, left: 8, right: 8),
                  padding: EdgeInsets.symmetric(vertical: 12, horizontal: 16),
                  child: Text(row, style: TextStyle(fontSize: 12)),
                ),
              )
              .toList(),
        ),
      ],
    );
  }
}