# Filament Spool Manager Interface 

This project is made as an example of interface with the Android app "Filament Spool Manager":

[Filament Spool Manager on Google Play](https://play.google.com/store/apps/details?id=com.printing3d.spoolsmanager)

Filament Spool Manager allows you to manage your stock of filaments for your 3D printer(s).

With the interface (as an AIDL file), you will be able to communicate a filament lenght (or weight) input to Filament Spool Manager.

## Pre-requisited

Filament Spool Manager is compatible with **API level 16+** (Jelly Bean)

## How does it work

It is very easy. In your own app, include the .aidl interface file in your /src/main/ folder. Don't change the package name (see in the sample app). It should be located at : 
**src/main/aidl/com/printing3d/spoolsmanager/IInputConsumptionService.aidl** :

![path to aidl file](https://image.ibb.co/gUMj28/Screenshot_from_2018_06_20_20_59_50.png)

In this interface, you have one (and only one for the moment) method to call:

``` java
    boolean selectSpoolAndInput(String label, float length, float weight);
```
This will open the Filament Spool Manager on a spool selector screen to input the value you've passed.

Please note that:

* You should provide at least a value > 0 to length or weight
* If you want to specify only weight for the consumption input, then set the length to 0
* If you want to specify only length for the consumption input, then set the weight to 0
* If you specify both length and weight values, the length is the priority.
* Lenght is in meters (m) and weight is in grams (g)

## How to connect to the service

First you have to ensure that Filament Spool Manager is properly installed on the device. Otherwise you wont be able to connect to the IPC, of course.

And don't forget that Filament Spool Manager is compatible with Android API level 16+. The app should be not installed with Android version below API 16, but it can be a great idea to check the compatibility before.

Create your remote service connection object:

``` kotlin
    private var service: IInputConsumptionService? = null

    private inner class RemoteServiceConnection: ServiceConnection {
        // Service is connected
        override fun onServiceConnected(name: ComponentName, boundService: IBinder) {
            service = IInputConsumptionService.Stub.asInterface(boundService)
        }

        // Service is disconnected
        override fun onServiceDisconnected(name: ComponentName) {
            service = null
        }
    }
```

Instantiate the connection with the service:

``` kotlin
    private var serviceConnection: RemoteServiceConnection? = null

    private fun connectService() {
        serviceConnection = RemoteServiceConnection()

        // Create service intent
        val intent = Intent()
        intent.setClassName(AIDL_PACKAGE, AIDL_CLASSNAME)
        intent.action = REMOTE_ACTION

        // Bind to service
        val ret = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
```

Here are the constant values, do not change them in your project:


``` kotlin
  /**
   * The remote action ID. Do not change this value.
   */
  private const val REMOTE_ACTION = "com.printing3d.spoolsmanager.InputConsumptionService.BIND"

  /**
   * Remote service package. Do not change this value.
   */
  private const val AIDL_PACKAGE = "com.printing3d.spoolsmanager"

  /**
   * Remote service location. Do not change this value.
   */
  private const val AIDL_CLASSNAME = "fr.appbase.app.base.ipc.InputConsumptionService"
```

And finally, send the input value to Filament Spool Manager:

``` kotlin
    private fun sendInput() {
        try {
            val lengthLabel: String = [...]
            val lengthInput: Float = [...]

            if (lengthInput <= 0) {
                Toast.makeText(this@DemoActivity,
                        "Length input should be positive and != 0", Toast.LENGTH_LONG).show()
                return
            }
            
            // Send the input value to the service
            service?.selectSpoolAndInput(lengthLabel, lengthInput, 0f)

        } catch (e: NumberFormatException) {
            Toast.makeText(this@DemoActivity, "Invalid number format", Toast.LENGTH_LONG).show()
        }
    }
```

 ## Screenshots
 
 When you input a filament length (or weight) remotely from a third-party application, Filament Spool Manager opens the list of available spools and allows you to input the value on the spool of your choice.
 
Once the spool is updated, Filament Spool Manager will close itself.
 
![Select spool for input](https://image.ibb.co/hdHhN8/Screenshot_1529524889_framed.png)

![Input value on selected spool](https://preview.ibb.co/kjKkaT/Screenshot_1529524895_framed.png)

## License

```
Copyright 2015 Square, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
