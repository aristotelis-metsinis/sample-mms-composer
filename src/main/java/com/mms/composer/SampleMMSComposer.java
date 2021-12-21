package com.mms.composer;

/*
 * @(#)SampleMMSComposer.java	1.1
 *
 * Summary
 *
 * This sample application composes an MMS with a predefined structure and multimedia content consisting of two slides,
 * each slide containing text, image and audio and then simply writes the encoded MMS to a file. The MMS multimedia content
 * is stored in "src/main/resources/multimedia" folder, while output MMS file will be saved in "src/main/resources/mms" folder.
 *
 * Usage
 *
 * 	 	mms_creation$ java.exe -Dfile.encoding=UTF-8 -classpath "target/classes;lib/MMSLibrary.jar" com.mms.composer.SampleMMSComposer <address of the message sender> <address of the message receiver> <subject of the multimedia message>
 * example :
 * 	 	mms_creation$ java.exe -Dfile.encoding=UTF-8 -classpath "target/classes;lib/MMSLibrary.jar" com.mms.composer.SampleMMSComposer "made by Aristotelis" +306900000000/TYPE=PLMN "This is a nice message ..."
 *
 * Description
 *
 * This sample application was derived from the "OriginatingApp.java" file which came with the
 * Nokia MMS Java Library 1.1
 *
 * Instead of sending the MMS to an MMSC, however, this sample application simply writes the
 * encoded MMS to a file. That file can then be used, e.g. with various tools
 * that are provided by Nokia -- the "EAIF" emulator, the "Series60" terminal emulator
 * that comes with the "Series60" SDK for Symbian OS, the "7210" simulator, etc.
 * It should be worth mentioning that this file can also be placed on a standard web server, with the ".MMS" file
 * extension mapped to the MIME "content-type" : "application/vnd.wap.mms-message" as follows :
 *    <IfModule mime_module>
 * 	    	:
 * 		    :
 *       AddType application/vnd.wap.mms-message .mms .MMS
 *   </IfModule>
 * In this case, to send the MMS message, an "MMS Notification" message shall be sent to the recipient, referencing the
 * URL from which the MMS message shall be retrieved. That is, according to the "MMS Client Transactions" document, the
 * MMS client, upon receiving an "m-notification.ind" will issue an "HTTP GET" to the indicated URL. The MMS server will
 * send back a typical "HTTP 200 OK" (if everything is OK) and in the body of that, it will send an "m-retrieve-conf"
 * PDU in bytes (have a look also below at "Important Note"). The "Content-Type" HTTP header shall be "application/vnd.wap.mms-message".
 *
 * The sample application is made as simple as possible while still pointing out key points
 * which are *not* explained in the samples that accompany the library.
 * For instance, many of the headers that were set in the original sample were set
 * to default values. Those have been left out here for simplification.
 *
 * Basic gist of this sample application will be :
 * 1) Set headers.
 * 2) Add various content parts.
 * 3) Encode the message.
 * 4) Write the encoded message to a file.
 *
 * Important Note
 *
 * The Nokia MMS Java Library creates compiled MMS messages of the type "m-send-req". This format is the format that we
 * use to submit a message to an MMSC. Unfortunately, if we try to send an MMS notification message out to ask a mobile
 * phone to fetch one of these "m-send-req" messages, the mobile phone will reject the message with a "Network Error Occurred"
 * error. However, as the format of the "m-send-req" and "m-retrieve-conf" messages is very similar, we can often just
 * flip the second byte of the file to convert formats. Specifically, an "m-send-req" format file always starts with the
 * bytes "8C 80". An "m-retrieve-conf" format file always starts with the bytes "8C 84". So, changing that second byte
 * will often do the trick. This is actually how this sample application creates the output MMS file.
 *
 * Change History
 * ----------------------------------------------------------------------------
 * * 25-09-2002 * Version 1.0 * Document added into ForumNokia                *
 * * 21-12-2021 * Version 1.1 * Updated by Aristotelis Metsinis               *
 * ----------------------------------------------------------------------------
 *
 * Copyright (c) Nokia Corporation 2002
 */

