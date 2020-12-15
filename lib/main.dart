import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:locator/home_page.dart';

void main() {
  runApp(Locator());
}

class Locator extends StatefulWidget {
  @override
  _LocatorState createState() => _LocatorState();
}

class _LocatorState extends State<Locator> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Locator',
      home: HomePage(),
    );
  }
}
