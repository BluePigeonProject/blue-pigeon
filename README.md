<p align="center">
<img src="/images/blue-pigeon-logo.jpg">
<br />
   <b>Blue Pigeon: Bluetooth-based Data Exfiltration and Proxy Tool for Red Teamers</b>
</p>



1. [The Motivation | Birth of the Bird](#the-motivation--birth-of-the-bird)

   ​	[How does Blue Pigeon work?](#how-does-blue-pigeon-work)

   ​	[The Pigeon's Key Requirements](#the-pigeons-key-requirements)

2. [The Components | Dissecting the Pidgey apart](#the-components--dissecting-the-pidgey-apart)

   ​	[1. Blue Pigeon - The Mobile Applicaton](#1-blue-pigeon---the-mobile-application--the-magical-little-blue-messenger)

   ​	[2. Blue Beak - Custom EdXposed Framework Hooks](#2-blue-beak---custom-edxposed-framework-hooks--our-birds-are-smart-and-picky)

   ​	[3. Blue Dispatcher - .NET module for compromised host](#3--blue-dispatcher---net-module-to-inject-into-compromised-host--the-mailman-behind-the-scenes)

3. [Setting up Blue Pigeon](#setting-up-blue-pigeon--requirements-installation-and-usage)

   ​	[Requirements](#requirements)

   ​	[Installation and Usage](#installation-and-usage)

4. [Security Considerations](#security-considerations)

5. [Licensing and Contributing to Blue Pigeon](#licensing-and-contributing-to-blue-pigeon)



# Blue Pigeon: Bluetooth-based Data Exfiltration and Proxy Tool for Red Teamers

Blue Pigeon is a Bluetooth-based data exfiltration and proxy tool to enable communication between a remote Command and Control (C2) server and a compromised host. 

Inspired by the [Rock Dove](https://en.wikipedia.org/wiki/Homing_pigeon)'s ability to carry messages back to its home, Blue Pigeon mimics the homing behaviour and delivers messages/payloads between the agent (compromised host) and its nest (mobile application and subsequently proxied to C2 server).

Blue Pigeon is developed as an Android application for the Red Teamer to deploy within vicinity of the compromised host and is particularly useful for Red Team operations where communications over traditional channels (i.e., web, e-mail, DNS) are not available.

Expanding on the “Exfiltration over Alternative Protocol” technique (ID: T1048) under the Exfiltration tactic of the MITRE AT&CK framework, Blue Pigeon provides a novel way of establishing Command and Control and performing data exfiltration as an Action on Objective of the Cyber Kill Chain by utilizing Bluetooth File Sharing as the exfiltration medium. 

Blue Pigeon is created by [@mahchiahui](https://github.com/mahchiahui), [@jingloon](https://github.com/jingloon) and [@cloudkanghao](https://github.com/cloudkanghao)

<p align="center">
<img src="/images/a-real-bird.jpeg">
<br />
(Source: https://spectrum.ieee.org/tech-history/silicon-revolution/consider-the-pigeon-a-surprisingly-capable-technology)
</p>


## The Motivation | Birth of the Bird

Establishing Command and Control and performing data exfiltration are key phases in the Cyber Kill Chain, but they often come with their complications and severe implications if done wrongly. In a Red Team operation, a misfired attempt could leave permanent traces in the network activity logs and raise alarm to the detection mechanisms. 

In some of our Red Teaming operations, we found ourselves in need of a way to exfiltrate data and communicate with the compromised host without having to go through the traditional channels (i.e., web, email, DNS). 

With few solutions available to address this need we explored various exfiltration ideas based on wireless/radio-comms vectors. As a result, Blue Pigeon was created to expand our Red Team toolset.



### How does Blue Pigeon work?

Blue Pigeon runs as a foreground service within an Android mobile phone. When the Red Teamer/Malicious Insider deploys the phone into proximity to the compromised host (infected with Blue Dispatcher payload), Blue Pigeon will be able to serve as an exfiltration point and command relay proxy to a remote C2 server (a.k.a Blue Coop).

<p align="center">
<img src="/images/overview-chart.jpg">
<br />
<a href="http://www.youtube.com/watch?v=lyhGW8lO1Kc" target="_blank">
<b>Click here for a demo video of Blue Pigeon in action</b>
</a>
<br />
</p>



### The Pigeon's Key Requirements

**Blue Pigeon was engineered with the following key requirements:**

#### **1. Evasiveness**

Blue Pigeon must allow data exfiltration while evading detection on various levels: 

- <u>Evading network traffic detection:</u> Data exfiltration from the compromised host cannot go through traditional communication channels as they are likely to be logged and monitored by standard Endpoint Detection and Response. (and there already are many other better options, why reinvent the wheel, right?)
  - Blue Pigeon communicates over Bluetooth, which (we think) is a rather obscure and unorthodox, yet reliable channel.
  
- <u>Evading human detection:</u> Data exfiltration must happen without any visual indicators on the victim's machine, as he is expected to still be using the compromised machine in the worst-case scenario.

  - o	Blue Pigeon can establish communication without the need for Bluetooth pairing, which would normally trigger a popup for user authentication.






#### **2. Stealth**

Blue Pigeon must maintain stealth, i.e. staying inconspicuous deployed on the field.

- Chunky laptops, Raspberry Pis and homebrew hacker kits are a straight *No-Go* as it limits the possibilities and feasibility of deployment. *We need a small and innocent looking pigeon, not a freakin' alpha male peacock!*

  - Blue Pigeon hides within a typical Android phone that the Red Teamer/Malicious Insider can bring into the field. No one ever suspects a mobile phone...
  
  - In the rare event where the Red Teamer is challenged for inspection, Blue Pigeon can be disguised as an inconspicuous application to suit the context of the operation. In the base proof-of-concept, the mobile application is disguised as a Battery Optimizer app. Feel free to fork the repo and take Blue Pigeon out for an aesthetic makeover!

- Blue Pigeon should avoid causing the Red Teamer to reveal any tell-tale signs of an exfiltration attempt while deploying the phone.

  - The Red Teamer only needs to launch the application and bring the phone within proximity of the compromised machine.

  - Blue Pigeon runs in the background of the mobile phone and does not require the screen to be kept active.

  - Blue Pigeon automatically consumes incoming messages and does not require the Red Teamer to manually accept the file transfer request pop-up (it won't even pop up actually).





#### **3. High Availability**

Blue Pigeon must maintain maximum availability to (and only to) the compromised host while being deployed.

- Blue Pigeon must be able to stay alive to catch incoming messages without regular intervention by the Red Teamer.

- Blue Pigeon must be able to withstand malicious requests/DoS attempts and maintain availability to the intended host.

  - Blue Pigeon is capable of permanently staying in discoverable mode for the compromised host to scan and connect to.
  
  - HMAC-based authentication and filename randomization are employed to mitigate against DoS and replay attacks. Blue Pigeon only accepts the file transfer requests after authenticating with a configurable secret passphrase.





## The Components | Dissecting the Pidgey apart

**Blue Pigeon is designed as a three-part framework comprising of following the components:**

<p align="center">
<img src="/images/blue-pigeon-components.jpg">
<br />
</p>


### 1. Blue Pigeon - The Mobile Application | The magical little blue messenger

Blue Pigeon is the Android mobile application that acts as the intermediary proxy between the compromised host and the C2 server (a.k.a a Blue Coop). Blue Pigeon communicates with the compromised host (infected with the Blue Dispatcher payload) over traditional Bluetooth file sharing (using OBEX). Data is exfiltrated from the host as JSON objects over text files. This can be customized by editing the `BluePigeonListener` class in the `com.csg.bluepigeon.pigeon` package and can be subsequently relayed to a remote Blue Coop. Upon receipt, a copy of the message will also be kept in the `/sdcard/blue_pigeon/backup` directory.

In this repo, Blue Pigeon comes in the form of a simple looking Battery Optimizer application. However, it can be revamped into other UI styles. Feel free to customize Blue Pigeon’s UI to suit your operation's needs by editing the `com.csg.bluepigeon.ui` package. 

<p align="center">
<img src="/images/battery-optimizer.jpg">
<br />
<i>Nothing to see here... just an innocent Battery Optimizer app!</i>
</p>



### 2. Blue Beak - Custom EdXposed Framework Hooks | Our birds are smart and picky!

The Blue Beak package houses a set of custom [EdXposed Framework](https://github.com/ElderDrivers/EdXposed) Hooks* written to bypass the Bluetooth API restrictions of modern Android (Android 9/10):

1. <u>BlueBeakHook</u> - inspects the incoming file transfer request and decides whether to consume it or not. Employs authentication using a configurable secret passphrase to make sure incoming messages are from trusted sources. We don’t want it to accidentally consume a malicious fat request (DoS by hogging attack mitigation). It also rejects previously consumed filenames (DoS by replay attack mitigation). (See [Security Considerations](#security-considerations))

   

2. <u>CooCooHook</u> - bypasses the default Android Bluetooth Discoverable Mode timeout to make sure the mobile phone will continue to broadcast discovery packets while being deployed in the field. This is needed so that the compromised host can scan and reach out to Blue Pigeon at any point in the operation.

   

3. <u>NoPopupHook</u> - bypasses the Bluetooth File Transfer pop-up generation on Android OS and allows accepting or rejecting requests via code. This is to prevent the Red Teamer from constantly being on the phone to accept the incoming messages. *Sus bro, sus...*

   

4. <u>BirdIsAliveHook</u> - provides a neat little visual indicator beside the system clock to indicate that all is well and ready for deployment.

  <p align="center">
    <img src="/images/bird-is-alive-hook.jpg">
    <br />Concept Art vs Real Icon
  </p>




```
*Why EdXposed? Well... we stumbled into 2 major problems early in the conceptualization phase:

1. Due to security concerns, the modern Android OS no longer allows apps to programmatically accept or filter the incoming Bluetooth file transfer requests. This is problematic as the modus operandi of Blue Pigeon relies on it being able to inspect the incoming requests and accept or reject them in real time.
2. The default maximum time of Android's Bluetooth Discovery Mode timeout is  3600 seconds (1 hour). Sticking to this limit would mean that the Red Teamer would have to manually refresh Blue Pigeon frequently, which is rather infeasible and may hinder the conduct of the operation.

To address these problems effectively, we decided to use the EdXposed Framework, which provides Android Runtime (ART) hooking, and write our custom hooks to bypass the OS-level restrictions in a modular fashion.
```





### 3.  Blue Dispatcher - .NET module to inject into compromised host | The mailman behind the scenes

The Blue Dispatcher contains the .NET modules to generate, send and receive payloads to and from Blue Pigeon using the Windows Bluetooth stack. The module allows for file transfer to happen without the need for Bluetooth security authentication between the Blue Pigeon and the compromised host, i.e., device pairing. This helps to maintain evasiveness (a key requirement) as there will not be visual pop-ups on the victim machine, which would elicit suspicion.

<p align="center">
<img src="/images/windows-popup.jpg">
<br/>
    <i>Developer's artistic impression of a failed operation due to visual sabotage. </i>
</p>


Most of the underlying Bluetooth modules used by Blue Dispatcher comes from the well-known and very powerful [32feet](https://github.com/inthehand/32feet/) library. 

**Note: Do implement your own application-layer encryption in Blue Dispatcher if needed, as the payloads are transferred via RFCOMM in plaintext.**





## Setting up Blue Pigeon | Requirements, installation and usage

### Requirements

To deploy Blue Pigeon for your data exfiltration needs, the following are required:

1. A rooted Android 9/10 phone with EdXposed Framework installed. For more details on installing EdXposed Framework, see [here](https://github.com/ElderDrivers/EdXposed).
2. The compromised host must be running Windows 10, have Bluetooth capability, and be infected with the Blue Dispatcher payload via other means. 
3. (Optional) A Blue Coop/remote C2 web server with standard REST API calls for Blue Pigeon to proxy data over to.

### Installation and Usage

1. Blue Pigeon requires a rooted Android phone + Magisk.

2. Install [riru-core](https://forum.xda-developers.com/attachments/magisk-riru-v21-3-zip.5190057/) and [riru-edxposed](https://github.com/ElderDrivers/EdXposed/releases/download/v0.4.6.4/EdXposed-SandHook-v0.4.6.4.4563.-release.zip) via Magisk. Reboot the Android device after installing the modules.

<p align="center">
<img src="/images/installation-riru.jpg">
</p>

3. Install the [EdXposed Manager](https://github.com/ElderDrivers/EdXposedManager/releases/download/v4.5.7.0.0/EdXposedManager-4.5.7-45700-org.meowcat.edxposed.manager-release.apk) apk. You should then get the following screen showing that EdXposed Framework is active. 

<p align="center">
<img src="/images/installation-edxposed-manager.jpg">
</p>

4. Install the BluePigeon.apk to load up Blue Beak hooks into EdXposed Manager. You should then see the Battery Optimizer module in the modules list.

<p align="center">
<img src="/images/installation-modules.jpg">
</p>

5. Reboot the device and you should see the Blue Pigeon icon beside the clock if all is successful.

<p align="center">
<img src="/images/installation-icon.jpg">
</p>

6. Launch Blue Pigeon by opening the "Battery Optimizer" application.

7. The Blue Coop endpoint and the secret passphrase (used for authentication) can be configured by clicking the buttons on the top left corner.

<p align="center">
<img src="/images/installation-bp.jpg">
</p>

8. Now, all the Red Teamer has to do is to bring the phone nearby, keep it juiced up, and click on the start button!







## Security Considerations

As an offensive tool, it would be kinda embarrassing if Blue Pigeon was to fall short against simple attempts from other attackers or bird haters. 

For a start\*, Blue Pigeon is designed with protection against these few potential attacks:



### 1. Rogue Request Prevention

One limitation of using the Bluetooth File Sharing as the exfiltration medium is its lack of asynchronous transfer. As only 1 file transfer process may take place at any point in time, Blue Pigeon must be careful to only allow and consume legitimate incoming requests.

If an adversary in the vicinity can craft a rogue request and trick Blue Pigeon into accepting it, they could possibly induce a temporary DoS attack against the setup by sending an excessively large dummy file. The attacker could even attempt to compromise the mobile phone or remote Blue Coop with malicious content embedded within the file.

To mitigate against such potential attacks, the file names of the incoming file requests are authenticated using a HMAC authentication scheme with a configurable shared passphrase.


The filename verification procedure is broken down into the following steps:

1. Filename must match expected format: eg `BP-e2211d9e19f1669d1a09ea4828d3e2bc171a10bce71cafb53a64179f497273ea.txt`
2. The 64-length hex string is split into two parts: `providedHash` and `rand`
3. Verify `HmacSHA512(rand, passphrase)` against the `providedHash` (comparing the first 32 hexadecimal digits/16 bytes)
4. Request is then considered as legitimate.

The file name verification logic resides in `com.csg.bluepigeon.util.EncryptionManager`and can be customized accordingly. 

*Developer note: Actually, a sender MAC address verification would be the most ideal, but we went with file name verification instead as it would be difficult to determine the victim’s Bluetooth MAC address in advance.*



### 2. Replay Attack Prevention

Another avenue of attack would be via replay attacks. An adversary in the vicinity could potentially sniff the communication traffic and obtain a file name that was previously accepted. They could then attempt to reuse/”replay” the file name and send a rogue file transfer request to Blue Pigeon. Similarly, a large-enough rogue file transfer could induce a temporary DoS against the setup.

To mitigate against this, file names will not be allowed to be reused. Blue Beak will ensure that the incoming file name has not been consumed before (by polling the `/sdcard/blue_pigeon/backup` folder).

In this base repo, the `rand` component of the filename is 32 hexadecimal digits/16 bytes long, which provides a collision rate of about 1 in 2^128 (trivial calculation, assuming good randomness).

The collision thresholds can be adjusted by customizing the filename structure and the verification scheme. The relevant codes reside in `com.csg.bluepigeon.util.EncryptionManager`.



\****If you have discovered any other bird-killing opportunities that we have overlooked and/or would like to contribute towards hardening Blue Pigeon, please feel free to create an issue or pull request!***









## Licensing and Contributing to Blue Pigeon

#### License

[GNU General Public License](/LICENSE) 

#### Have an idea to make Blue Pigeon better?

Feel free to create issues and send in pull requests!