// These imports are necessary for utilities.
import java.io.*;
import java.util.Date;
import java.util.Random;

// Nokia MMS Java Library version 1.1
// The library provides examples of the most common tasks applications perform through the Multimedia Messaging
// Service Center (MMSC). Examples include such tasks as message creation and encoding, decoding
// and sending messages to the MMSC according to the WAP-209 MMS encapsulation specification and
// external application interface specification.
// References :
// * External Applications Developer’s Guide, DN00148759, Nokia Oyj
// * WAP-209-MMSEncapsulation-20010601-a, WAP Forum specification
import com.nokia.mms.*;

public class SampleMMSComposer {
  // Path where the resource multimedia contents are stored.
  private final String MULTIMEDIA_PATH = new File( "src/main/resources/multimedia" ).getAbsolutePath();
  // Path where the output MMS file shall be stored.
  private final String MMS_PATH = new File( "src/main/resources/mms" ).getAbsolutePath();

  /**
   * Compose a Multimedia Message. Application constructor.
   *
   * @param originator address of the message sender
   * @param recipient address of the message receiver
   * @param subject subject of the multimedia message
   */
  public SampleMMSComposer( String originator, String recipient, String subject ) {
    // The "MMMessage" class represents a Multimedia Message. It contains all the methods to set and
    // get the "mm-header" fields and to add the contents (represented by the "MMContent" class) included
    // in the body of the MM.
    MMMessage mms = new MMMessage();
    // Set Multimedia Message headers.
    SetHeaders( mms, originator, recipient, subject );
    // Add Multimedia Message various content parts to the body of the message.
    AddContents( mms );

    // The "MMEncoder" class encodes Multimedia Message object (MMMessage) into an array of bytes
    // according to the specification "WAP-209-MMSEncapsulation" (WAP Forum).
    // Instantiate an "encoder" object.
    MMEncoder encoder = new MMEncoder();
    // Set the message to be encoded.
    encoder.setMessage( mms );

    try {
      // Encode the Multimedia Message.
      encoder.encodeMessage();
      // Retrieve the buffer of bytes representing the encoded Multimedia Message.
      byte[] out = encoder.getMessage();
      // Print the encoded message to a file.
      createMmsFile( out, MMS_PATH + "/Sample.mms" );
    }
    catch ( Exception e ) {
      System.out.println( "An error occurred encoding or saving the Multimedia Message : " + e.getMessage() );
      e.printStackTrace();
    }
  }

  /**
   * Generate a random int with n digits.
   *
   * @param n number of digits
   * @return a random int with n digits
   */
  private int generateRandomDigits( int n ) {
    int m = (int) Math.pow( 10, n - 1 );
    return m + new Random().nextInt(9 * m );
  }

