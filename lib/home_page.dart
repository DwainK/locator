import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:geolocator/geolocator.dart';

class HomePage extends StatefulWidget {
  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  static const serviceChannel =
      const MethodChannel("biz.corestats.locator/foregroundService");

  String _locationMessage = "";

  @override
  void initState() {
    super.initState();
    startServiceInPlatform();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Center(child: Text('Locator')),
      ),
      body: Align(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(_locationMessage),
            FlatButton(
                onPressed: () {
                  _getCurrentLocation();
                  getCurrentLocationChannel();
                },
                color: Colors.green,
                child: Text('Find Location'))
          ],
        ),
      ),
    );
  }

  void startServiceInPlatform() async {
    if (Platform.isAndroid) {
      String data = await serviceChannel.invokeMethod("startService");
      debugPrint(data);
    }
  }

  void _getCurrentLocation() async {
    final position = await Geolocator()
        .getCurrentPosition(desiredAccuracy: LocationAccuracy.high);
    print(position);

    setState(() {
      _locationMessage = "${position.latitude},${position.longitude}";
    });
  }

  void getCurrentLocationChannel() {
    serviceChannel.invokeMethod('getLocation');
  }
}
