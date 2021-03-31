package com.csg.bluepigeon.pigeon;

import android.bluetooth.BluetoothSocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.obex.ObexTransport;

public class BluetoothObexTransport implements ObexTransport {
    private BluetoothSocket mSocket = null;
    public BluetoothObexTransport(BluetoothSocket socket) {
        this.mSocket = socket;
    }
    @Override
    public void close() throws IOException {
        mSocket.close();
    }
    @Override
    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }
    @Override
    public DataOutputStream openDataOutputStream() throws IOException {
        return new DataOutputStream(openOutputStream());
    }
    @Override
    public InputStream openInputStream() throws IOException {
        return mSocket.getInputStream();
    }
    @Override
    public OutputStream openOutputStream() throws IOException {
        return mSocket.getOutputStream();
    }
    @Override
    public void connect() throws IOException {
    }
    @Override
    public void create() throws IOException {
    }
    @Override
    public void disconnect() throws IOException {
    }
    @Override
    public void listen() throws IOException {
    }
    public boolean isConnected() throws IOException {
        return true;
    }
    @Override
    public int getMaxTransmitPacketSize() {
        //if (mSocket.getConnectionType() != BluetoothSocket.TYPE_L2CAP) {
        //    return -1;
        // }
        return 8192;//mSocket.getMaxTransmitPacketSize();
    }
    @Override
    public int getMaxReceivePacketSize() {
        // if (mSocket.getConnectionType() != BluetoothSocket.TYPE_L2CAP) {
        //    return -1;
        // }
        return 8192;//mSocket.getMaxReceivePacketSize();
    }
    public String getRemoteAddress() {
        if (mSocket == null) {
            return null;
        }
        return mSocket.getRemoteDevice().getAddress();
    }
    @Override
    public boolean isSrmSupported() {
        //  if (mSocket.getConnectionType() == BluetoothSocket.TYPE_L2CAP) {
        //      return true;
        //   }
        return false;
    }
}
