Open Source project to create a simple Android widget for viewing PurpleAir air quality data. The primary use-case is looking
at your own devices (that is relevant to API usage limits - you aren't really limited when looking at your own sensors, I think).

PLEASE NOTE: This project is not affiliated with nor endorsed by PurpleAir.

## Running tests locally

Most tests are plain JVM unit tests and need nothing extra. A couple of tests hit the real
PurpleAir API to catch drift our unit tests can't (an expired key, a changed response shape,
etc.) -- see `PurpleAirIntegrationTest`. To run those locally, add your own PurpleAir credentials
to `local.properties` (gitignored, never committed):

```
purpleair.apiKey=your-purpleair-api-key
purpleair.sensorId=a-sensor-id-you-can-read
```

Without these set, the integration test(s) skip automatically rather than failing. The app
itself no longer uses a build-time API key or sensor ID at all -- each user enters their own via
the in-app setup flow (see `ApiKeyStore`) -- `local.properties`'s `purpleair.apiKey`/`sensorId`
are purely for local integration test runs. The sensor ID config screen intentionally starts
blank for everyone, dev included, so local testing matches the real user experience.


## TODO - Further iteration
- [ ] Add hamburger menu option to rate the app (probably need the app live to get a link for this)
- [ ] Long-pressing on the Contacts or TickTick widget gave me a Settings option... how can I get that too? If that's available, I'd prefer it over the gear icon.
- [ ] Add really robust handling of errors (like the sensor not getting data anymore, inability to connect to PurpleAir, etc.). We already have one case for SensorID not found.
- [ ] Add options for push notifications when the value changes past a certain threshold (ie: the first time you go over 100 without until you go back below 100 or 90 or something), etc..
- [ ] Consider using R8 for code-obfuscation (it's more like compression in practice). This creates a mapping file that Play Store gets so you can still see where errors are happening.
- [ ] Investigate if we can skip the API key config section and use our own backend or if that will cost money. On iOS Paku does that but it's got paid features, so they might be paying.
