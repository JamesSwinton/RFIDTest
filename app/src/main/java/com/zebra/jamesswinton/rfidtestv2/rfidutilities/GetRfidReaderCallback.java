package com.zebra.jamesswinton.rfidtestv2.rfidutilities;

import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.Readers;

public interface GetRfidReaderCallback {
    void onReadersFound(Readers readers);
    void onRfidReaderFound(RFIDReader rfidReader);
    void onReaderConnected(RFIDReader rfidReader);
    void onReaderConfigured(RFIDReader rfidReader);
    void onError(String errorMessage);
}
