# ChatYoXMPP-Android
**Description:**
'ChatYoXMPP' is instant chat messaging app using XMPP(openfire). XMPP(Extensible Messaging and Presence Protocole) (https://xmpp.org/about/) is a communications protocol for real-time comunications oriented middleware based on XML. I am using MVP architecture pattern for code in Android app. The user need to be pre-registed on server. App allows to login with host, jabberId(without suffix of hostname) and password.
'ChatYoXMPP' currently only trasfers text messages. In future more features will be added like- user-registration, media trasfer and group chat. 


**Installation:**
Assuming that [openfire server](http://mindbowser.com/openfire-installation-and-database-configuration/) is installed on your server. We are using Smack XMPP client library. Smack is open-source, easy to use and written in Java for Java SE compatible JVMs and Android. Smack and XMPP allows you to easily exchange data, in various ways.
Add dependency in build.gradle. For latest version you can check [here](http://www.igniterealtime.org/downloads/)

    ```implementation 'org.igniterealtime.smack:smack-android:4.1.3'
    // Optional for XMPP extensions support
    implementation 'org.igniterealtime.smack:smack-android-extensions:4.1.3'
    // Optional for XMPPTCPConnection
    implementation 'org.igniterealtime.smack:smack-tcp:4.1.3'```

I am using 'Anko' for small async operations in app, you can use as your choice (AsyncTast etc.).

    ```implementation 'org.jetbrains.anko:anko-common:0.9'```

**Usage:**
The purpose of the app is to help new developers to integrate XMPP based messaging in thier app. The app is simple to use.
