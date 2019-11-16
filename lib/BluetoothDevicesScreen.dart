import 'dart:convert';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class BluetoothDevicesScreen extends StatefulWidget {
  @override
  _BluetoothDevicesScreen createState() => new _BluetoothDevicesScreen();
}

class _BluetoothDevicesScreen extends State<BluetoothDevicesScreen> {
  var devices = [];

  String status = "";

  _BluetoothDevicesScreen() {
    doRequest();
  }

  void deviceTap(String deviceAddress) {
    const platform = const MethodChannel('flutter.native/helper');
    try {
      platform.invokeMethod('device:' + deviceAddress);
    } on PlatformException catch (e) {}
  }

  void doRequest() async {
    const platform = const MethodChannel('flutter.native/helper');
    var response;
    try {
      final String result = await platform.invokeMethod('getPairedDevices');
      response = jsonDecode(result);
      setState(() {
        devices = response;
      });
    } on PlatformException catch (e) {}
  }

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: EdgeInsets.symmetric(vertical: 12),
      children: devices
          .map((el) => GestureDetector(
                onTap: () {
                  deviceTap(el[1]);
                },
                child: Container(
                  margin: EdgeInsets.symmetric(vertical: 4, horizontal: 8),
                  padding: EdgeInsets.symmetric(vertical: 12, horizontal: 16),
                  color: Colors.white,
                  child: Row(
                    children: <Widget>[
                      Expanded(
                        child: Text(el[0]),
                      ),
                      Text(el[1]),
                    ],
                  ),
                ),
              ))
          .toList(),
    );
  }
}