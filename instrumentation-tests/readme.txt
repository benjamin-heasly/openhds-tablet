Getting Android instrumentation tests running was a huge pain!

-- Basic Setup --

I started with the Android command line tool for creating a test project.  It didn't quire work as advertised.  I had
to create the folder for the test project first, then run the command from inside.  Something like

cd openhds-tablet
mkdir android-tests
~/adt-bundle-linux-x86_64-20140321/sdk/tools/android create test-project -m ".." -n AndroidTests -p "."

See:
http://developer.android.com/tools/testing/testing_android.html#TestProjects
http://developer.android.com/tools/testing/testing_otheride.html#UpdateTestProject

The above assume we want to use the command line or and to build.  But we want to use Maven.  This old page had helpful
advice:
http://code.google.com/p/maven-android-plugin/wiki/AutomateAndroidTestProject

The suggested pom.xml needed updating.  We need to specify the package and "instrumentation" class in the pom, for
the maven-android plugin.
                <configuration>
                    <sdk>
                        <platform>19</platform>
                    </sdk>
                    <test>
                        <instrumentationPackage>org.openhds.mobile.tests</instrumentationPackage>
                        <instrumentationRunner>android.test.InstrumentationTestRunner</instrumentationRunner>
                    </test>
                </configuration>


The values should match the values in the Android Manifest of the test project.

See:
http://books.sonatype.com/mvnref-book/reference/android-dev-sect-test.html

It seems that all dependencies in the test project need to be declared to Maven as "provided" scope:
http://stackoverflow.com/questions/16132941/android-maven-instrumentation-tests-with-robotium-returns-illegalaccesserror

-- Separate Test Project --
I rearranged the above to use two separate Maven projects: one for the application itself, and one for the
instrumentation tests.  This allows us to use a parent project that builds and deploys both projects automatically.
This is important because each project needs to be deployed as an .apk on the tablet, and we want to make sure changes
to the application and the tests stay in sync.

See:
http://maven.apache.org/guides/mini/guide-multiple-modules.html

Here is an example Maven command for a full test run with uninstall and reinstall of both apks:
cd openhds-tablet
mvn -Dandroid.sdk.path="/home/motech/android-sdk-linux" -Dandroid.device=usb clean install

Running this from the command line makes it hard to attach the debugger.  But after you run this command once, you can
then debug the app or tests like you would normally, from Eclipse or IntelliJ.  Debug and make changes, and re-run the
full Maven build from the command line when you need to makes sure the app and tests are in sync.

Also, sometimes the Maven command fails with "Run failed: Instrumentation run failed due to 'Process crashed.'".
I haven't figured out why.  But often it works just to re-run the command.

 --BSH