  /**
   * Set Multimedia Message headers.
   *
   * @param m multimedia message
   * @param sender address of the message sender
   * @param receiver address of the message receiver
   * @param subject subject of the multimedia message
   */
  private void SetHeaders( MMMessage m, String sender, String receiver, String subject ) {
    // In the "MMMessage" object we are going to set both mandatory and optional properties.
	// Just going to set the mandatory properties, and the "Subject".
	// "Type", "TransID", "Version" are all mandatory, and must be the first headers, in this order !
    // Set the type of the message (Mandatory). Specify the transaction type.
    // "IMMConstants" is an interface that includes all the constants that are useful for the treatment of a
    // multimedia message (MM).
    // Note : the "createMmsFile" method below contains a "hack" that finally converts the type of the message from "m-send-req"
    // to "m-retrieve-conf" since the Nokia MMS Java Library creates compiled MMS messages of the type "m-send-req".
	m.setMessageType( IMMConstants.MESSAGE_TYPE_M_SEND_REQ );
	// Set the transaction ID (Mandatory). It is a unique identifier for the message. It identifies the
    // "M-Send.req" and the corresponding reply only.
    m.setTransactionId( String.valueOf( generateRandomDigits(10 ) ) );
    // Set the MMS version number (Mandatory). According to the followed specification the version is 1.0.
    // "IMMConstants" is an interface that includes all the constants that are useful for the treatment of a
    // multimedia message (MM).
    m.setVersion( IMMConstants.MMS_VERSION_10 );

    // Set the sending time of the message at the "MMS Proxy-Relay" (Optional).
    // Note : in the "m-send-req" format, the "Date:" header is optional, while it is required in the "m-retrieve-conf"
    // format. Some MMS clients will refuse an MMS message if it does not have a "Date:" header.
    m.setDate( new Date( System.currentTimeMillis() ) );

    // Set the address of the message sender (Mandatory) - is the string representing the address of the sender.
    // It has to be specified in the full format i.e.: +306900000000/TYPE=PLMN or joe@user.org or 123.123.123.123/TYPE=IPv4.
    // (See WAP-209-MMSEncapsulation (WAP Forum)).
    m.setFrom( sender );
    // At least one "To", "CC", "BCC" is mandatory.
    // Add a new receiver of the Multimedia Message. The message can have more than one receiver but at least one - is the
    // string representing the address of the receiver. It has to be specified in the full format i.e.:
    // +306900000000/TYPE=PLMN or joe@user.org or 123.123.123.123/TYPE=IPv4. (See WAP-209-MMSEncapsulation (WAP Forum)).
    m.addToAddress( receiver );

    // Specify whether the user wants a delivery report from each recipient (Optional).
    m.setDeliveryReport( false );
    // Specify whether the user wants a read report from each recipient as a new message (Optional).
    m.setReadReply( false );
    // Specify whether the user wants sender visibility (Optional). "0x80" if the user wants the sender visibility
    // setting to "Hide". "0x81" if the user wants the sender visibility setting to "Show".
    // "IMMConstants" is an interface that includes all the constants that are useful for the treatment of a
    // multimedia message (MM).
    m.setSenderVisibility( IMMConstants.SENDER_VISIBILITY_SHOW );

    // Set the subject of the Multimedia Message (Optional).
    m.setSubject( subject );

    // Set the message class of the Multimedia Message (Optional) - is the message class. It can have one of the
    // following values: MESSAGE_CLASS_PERSONAL, MESSAGE_CLASS_ADVERTISEMENT, MESSAGE_CLASS_INFORMATIONAL,
    // MESSAGE_CLASS_AUTO
    // "IMMConstants" is an interface that includes all the constants that are useful for the treatment of a
    // multimedia message (MM).
    m.setMessageClass( IMMConstants.MESSAGE_CLASS_PERSONAL );
    // Set the priority of the message for the recipient (Optional). One of the following values: PRIORITY_LOW,
    // PRIORITY_NORMAL, PRIORITY_HIGH
    // "IMMConstants" is an interface that includes all the constants that are useful for the treatment of a
    // multimedia message (MM).
    m.setPriority( IMMConstants.PRIORITY_NORMAL );

    // "ContentType" is mandatory, and must be last header ! These last 3 lines set the "ContentType" to
    //    application/vnd.wml.multipart.related;type="application/smil";start="<0000>"
    // In case of "multipart.mixed", only the first line is needed (and change the constant).
    // However, in (our) case of multipart related message a "SMIL" presentation is available.
    // Set the content type of the Multimedia Message (Mandatory) - is the content type. The standard for interoperability
    // supports one of the following values: CT_APPLICATION_MULTIPART_MIXED, CT_APPLICATION_MULTIPART_RELATED
    // "IMMConstants" is an interface that includes all the constants that are useful for the treatment of a
    // multimedia message (MM).
    m.setContentType( IMMConstants.CT_APPLICATION_MULTIPART_RELATED );
    // Presentation part is written in "SMIL".
    // Set the type of the presentation part. Mandatory when the content type of the Multimedia Message is
    // CT_APPLICATION_MULTIPART_RELATED - the type of the presentation part. The standard for interoperability supports
    // only the value: CT_APPLICATION_SMIL
    // "IMMConstants" is an interface that includes all the constants that are useful for the treatment of a
    // multimedia message (MM).
    m.setMultipartRelatedType( IMMConstants.CT_APPLICATION_SMIL );
    // Any string will do for the "Content-ID", but it must match that used for the presentation part,
    // and it must be "Content-ID", it cannot be "Content-Location".
    // Presentation part has "Content-ID=<0000>", i.e. the "id" of the content containing the "SMIL" presentation.
    // Set the content ID of the content containing the presentation part of the Multimedia Message. Mandatory when the
    // content type of the Multimedia Message is CT_APPLICATION_MULTIPART_RELATED
    m.setPresentationId( "<0000>" );
  }

