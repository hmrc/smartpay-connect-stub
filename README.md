
# smartpay-connect-stub
This stub supports the Face to Face service, and is designed to behave like a PED (Pin enabled device). The PED model that is being used on the service is the Lane 3000.  

### How to run

The stub must be run on your local machine. You can do this via service manager.

Warning: You will also need to run stubs locally if you want to use a stubbed test environment. This is due to the frontend service using websockets - which are not supported on the MDTP platform.

#### Service manager

`sm --start SMARTPAY_CONNECT_STUB`

### How to configure stub path

1. Go to http://localhost:9263/smartpay-connect-stub/set-path-for-device.
2. Select the scenario from the dropdown
3. Click submit
4. Start a face to face journey by going to http://localhost:9260/face-to-face/start

If you do not set the stub path, the default is 'success with chip@pin' scenario.

When you want to select a different path, you will need to set it each time by following the configuration steps above.