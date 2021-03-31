package com.csg.bluepigeon.pigeon;
import android.bluetooth.*;
import android.util.*;

import java.io.*;
import java.util.*;
import javax.obex.*;

public class BluetoothOPPHelper
{
    String address;
    BluetoothAdapter mBtadapter;
    BluetoothDevice device;
    ClientSession session;
    BluetoothSocket mBtSocket;
    protected final UUID OPPUUID=UUID.fromString(("00001105-0000-1000-8000-00805f9b34fb"));

    private String TAG="BluetoothOPPHelper";

    public BluetoothOPPHelper(String address)
    {
        mBtadapter = BluetoothAdapter.getDefaultAdapter();
        device = mBtadapter.getRemoteDevice(address);
        try
        {
            mBtSocket = device.createInsecureRfcommSocketToServiceRecord(OPPUUID);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);

        }
    }
    public ClientSession StartBatch(int n)
    {
        ClientSession mSession = null;

        boolean retry=true;
        int times=0;
        while (retry && times < 4) {
            try {
                mBtSocket.connect();
            }
            catch (Exception e) {
                Log.e(TAG, "obex failed to connect, trying again");
                retry = true;
                times++;
                continue;
            }

            try {
                BluetoothObexTransport mTransport = null;
                mSession = new ClientSession((ObexTransport)(mTransport = new BluetoothObexTransport(mBtSocket)));

                HeaderSet headerset = mSession.connect(null);

                if (headerset.getResponseCode() == ResponseCodes.OBEX_HTTP_OK) {
                    Log.d(TAG, "StartBatch: successfully started session");
                }
                else {
                    Log.e(TAG, "StartBatch: failed to start session");
                    mSession.disconnect(headerset);
                    times++;
                    continue;
                }

            }
            catch (Exception e)
            {
                Log.e(TAG, "Obex session failed to start" , e);
                retry = true;
                times++;
                continue;
            }
            retry=false;
        }
        return mSession;

    }

    protected boolean Put(ClientSession session, byte[] bytes, String filename, String filetype)
    {
        boolean retry=true;
        int times=0;
        while (retry && times < 4) {
            Operation putOperation=null;
            OutputStream mOutput = null;
            try {
                // Send a file with meta data to the server
                final HeaderSet hs = new HeaderSet();
                hs.setHeader(HeaderSet.NAME, filename);
                hs.setHeader(HeaderSet.TYPE, filetype);
                hs.setHeader(HeaderSet.LENGTH, new Long((long)bytes.length));
                putOperation = session.put(hs);
                mOutput = putOperation.openOutputStream();
                mOutput.write(bytes);
                mOutput.close();
                putOperation.close();
                retry = false;
            }
            catch (Exception e) {
                Log.e(TAG, "put failed", e);
                retry = true;
                times++;
                continue;
            }
            finally {
                try {
                    if(mOutput!=null) {
                        mOutput.close();
                        Log.d(TAG, "Put: close moutput");
                    }
                    if(putOperation!=null) {
                        putOperation.close();
                        Log.d(TAG, "Put: closed putoperation");
                    }
                }
                catch (Exception e) {
                    Log.e(TAG, "put finally" , e);
                    retry = true;
                    times++;
                    continue;
                }
            }

            try {
                session.disconnect(null);
                Thread.sleep((long)500);
                mBtSocket.close();
                return true;
            } catch (IOException e) {
                Log.d(TAG, "Put: diconnecting session");
            } catch (InterruptedException e) {

            }
        }
        return false;
    }

    protected boolean Put(ClientSession s, OPPBatchInfo info)
    {
        return Put(s,info.data,info.as,info.type);
    }

    private void FinishBatch(ClientSession mSession) throws IOException
    {
        mSession.disconnect(null);
        try
        {
            Thread.sleep((long)500);
        }
        catch (InterruptedException e)
        {}
        mBtSocket.close();
    }

    public boolean flush() throws IOException
    {
        if (sendQueue.isEmpty())
        {
            return true;
        }
        try
        {
            Thread.sleep((long)2000);
        }
        catch (InterruptedException e)
        {}
        ClientSession session=StartBatch(sendQueue.size());
        if (session == null)
        {
            return false;
        }
        while (!sendQueue.isEmpty())
        {
            if (Put(session, sendQueue.remove()) == false)
            {
                Log.e(TAG, "Put failed");
            }
        }
        FinishBatch(session);
        return true;
    }
    Queue<OPPBatchInfo> sendQueue;
    public boolean AddTransfer(String as,String mimetype,byte[] data)
    {
        return sendQueue.add(new OPPBatchInfo(as,mimetype,data));
    }
    class OPPBatchInfo
    {
        String as;
        String type;
        byte[] data;
        public OPPBatchInfo(String as,String type,byte[] data)
        {
            this.as=as;
            this.data=data;
            this.type=type;
        }
    }
}