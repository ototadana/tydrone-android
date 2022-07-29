# tydrone-android-plugin

Unity Plugin for Android Application to
use [Tello Low-Level Protocol](https://tellopilots.com/wiki/protocol/).

This is for running [tello-llp-wrapper](https://github.com/ototadana/tello-llp-wrapper) on android
devices.

## Requirement

Android 10. This plugin has been tested only with Oculus Quest 2.

## Setup

1. Download `tello-llp-wrapper-*.jar`
   from [this page](https://github.com/ototadana/tello-llp-wrapper/releases).
2. Copy `tello-llp-wrapper-*.jar` to the `./tydrone-android-plugin/libs` folder. If the folder does
   not exist, create it.
3. Download [javacv-platform-1.5.7-bin.zip](https://github.com/bytedeco/javacv/releases/tag/1.5.7)
4. Create a small jar file using a script like the following:
   ```shell
   # unzip the downloaded file
   unzip javacv-platform-1.5.7-bin.zip

   # cd to the destination folder of the zip file
   cd javacv-platform-1.5.7-bin

   # create a working directory
   mkdir tmp

   # copy important files
   cp LICENSE.txt tmp
   cp README.md tmp
   cp CHANGELOG.md tmp

   # extract jar files to the tmp directory
   cd tmp
   jar xf ../ffmpeg.jar
   jar xf ../ffmpeg-android-arm64.jar
   jar xf ../javacpp.jar
   jar xf ../javacpp-android-arm64.jar
   jar xf ../javacv.jar
   jar xf ../openblas.jar
   jar xf ../openblas-android-arm64.jar
   jar xf ../opencv.jar
   jar xf ../opencv-android-arm64.jar

   # remove some files to avoid build errors
   rm -fr META-INF/versions
   rm -fr META-INF/native-image
   
   # create jar file
   jar cf javacv-1.5.7.jar *
   ```
5. Copy the created jar file to the `./tydrone-android-plugin/libs` folder.
6. Copy `classes.jar` from Unity to the `./tydrone-android-plugin/dev-libs` folder.
7. Build **tydrone-android-plugin** module.
8. Copy `./tydrone-android-plugin/build/outputs/aar/tydrone-android-plugin-*.aar`
   to `Assets/Plugins/Android` folder of your Unity Project.

## Usage

The [Facade interface of the tello-llp-wrapper](https://github.com/ototadana/tello-llp-wrapper#usage)
can be used in Unity scripts by creating a simple wrapper class like the following:

```csharp
public class TyDroneAndroidPlugin
{
    private AndroidJavaObject tyDrone;

    public TyDroneAndroidPlugin()
    {
        this.tyDrone = new AndroidJavaObject("com.xpfriend.tydrone.AndroidMain");
    }

    public void EntryCommand(string command)
    {
        tyDrone.Call("entryCommand", command);
    }

    public string GetNotice()
    {
        return tyDrone.Call<string>("getNotice");
    }

    public string GetSentCommand()
    {
        return tyDrone.Call<string>("getSentCommand");
    }

    public string GetStates()
    {
        return tyDrone.Call<string>("getStates");
    }

    public bool IsRecording()
    {
        return tyDrone.Call<bool>("isRecording");
    }

    public byte[] PickImage()
    {
        AndroidJavaObject obj = this.tyDrone.Call<AndroidJavaObject>("pickImage");
        if (obj == null || obj.GetRawObject() == null)
        {
            return new byte[] { };
        }

        sbyte[] image = AndroidJNIHelper.ConvertFromJNIArray<sbyte[]>(obj.GetRawObject());
        if (image == null)
        {
            return new byte[] { };
        }
        return (byte[])(Array)image;
    }

    public void SetRecording(bool value)
    {
        this.tyDrone.Call("setRecording", value);
    }

    public void Run()
    {
        this.tyDrone.Call("run");
    }
}
```

For example, you can apply the video image obtained from Tello to Texture2D as follows:

```csharp
byte[] bytes = tyDrone.PickImage();
if (bytes.Length == 0)
{
   return;
}

Texture2D texture = new Texture2D(960, 720, TextureFormat.RGB24, false);
texture.LoadRawTextureData(bytes);
texture.Apply();
```

See [README.md of the tello-llp-wrapper](https://github.com/ototadana/tello-llp-wrapper) for
information on the use of other methods.

## License

This software is released under the MIT License, see [LICENSE](./LICENSE).

For dependent software licenses, see:

- https://github.com/ototadana/tello-llp-wrapper
- https://github.com/bytedeco/javacv