import 'dart:async';

import 'package:flutter/services.dart';

/// Scan mode which is either QR code or BARCODE
enum ScanMode { QR, BARCODE, DEFAULT }

/// Provides access to the barcode scanner.
///
/// This class is an interface between the native Android and iOS classes and a
/// Flutter project.
class FlutterBarcodeScanner {
  static const MethodChannel _channel = MethodChannel('flutter_barcode_scanner');

  static const EventChannel _eventChannel = EventChannel('flutter_barcode_scanner_receiver');

  static Stream? _onBarcodeReceiver;

  /// Scan with the camera until a barcode is identified, then return.
  ///
  /// Shows a scan line with [lineColor] over a scan window. A flash icon is
  /// displayed if [isShowFlashIcon] is true. The text of the cancel button can
  /// be customized with the [cancelButtonText] string.
  static Future<String> scanBarcode(
      {required String lineColor, required ScanMode scanMode, required String alertText,required String titleText, required bool isUserPremium}) async {
    // Pass params to the plugin
    Map params = <String, dynamic>{
      'lineColor': lineColor,
      'isContinuousScan': false,
      'scanMode': scanMode.index,
      'isUserPremium': isUserPremium,
      'alertText': alertText,
      'titleText': titleText,

    };

    /// Get barcode scan result
    final barcodeResult = await _channel.invokeMethod('scanBarcode', params) ?? '';
    return barcodeResult;
  }

  /// Returns a continuous stream of barcode scans until the user cancels the
  /// operation.
  ///
  /// Shows a scan line with [lineColor] over a scan window. A flash icon is
  /// displayed if [isShowFlashIcon] is true. The text of the cancel button can
  /// be customized with the [cancelButtonText] string. Returns a stream of
  /// detected barcode strings.
  static Stream? getBarcodeStreamReceiver({
    required String lineColor,
    required ScanMode scanMode,
    required String alertText,
    required String titleText,
    required bool isUserPremium,
  }) {
    // Pass params to the plugin
    Map params = <String, dynamic>{
      'lineColor': lineColor,
      'isContinuousScan': false,
      'scanMode': scanMode.index,
      'isUserPremium': isUserPremium,
      'alertText': alertText,
      'titleText': titleText,
    };
    // Invoke method to open camera, and then create an event channel which will
    // return a stream
    _channel.invokeMethod('scanBarcode', params);
    _onBarcodeReceiver ??= _eventChannel.receiveBroadcastStream();
    return _onBarcodeReceiver;
  }
}
