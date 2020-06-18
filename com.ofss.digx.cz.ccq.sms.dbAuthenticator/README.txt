----------------------------------------------------------------------
Steps to follow :
----------------------------------------------------------------------
1. Copy the 'commo.dtd' and 'setWLSEnv.cmd' from the location - '$WL_HOME\server\lib' and '$WL_HOME\server\bin' respectively to the current directory.

2. Open the cmd in this directory and set the Weblogic environment first by executing the command - setWLSEnv.cmd

3. Run ant by executing the 'ant build' command.

4. Place the jar so created in the '$WL_HOME\server\lib\mbeantypes'. - OBDXDBAuthenticator.jar

5. After placing the jar file, restart the Weblogic server (Admin & Managed).

6. Add this authenticator in security realms with control flag as "SUFFICIENT"