  /**
   * Add Multimedia Message various content parts to the body of the message.
   *
   * @param m multimedia message
   */
  private void AddContents( MMMessage m ) {
    // This is where the majority of the work is done. Note that here we are adding the parts of the
    // message in the order we want them to appear. Actually the presentation part specifies that,
    // but in terminals which cannot understand the presentation part, the order may be significant,
    // so there seems to be no reason to use random order.

    // Note also, that the current version (1.1) of the library encodes the message in some sort
    // of random order, so developers must either fix that problem using the source code, or
    // be prepared for random order output.

    // Add "SMIL" content.
    // The "MMContent" class represents a generic entry of a Multimedia Message. Creates the object representing the content.
    // It contains methods to set and get the array of bytes representing the content, to set and get the content type
    // and to save the content into a binary file. The "MMContent" object provides the methods to set the content and its properties.
    MMContent smil_part = new MMContent();
    // Read the "multimedia" file with "readFile()" that is a function that reads a file and
    // returns an array of bytes.
    byte[] b1 = readFile(MULTIMEDIA_PATH + "/HelloWorld.smil" );
    // Write bytes from the specified byte array starting at a specific offset.
    smil_part.setContent( b1, 0, b1.length );
    // If "<>" are used with this method, the result is "Content-ID".
    // Set the ID of the entry.
    smil_part.setContentId( "<0000>" );
    // Set the type of the entry - valid content types : See WAP-203-WSP (WAP Forum) (Table 40). Examples are : text/plain,
    // image/jpeg, image/gif, etc. use also some constants like: CT_TEXT_HTML, CT_TEXT_PLAIN, CT_TEXT_WML, CT_IMAGE_GIF,
    // CT_IMAGE_JPEG, CT_IMAGE_WBMP, CT_APPLICATION_SMIL, etc.
    // "IMMConstants" is an interface that includes all the constants that are useful for the treatment of a
    // multimedia message (MM).
    smil_part.setType( IMMConstants.CT_APPLICATION_SMIL );
    // Add a content to the message.
    m.addContent( smil_part );

    // Add "slide1" text.
    // The "MMContent" class represents a generic entry of a Multimedia Message. Creates the object representing the content.
    // It contains methods to set and get the array of bytes representing the content, to set and get the content type
    // and to save the content into a binary file. The "MMContent" object provides the methods to set the content and its properties.
    MMContent s1_text = new MMContent();
    // Read the "multimedia" file with "readFile()" that is a function that reads a file and
    // returns an array of bytes.
    byte[] b2 = readFile(MULTIMEDIA_PATH + "/HelloWorld.txt" );
    // Write bytes from the specified byte array starting at a specific offset.
    s1_text.setContent( b2,0, b2.length );
    // If "<>" are not used with this method, the result is "Content-Location".
    // Set the ID of the entry.
    s1_text.setContentId( "HelloWorld.txt" );
    // Set the type of the entry - valid content types : See WAP-203-WSP (WAP Forum) (Table 40). Examples are : text/plain,
    // image/jpeg, image/gif, etc. use also some constants like: CT_TEXT_HTML, CT_TEXT_PLAIN, CT_TEXT_WML, CT_IMAGE_GIF,
    // CT_IMAGE_JPEG, CT_IMAGE_WBMP, CT_APPLICATION_SMIL, etc.
    // "IMMConstants" is an interface that includes all the constants that are useful for the treatment of a
    // multimedia message (MM).
    s1_text.setType( IMMConstants.CT_TEXT_PLAIN );
    // Add a content to the message.
    m.addContent( s1_text );

    // Add "slide1" image.
    // The "MMContent" class represents a generic entry of a Multimedia Message. Creates the object representing the content.
    // It contains methods to set and get the array of bytes representing the content, to set and get the content type
    // and to save the content into a binary file. The "MMContent" object provides the methods to set the content and its properties.
    MMContent s1_image = new MMContent();
    // Read the "multimedia" file with "readFile()" that is a function that reads a file and
    // returns an array of bytes.
    byte[] b3 = readFile(MULTIMEDIA_PATH + "/SmileyFace.gif" );
    // Write bytes from the specified byte array starting at a specific offset.
    s1_image.setContent( b3,0, b3.length );
    // If "<>" are not used with this method, the result is "Content-Location".
    // Set the ID of the entry.
    s1_image.setContentId( "SmileyFace.gif" );
    // Set the type of the entry - valid content types : See WAP-203-WSP (WAP Forum) (Table 40). Examples are : text/plain,
    // image/jpeg, image/gif, etc. use also some constants like: CT_TEXT_HTML, CT_TEXT_PLAIN, CT_TEXT_WML, CT_IMAGE_GIF,
    // CT_IMAGE_JPEG, CT_IMAGE_WBMP, CT_APPLICATION_SMIL, etc.
    // "IMMConstants" is an interface that includes all the constants that are useful for the treatment of a
    // multimedia message (MM).
    s1_image.setType( IMMConstants.CT_IMAGE_GIF );
    // Add a content to the message.
    m.addContent( s1_image );

    // Add "slide1" audio.
    // The "MMContent" class represents a generic entry of a Multimedia Message. Creates the object representing the content.
    // It contains methods to set and get the array of bytes representing the content, to set and get the content type
    // and to save the content into a binary file. The "MMContent" object provides the methods to set the content and its properties.
    MMContent s1_audio = new MMContent();
    // Read the "multimedia" file with "readFile()" that is a function that reads a file and
    // returns an array of bytes.
    byte[] b4 = readFile(MULTIMEDIA_PATH + "/HelloWorld.amr" );
    // Write bytes from the specified byte array starting at a specific offset.
    s1_audio.setContent( b4,0, b4.length );
    // If "<>" are not used with this method, the result is "Content-Location".
    // Set the ID of the entry.
    s1_audio.setContentId( "HelloWorld.amr" );
    // Note how to use "mime-types" with no pre-defined constant !
    // Set the type of the entry - valid content types : See WAP-203-WSP (WAP Forum) (Table 40). Examples are : text/plain,
    // image/jpeg, image/gif, etc. use also some constants like: CT_TEXT_HTML, CT_TEXT_PLAIN, CT_TEXT_WML, CT_IMAGE_GIF,
    // CT_IMAGE_JPEG, CT_IMAGE_WBMP, CT_APPLICATION_SMIL, etc.
    s1_audio.setType( "audio/amr" );
    // Add a content to the message.
    m.addContent( s1_audio );

    // "Add slide2" text.
    // The "MMContent" class represents a generic entry of a Multimedia Message. Creates the object representing the content.
    // It contains methods to set and get the array of bytes representing the content, to set and get the content type
    // and to save the content into a binary file. The "MMContent" object provides the methods to set the content and its properties.
    MMContent s2_text = new MMContent();
    // Read the "multimedia" file with "readFile()" that is a function that reads a file and
    // returns an array of bytes.
    byte[] b5 = readFile(MULTIMEDIA_PATH + "/TheEnd.txt" );
    // Write bytes from the specified byte array starting at a specific offset.
    s2_text.setContent( b5,0, b5.length );
    // Here, again, we are using "Content-ID" - just for demonstration.
    // Set the ID of the entry.
    s2_text.setContentId( "<TheEnd.txt>" );
    // Set the type of the entry - valid content types : See WAP-203-WSP (WAP Forum) (Table 40). Examples are : text/plain,
    // image/jpeg, image/gif, etc. use also some constants like: CT_TEXT_HTML, CT_TEXT_PLAIN, CT_TEXT_WML, CT_IMAGE_GIF,
    // CT_IMAGE_JPEG, CT_IMAGE_WBMP, CT_APPLICATION_SMIL, etc.
    // "IMMConstants" is an interface that includes all the constants that are useful for the treatment of a
    // multimedia message (MM).
    s2_text.setType( IMMConstants.CT_TEXT_PLAIN );
    // Add a content to the message.
    m.addContent( s2_text );

    // Add "slide2" image.
    // The "MMContent" class represents a generic entry of a Multimedia Message. Creates the object representing the content.
    // It contains methods to set and get the array of bytes representing the content, to set and get the content type
    // and to save the content into a binary file. The "MMContent" object provides the methods to set the content and its properties.
    MMContent s2_image = new MMContent();
    // Read the "multimedia" file with "readFile()" that is a function that reads a file and
    // returns an array of bytes.
    byte[] b6 = readFile(MULTIMEDIA_PATH + "/TheEnd.gif" );
    // Write bytes from the specified byte array starting at a specific offset.
    s2_image.setContent( b6,0, b6.length );
    // Here, again, we are using "Content-ID" - just for demonstration.
    // Set the ID of the entry.
    s2_image.setContentId( "<TheEnd.gif>" );
    // Set the type of the entry - valid content types : See WAP-203-WSP (WAP Forum) (Table 40). Examples are : text/plain,
    // image/jpeg, image/gif, etc. use also some constants like: CT_TEXT_HTML, CT_TEXT_PLAIN, CT_TEXT_WML, CT_IMAGE_GIF,
    // CT_IMAGE_JPEG, CT_IMAGE_WBMP, CT_APPLICATION_SMIL, etc.
    // "IMMConstants" is an interface that includes all the constants that are useful for the treatment of a
    // multimedia message (MM).
    s2_image.setType( IMMConstants.CT_IMAGE_GIF );
    // Add a content to the message.
    m.addContent( s2_image );

    // Add "slide2" audio.
    // The "MMContent" class represents a generic entry of a Multimedia Message. Creates the object representing the content.
    // It contains methods to set and get the array of bytes representing the content, to set and get the content type
    // and to save the content into a binary file. The "MMContent" object provides the methods to set the content and its properties.
    MMContent s2_audio = new MMContent();
    // Read the "multimedia" file with "readFile()" that is a function that reads a file and
    // returns an array of bytes.
    byte[] b7 = readFile(MULTIMEDIA_PATH + "/YallComeBackNowYaHear.amr" );
    // Write bytes from the specified byte array starting at a specific offset.
    s2_audio.setContent( b7,0, b7.length );
    // Note that the filename and "Content-ID" don't need to be the same.
    // Set the ID of the entry.
    s2_audio.setContentId( "<YCBNYH.amr>" );
    // Note how to use "mime-types" with no pre-defined constant !
    // Set the type of the entry - valid content types : See WAP-203-WSP (WAP Forum) (Table 40). Examples are : text/plain,
    // image/jpeg, image/gif, etc. use also some constants like: CT_TEXT_HTML, CT_TEXT_PLAIN, CT_TEXT_WML, CT_IMAGE_GIF,
    // CT_IMAGE_JPEG, CT_IMAGE_WBMP, CT_APPLICATION_SMIL, etc.
    s2_audio.setType( "audio/amr" );
    // Add a content to the message.
    m.addContent( s2_audio );
  }

