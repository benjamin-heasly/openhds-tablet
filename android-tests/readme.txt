Getting Android instrumentation tests running was a huge pain!

I started with teh Android command line tool for creating a test project.  It didn't quire work as advertised.  I had
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
the maven-android plugin
                <configuration>
                    <sdk>
                        <platform>19</platform>
                    </sdk>
                    <test>
                        <instrumentationPackage>org.openhds.mobile.tests</instrumentationPackage>
                        <instrumentationRunner>android.test.InstrumentationTestRunner</instrumentationRunner>
                    </test>
                </configuration>

The values should match the values in the manifest of the test project.

See:
http://books.sonatype.com/mvnref-book/reference/android-dev-sect-test.html

It seems that all dependencies in the test project need to be declared to Maven as "provided" scope:
http://stackoverflow.com/questions/16132941/android-maven-instrumentation-tests-with-robotium-returns-illegalaccesserror

Finally this worked with a USB device attached.  The command completed and ran a test method that I wrote.

Here is an example Maven command:
cd openhds-tablet/android-tests
mvn -Dandroid.sdk.path="/home/optiplex-710-b/adt-bundle-linux-x86_64-20140321/sdk" -Dandroid.device=usb clean install

For now they android tests are in a separate maven project in the android-tests folder.  We should probably make this a
module of the main project so that the tests run automatically.  This should also allow the IDE to do magic like
auto-import, auto-complete, etc.  I think this would just boil down to Maven config, now that the basics are in place.

 --BSH