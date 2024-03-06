# Install and configure OR-Tools
1. Check openrao version in pom file
2. Navigate to that tag of this pom [file](https://github.com/powsybl/powsybl-open-rao/blob/main/pom.xml) in order to check for the needed OR-Tools version
3. Navigate to Google's release [here](https://github.com/google/or-tools/releases) and go to that given release
4. From its assets, download the java one for your OS
5. Extract it somewhere on your PC, and extract the jar to get from inside it the .so or .dll native library
6. In your app's environment variables, add the path to the native library files to LD_LIBRARY_PATH