  /**
   * Read the "multimedia" file with "readFile()" that is a function that reads a file and
   * returns an array of bytes.
   *
   * @param filename name of the "multimedia" file
   * @return array of bytes
   */
  private byte[] readFile( String filename ) {
    int fileSize = 0;
    RandomAccessFile fileH = null;

    // Open the file for reading.
    try {
      // Create a random access file stream to read from with the specified name. A new "FileDescriptor" object is created
      // to represent the connection to the file.
      fileH = new RandomAccessFile( filename, "r" );
      // Return the length of this file.
      fileSize = (int) fileH.length();
    }
    catch ( IOException ioErr ) {
      System.err.println( "Cannot find : \"" + filename + "\"" );
      System.err.println( ioErr );

      // Exit immediately in case of error opening the file.
      System.exit(200 );
    }

    // Allocate the buffer large enough to hold entire file.
    byte[] buf = new byte[ fileSize ];

    // Read all bytes of file.
    int i = 0;
    try {
       while ( true ) {
         try {
           // Reads a byte from the file, starting from the current file pointer.
           buf[ i++ ] = fileH.readByte();
         }
         catch ( EOFException e ) {
           // Break in case of error reading the file.
           break;
         }
       }
    }
    catch ( IOException ioErr ) {
      System.out.println( "ERROR in reading of file : \"" + filename + "\"" );
    }

    // Return the array of bytes.
    return buf;
  }

