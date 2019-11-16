import 'dart:async';
import 'dart:convert';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class DrawScreen extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _DrawScreen();
  }
}

class _DrawScreen extends State<DrawScreen> {
  Timer timer;
  var satellites = [];

  void updateState() {
    timer = Timer.periodic(Duration(seconds: 1), (Timer t) async {
      const platform = const MethodChannel('flutter.native/helper');
      try {
        var response = await platform.invokeMethod('getSatellites');
        response = jsonDecode(response);
        setState(() {
          satellites = response;
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
      children: <Widget>[
        Row(
          children: <Widget>[
            Expanded(
              child: Container(
                margin: EdgeInsets.only(left: 8, top: 8),
                padding: EdgeInsets.symmetric(vertical: 8, horizontal: 12),
                color: Colors.yellow,
                child: Text('GPS'),
              ),
            ),
            Expanded(
              child: Container(
                margin: EdgeInsets.only(left: 8, right: 8, top: 8),
                padding: EdgeInsets.symmetric(vertical: 8, horizontal: 12),
                color: Colors.red[400],
                child: Text('GALILEO'),
              ),
            ),
            Expanded(
              child: Container(
                margin: EdgeInsets.only(right: 8, top: 8),
                padding: EdgeInsets.symmetric(vertical: 8, horizontal: 12),
                color: Colors.green[400],
                child: Text('GLONASS'),
              ),
            ),
          ],
        ),
        CustomPaint(
          size: Size(MediaQuery.of(context).size.width,
              MediaQuery.of(context).size.width),
          painter: Signature(satellites: satellites),
        ),
      ],
    );
  }
}

class Signature extends CustomPainter {
  var satellites = [];

  Signature({this.satellites});

  double elevationToRadius(double s, double elev) {
    return ((s / 2) - 10) * (1 - (elev / 90));
  }

  @override
  void paint(Canvas canvas, Size size) {
    var paint = Paint()
      ..style = PaintingStyle.stroke
      ..color = Colors.black;

    canvas.drawCircle(
        Offset(size.width / 2, size.width / 2), size.width / 2 - 10, paint);
    canvas.drawLine(Offset(10, size.width / 2),
        Offset(size.width - 10, size.width / 2), paint);
    canvas.drawLine(Offset(size.width / 2, 10),
        Offset(size.width / 2, size.width - 10), paint);

    if (satellites != null) {
      for (var satellite in satellites) {
        var elev = satellite['elev'];
        var azim = satellite['azim'];
        var prn = satellite['prn'];
        var snr = satellite['snr'];
        if (elev != null && azim != null && prn != null && snr != null) {
          var radius = elevationToRadius(size.width, elev);
          var x = size.width / 2 + radius * sin(azim * pi / 180);
          var y = size.width / 2 + radius * cos(azim * pi / 180);
          Paint paint = Paint();
          if (snr == 0) {
            paint.style = PaintingStyle.stroke;
          } else {
            paint.style = PaintingStyle.fill;
          }
          if (satellite['type'] == 'gps') {
            paint.color = Colors.yellow;
          } else if (satellite['type'] == 'glonass') {
            paint.color = Colors.green[400];
          } else {
            paint.color = Colors.red;
          }
          canvas.drawCircle(Offset(x, y), 5, paint);
        }
      }
    }
  }

  @override
  bool shouldRepaint(CustomPainter oldDelegate) {
    return true;
  }
}
