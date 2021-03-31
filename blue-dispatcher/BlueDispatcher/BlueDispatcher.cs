using System;
using System.IO;
using System.Text;
using System.Net;
using System.Threading;
using System.Collections.Generic;
using System.Security.Cryptography;

using Newtonsoft.Json;

using InTheHand.Net;
using InTheHand.Net.Sockets;

namespace BlueDispatcher
{
    class BlueDispatcher
    {
        readonly BluetoothClient client;
        readonly int scanRetry = 3;
        readonly int sendRetry = 3;
        readonly int sendSleep = 5000;
        readonly string BLUE_MSG_PREFIX = "BP-";
        readonly string FILE_EXT = ".txt";
        readonly string secretkey = "testkey";
        public BlueDispatcher()
        {
            client = new BluetoothClient();
        }
        public void Send(String deviceName, Dictionary<string, string> payload)
        {
            BluetoothDeviceInfo device = Scan(deviceName);
            string filename = BLUE_MSG_PREFIX + GenBlueMsgFilename() + FILE_EXT;
            File.AppendAllText(filename, JsonConvert.SerializeObject(payload));
            SendFile(device.DeviceAddress, filename);
        }

        /// <summary>
        /// Scans for blue pigeon device
        /// </summary>
        /// <param name="deviceName"></param>
        /// <returns>BluetoothDeviceInfo</returns>
        private BluetoothDeviceInfo Scan(String deviceName)
        {
            for (int i = 0; i < scanRetry; i++)
            {
                Console.WriteLine("Scanning... ");
                BluetoothDeviceInfo[] devices = client.DiscoverDevicesInRange();
                Console.WriteLine("Finished Scan... ");
                Console.WriteLine("Number of discoverable bluetooth devices: " + devices.Length.ToString());
                foreach (BluetoothDeviceInfo d in devices)
                {
                    if (d.DeviceName == deviceName)
                    {
                        Console.WriteLine("Found device");
                        return d;
                    }
                }
                if (i < scanRetry - 1)
                {
                    Console.WriteLine("No device found, retrying scan");
                }
            }
            throw (new PigeonNotFoundException());
        }

        /// <summary>
        /// Generates the blue message filename that blue pigeon will accept
        /// </summary>
        /// <returns>newly generated file name</returns>
        private string GenBlueMsgFilename()
        {
            string randName = RandomString(32);
            byte[] b = new HMACSHA512(Encoding.UTF8.GetBytes(secretkey)).ComputeHash(Encoding.UTF8.GetBytes(randName));
            string hash = BitConverter.ToString(b).Replace("-", "").Substring(0,32).ToLower();
            return hash + randName;
        }

        /// <summary>
        /// Generates random strings to be used in the filename generation
        /// </summary>
        /// <param name="length"></param>
        /// <returns>random string</returns>
        private string RandomString(int length)
        {
            const string valid = "abcdefghijklmnopqrstuvwxyz1234567890";
            StringBuilder res = new StringBuilder();
            using (RNGCryptoServiceProvider rng = new RNGCryptoServiceProvider())
            {
                byte[] uintBuffer = new byte[sizeof(uint)];

                while (length-- > 0)
                {
                    rng.GetBytes(uintBuffer);
                    uint num = BitConverter.ToUInt32(uintBuffer, 0);
                    res.Append(valid[(int)(num % (uint)valid.Length)]);
                }
            }

            return res.ToString();
        }

        /// <summary>
        /// Obex method that sends the file to the blue pigeon
        /// </summary>
        /// <param name="addr"></param>
        /// <param name="filepath"></param>
        private void SendFile(BluetoothAddress addr, String filepath)
        {
            for (int i = 0; i < sendRetry; i++)
            {
                try
                {
                    var req = new ObexWebRequest(addr, filepath);
                    req.ReadFile(filepath);
                    ObexWebResponse rsp = (ObexWebResponse)req.GetResponse();
                    Console.WriteLine("Response Code: {0} (0x{0:X})", rsp.StatusCode);
                    break;
                }
                catch (WebException e)
                {
                    Console.WriteLine("Blue Coop is busy...");
                    Console.WriteLine("Will try again in 5s...");
                    Thread.Sleep(sendSleep);
                }
            }
        }

    }

}
