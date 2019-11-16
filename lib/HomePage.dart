import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:neva_energy/utils.dart';

import 'BluetoothDevicesScreen.dart';
import 'DataSourceScreen.dart';
import 'DrawScreen.dart';
import 'LocationScreen.dart';
import 'SettingsScreen.dart';

class HomePage extends StatelessWidget {
  HomePage();

  @override
  Widget build(BuildContext context) {
    Future.delayed(Duration.zero, () async {
      var status;
      var response;
      try {
        const platform = const MethodChannel('flutter.native/helper');
        final String result = await platform.invokeMethod('testMockLocation');
        response = jsonDecode(result);
        status = response["status"];
        response = response["message"];
      } on PlatformException catch (e) {
        status = false;
        response = "Failed to Invoke: '${e.message}'.";
      }

      if (!status) {
        showAlert(context, response);
      }
    });

    SystemChrome.setSystemUIOverlayStyle(
        SystemUiOverlayStyle.dark.copyWith(statusBarColor: Colors.blue[600]));

    return SafeArea(
      top: true,
      child: Stack(
        children: <Widget>[
          Image.asset(
            "assets/background.png",
            height: MediaQuery.of(context).size.height,
            width: MediaQuery.of(context).size.width,
            fit: BoxFit.cover,
          ),
          Scaffold(
            backgroundColor: Colors.transparent,
            body: DefaultTabController(
              length: 5,
              child: SizedBox(
                child: Column(
                  children: <Widget>[
                    Container(
                      padding: const EdgeInsets.all(16.0),
                      child: Image.asset("assets/logo.png"),
                    ),
                    TabBar(
                      labelColor: Colors.white,
                      tabs: <Widget>[
                        Tab(icon: Icon(Icons.gps_fixed)),
                        Tab(icon: Icon(Icons.bluetooth)),
                        Tab(icon: Icon(Icons.public)),
                        Tab(icon: Icon(Icons.location_on)),
                        Tab(icon: Icon(Icons.format_list_bulleted)),
                      ],
                    ),
                    Expanded(
                      child: TabBarView(
                        children: <Widget>[
                          Container(
                            color: Colors.transparent,
                            child: DataSourceScreen(),
                          ),
                          Container(
                            child: BluetoothDevicesScreen(),
                          ),
                          Container(
                            child: DrawScreen(),
                          ),
                          Container(
                            child: LocationScreen(),
                          ),
                          Container(
                            child: SettingsScreen(),
                          ),
                        ],
                      ),
                    ),
                    Container(
                      margin: const EdgeInsets.symmetric(
                          horizontal: 0, vertical: 16),
                      padding: const EdgeInsets.symmetric(
                          horizontal: 16, vertical: 8),
                      color: Colors.red,
                      child: Row(
                        children: <Widget>[
                          Expanded(
                            child: Text(
                              "ООО «Невская Энергетика»",
                              style: TextStyle(color: Colors.white),
                            ),
                          ),
                          Text(
                            "www.nevaenergy.ru",
                            style: TextStyle(color: Colors.white),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