  /**
   * Print the encoded message to a file.
   * This file shall be placed on a standard web server, with the ".MMS" file extension mapped to the
   * MIME "content-type" : "application/vnd.wap.mms-message" as follows :
   *
   *    <IfModule mime_module>
   * 	    	:
   * 		    :
   *        AddType application/vnd.wap.mms-message .mms .MMS
   *    </IfModule>
   *
   * To send the MMS message, an "MMS Notification" message shall be sent to the recipient, referencing the URL from which
   * the MMS message shall be retrieved. That is, according to the "MMS Client Transactions" document, the MMS client,
   * upon receiving an "m-notification.ind" will issue an HTTP GET to the indicated URL. The MMS server will send back a
   * typical "HTTP 200 OK" (if everything is OK) and in the body of that, it will send an "m-retrieve-conf" PDU in bytes.
   * The "Content-Type" HTTP header shall be "application/vnd.wap.mms-message".
   *
   * @param output buffer of bytes representing the encoded multimedia message
   * @param filename name of the encoded multimedia message file
   */
  public void createMmsFile( byte[] output, String filename ) {
	try	{
	  // Create a new "File" instance by converting the given pathname string into an abstract pathname.
	  File f = new File( filename );
	  // Create a file output stream to write to the file represented by the specified "File" object. A new "FileDescriptor"
      // object is created to represent this file connection.
	  FileOutputStream out = new FileOutputStream( f );

	  // Hack : the Nokia MMS Java Library creates compiled MMS messages of the type "m-send-req". This format is the
      // format that we use to submit a message to an MMSC. Unfortunately, if we try to send an MMS notification message
      // out to ask a mobile phone to fetch one of these "m-send-req" messages, the mobile phone will reject the message
      // with a "Network Error Occurred" error. However, as the format of the "m-send-req" and "m-retrieve-conf" messages
      // is very similar, we can often just flip the second byte of the file to convert formats. Specifically, an "m-send-req"
      // format file always starts with the bytes "8C 80". An "m-retrieve-conf" format file always starts with the bytes "8C 84".
      // So, changing that second byte will often do the trick.
	  output[ 1 ] = (byte) 0x84;

	  // Write bytes from the specified byte array to the file output stream.
  	  out.write( output );
  	  // Close the file output stream and release any system resources associated with this stream.
	  out.close();
	}
	catch ( Exception e )	{
	  System.out.println( e.getMessage() );
	}
  }

  /**
   * Sample MMS Composer application
   *
   * @param args application's input arguments
   */
  public static void main ( String[] args ) {
    String originator = args[0];
    String recipient = args[1];
    String subject = args[2];

    // When the application is executed, a new "SampleMMSComposer" object is instantiated.
    // The application requires three input arguments :
    // * the address of the message sender
    // * the address of the message receiver
    // * the subject of the multimedia message
    new SampleMMSComposer( originator, recipient, subject );
  }

}
