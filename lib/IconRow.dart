import 'package:flutter/material.dart';

class IconRow extends StatelessWidget {
  final String name;
  final String value;
  final Widget icon;

  IconRow(this.name, this.value, this.icon);

  @override
  Widget build(BuildContext context) {
    return Row(
      children: <Widget>[
        icon,
        Padding(
          padding: EdgeInsets.symmetric(horizontal: 8, vertical: 8),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Text(name),
              Text(value, style: TextStyle(fontWeight: FontWeight.bold),),
            ],
          ),
        ),
      ],
    );
  }
}