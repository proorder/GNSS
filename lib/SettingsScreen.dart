import 'dart:convert';
import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class SettingsScreen extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _SettingsScreen();
  }
}

class _SettingsScreen extends State<SettingsScreen> {
  bool onFixed = false;
  bool onAnother = false;
  bool loggingOn = false;
  TextEditingController textEditingController = TextEditingController();
  int altErr;

  void updateState() async {
    const platform = const MethodChannel('flutter.native/helper');
    try {
      var response = await platform.invokeMethod('getSoundsState');
      response = jsonDecode(response);
      stdout.write('ON ANOTHER');
      stdout.write(response['on_another']);
      setState(() {
        onFixed = response['on_fixed'] == null ? false : response['on_fixed'];
        onAnother =
            response['on_another'] == null ? false : response['on_another'];
        loggingOn = response['logging'] == null ? false : response['logging'];
        altErr = response['alt_err'] == null ? 10 : response['alt_err'];
        textEditingController.text = altErr.toString();
      });
    } on PlatformException catch (e) {}
  }

  void setAltErr() async {
    if (altErr != null) {
      const platform = const MethodChannel('flutter.native/helper');
      try {
        await platform.invokeMethod('setAltErr:' + altErr.toString());
      } on PlatformException catch (e) {}
    }
  }

  void setOnFixedSound() async {
    const platform = const MethodChannel('flutter.native/helper');
    try {
      await platform.invokeMethod('setOnFixedSound');
    } on PlatformException catch (e) {}
  }

  void setOnAnotherSound() async {
    const platform = const MethodChannel('flutter.native/helper');
    try {
      await platform.invokeMethod('setOnAnotherSound');
    } on PlatformException catch (e) {}
  }

  void setLoggingOn() async {
    const platform = const MethodChannel('flutter.native/helper');
    try {
      await platform.invokeMethod('setLoggingOn');
    } on PlatformException catch (e) {}
  }

  @override
  void initState() {
    super.initState();
    updateState();
  }

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: EdgeInsets.symmetric(vertical: 12, horizontal: 8),
      children: <Widget>[
        Container(
          padding: EdgeInsets.symmetric(vertical: 4, horizontal: 8),
          color: Colors.white,
          child: Row(
            children: <Widget>[
              Expanded(
                child: Text('Сигнал при появлении Fixed: '),
              ),
              CupertinoSwitch(
                activeColor: Colors.blue,
                value: onFixed,
                onChanged: (bool value) {
                  setOnFixedSound();
                  setState(() {
                    onFixed = value;
                  });
                },
              ),
            ],
          ),
        ),
        Container(
          padding: EdgeInsets.symmetric(vertical: 4, horizontal: 8),
          color: Colors.white,
          child: Row(
            children: <Widget>[
              Expanded(
                child: Text('Сигнал при смене статуса с Fixed: '),
              ),
              CupertinoSwitch(
                activeColor: Colors.blue,
                value: onAnother,
                onChanged: (bool value) {
                  setOnAnotherSound();
                  setState(() {
                    onAnother = value;
                  });
                },
              ),
            ],
          ),
        ),
        Container(
          padding: EdgeInsets.symmetric(vertical: 4, horizontal: 8),
          color: Colors.white,
          child: Row(
            children: <Widget>[
              Expanded(
                child: Text('Логирование NMEA: '),
              ),
              CupertinoSwitch(
                activeColor: Colors.blue,
                value: loggingOn,
                onChanged: (bool value) {
                  setLoggingOn();
                  setState(() {
                    loggingOn = value;
                  });
                },
              ),
            ],
          ),
        ),
        Container(
          padding: EdgeInsets.symmetric(vertical: 4, horizontal: 8),
          color: Colors.white,
          child: Column(
            children: <Widget>[
              Text('Погрешность вброса значения высоты, метры: '),
              Row(
                children: <Widget>[
                  Expanded(
                    child: TextField(
                      decoration: InputDecoration(hintText: '10'),
                      keyboardType: TextInputType.number,
                      controller: textEditingController,
                      onChanged: (String e) {
                        setState(() {
                          altErr = int.parse(e);
                        });
                      },
                    ),
                  ),
                  FlatButton(
                    color: Colors.blue,
                    textColor: Colors.white,
                    splashColor: Colors.blueAccent,
                    disabledColor: Colors.grey,
                    disabledTextColor: Colors.black,
                    padding: EdgeInsets.all(8.0),
                    onPressed: () {
                      setAltErr();
                    },
                    child: Text(
                      "Сохранить",
                      style: TextStyle(fontSize: 14.0),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ],
    );
  }
}
