## Sample MMS Composer

This sample java application composes an MMS with a predefined structure and multimedia content consisting of two slides, each slide containing text, image and audio and then simply writes the encoded MMS to a file. The MMS multimedia content is stored in *"src/main/resources/multimedia"* folder of the java project, while output MMS file will be saved in *"src/main/resources/mms"* folder.

----

### Usage

<pre>
sample-mms-composer$ java.exe -Dfile.encoding=UTF-8 -classpath "target/classes;lib/MMSLibrary.jar" com.mms.composer.SampleMMSComposer &lt;address of the message sender> &lt;address of the message receiver> &lt;subject of the multimedia message>
</pre>

example :

<pre>
sample-mms-composer$ java.exe -Dfile.encoding=UTF-8 -classpath "target/classes;lib/MMSLibrary.jar" com.mms.composer.SampleMMSComposer "made by Aristotelis" +306900000000/TYPE=PLMN "This is a nice message ..."
</pre>

----

### Description

This sample application was derived from the *"OriginatingApp.java"* file which came with the *Nokia MMS Java Library 1.1*


Instead of sending the MMS to an MMSC, however, this sample application simply writes the encoded MMS to a file. That file can then be used, e.g. with various tools that are provided by Nokia -- the "EAIF" emulator, the "Series60" terminal emulator that comes with the "Series60" SDK for Symbian OS, the "7210" simulator, etc. It should be worth mentioning that this file can also be placed on a standard web server, with the *".MMS"* file extension mapped to the MIME *"content-type" : "application/vnd.wap.mms-message"* in the *"/etc/httpd/conf/httpd.conf"* as follows or similarly e.g. in the *"/etc/mime.types"*:

<pre>
   &lt;IfModule mime_module>
	    	:
		:
      AddType application/vnd.wap.mms-message .mms .MMS
   &lt;/IfModule>
</pre>

In this case, to send the MMS message, an *"MMS Notification"* message shall be sent to the recipient, referencing the URL from which the MMS message shall be retrieved. That is, according to the "MMS Client Transactions" document, the MMS client, upon receiving an *"m-notification.ind"* will issue an *"HTTP GET"* to the indicated URL. The MMS server will send back a typical *"HTTP 200 OK"* (if everything is OK) and in the body of that, it will send an *"m-retrieve-conf"* PDU in bytes (have a look also below at *"Important Note"*). The *"Content-Type"* HTTP header shall be *"application/vnd.wap.mms-message"*.

The sample application is made as simple as possible while still pointing out key points which are *not* explained in the samples that accompany the library. For instance, many of the headers that were set in the original sample were set to default values. Those have been left out here for simplification.

Basic gist of this sample application will be :
1) Set headers.
2) Add various content parts.
3) Encode the message.
4) Write the encoded message to a file.

----

### Important Note

The *Nokia MMS Java Library* creates compiled MMS messages of the type *"m-send-req"*. This format is the format that we use to submit a message to an MMSC. Unfortunately, if we try to send an *"MMS notification"* message out to ask a mobile phone to fetch one of these *"m-send-req"* messages, the mobile phone will reject the message with a *"Network Error Occurred"* error. However, as the format of the *"m-send-req"* and *"m-retrieve-conf"* messages is very similar, we can often just flip the second byte of the file to convert formats. Specifically, an *"m-send-req"* format file always starts with the bytes *"8C 80"*. An *"m-retrieve-conf"* format file always starts with the bytes *"8C 84"*. So, changing that second byte will often do the trick. This is actually how this sample application creates the output MMS file.

----

### Change History

* 25-09-2002 - Version 1.0 Document added into ForumNokia
* 21-12-2021 - Version 1.1 Updated by Aristotelis Metsinis

Copyright (c) Nokia Corporation 2002
