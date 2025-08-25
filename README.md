**Introduction**
This project was created with the intention of demonstrating what a social engineering attack would look like in educational terms, in which one company impersonates another and sends emails to specific people, leading them to
believe that they would earn money if they used the application attached to the email. A person with limited resources or who is financially broke and lacks knowledge of digital security
could fall for this.
The purpose of this audit is to explain how the keylogger integrated into the Android app “CalcLab” works, its code structure, and the communication between its components.

This keylogger has two main mechanisms for capturing data entered by the user:
1. Direct access to the physical keyboard hardware via /dev/input/event2 on rooted Android devices.
2. Use of an Accessibility Service to capture text typed in any application without requiring root permissions.
**LIMITATION:** ONLY works on Android 12 and below, since Android 13 does not allow special permissions to be given to APKs downloaded from unknown sources.

**Timeline of operation and communication between files**
The following is a step-by-step description of how the files interact with each other to capture and send keystrokes.

**1. Compilation of the native library (keylogger.c)**
**File involved:** CMakeLists.txt
This file defines the configuration for compiling the C code (keylogger.c) into a native .so library. The compilation generates libkeylogger.so, which will be integrated into the 
Android application.
Kotlin will load this library at runtime with:
System.loadLibrary(“keylogger”)
Result: A native C module capable of interacting directly with the device's hardware is obtained.

**2. Capturing keystrokes using native code (keylogger.c)**
**File involved:** keylogger.c
How it works:
1. The input device /dev/input/event2 is opened.
2. A thread (pthread_t) is created to read keyboard events in the background.
3. Each time a key is pressed, the following is executed:
Java_com_agenciacristal_calculadora_Keylogger_sendKeyDataToServer(env, globalObj, event.code);
This method sends the keystroke to the Kotlin code.
4. Finally, the keystroke is sent to the Flask server for storage.
Result: The keylogger directly captures keystrokes on devices with root access.

**3. Communication between C and Kotlin using JNI (Keylogger.kt)**
**File involved:** Keylogger.kt
How it works: The native function is defined in Kotlin:
external fun startLogging()
sendKeyDataToServer(keyCode: Int) is implemented, which:
1. Converts the keyCode to JSON.
2. Sends it to a Flask server via an HTTP POST request.
Result: Keyboard keystrokes detected in C are communicated to Kotlin, where they are then sent to the server.

**4. Text capture using an Accessibility Service (KeyLoggerAccessibilityService.kt)**
**File involved:** KeyLoggerAccessibilityService.kt
How it works: It registers as an Accessibility Service on Android. It captures TYPE_VIEW_TEXT_CHANGED events in any app. When it detects a change in the text entered,
it sends it to the Flask server.
Result: This method allows text to be recorded without the need for root access, making it more effective on most devices.

**5. Activating the keylogger from the application (MainActivity.kt)**
**File involved:** MainActivity.kt
Functionality: Checks whether the Accessibility Service is enabled. If not, it requests activation from the user. Keylogger.startLogging() is executed to start keystroke capture.
Result: The keylogger is activated when the application is opened, without the user suspecting its operation.

**6. Data storage on the Flask server**
**Destination:** https://flask-server-xysa.onrender.com/log
Both the C keylogger and the accessibility service send data to this server. The server stores keystrokes for later analysis.
Result: User data collection is centralized on an external server.

**Audit conclusion**
- The keylogger has two methods of data capture: one based on root access (/dev/input/event2) and another based on accessibility services.
- It integrates silently into a legitimate application, making it highly evasive.
- The collected data is sent to an external Flask server without the user noticing.
- The use of the accessibility service allows it to function without the need for root permissions, increasing its effectiveness.
