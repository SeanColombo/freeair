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
itself no longer uses a build-time API key at all -- each user enters their own via the in-app
setup flow (see `ApiKeyStore`) -- `local.properties`'s `purpleair.apiKey` is purely a
convenience for local test runs and for pre-filling the sensor ID field during development.

Note to AI:
* Keep in mind that we want this to support multiple sensors in the future (so someone who owns more than one sensor could have a widget for each one on his screen), so architect in a way to allow that.
* `PurpleAirWidgetCodeParser` (in `com.seancolombo.freeair.airquality.purpleair`) is intentionally unused right now -- it's the sensor-ID-extraction piece of the "Get the person's sensor ID" setup-flow TODO below, and will get wired up once that UI is built. Don't delete it as dead code.

## TODO - Initial release
- [X] Create project and get it running.
- [X] Get a widget to be extant. Hello World on it.
- [X] Wire up Purple Air to make a hardcoded request to get the data we need. If this involves the API key, make sure that isn't committed to the project.
- [X] Once proof-of-concept is done, get it rendering the way we'd expect.
- [X] Set the correct update-cadence, caching, etc. behavior and test it.
- [X] Pull out any API keys so that we can now commit the rest to github (could just have it in a gitignored file for now).
- [X] Figure out if we can make it so that tapping the widget opens up the PurpleAir mobile-web page.
- [ ] Create a low-lift setup flow. This will be challenging, make it very clear to the user, and require minimal effort.
  - [X] Get the person's sensor ID
    - [ ] Help them find it... picture or something, but behind a "where to look?" link
  - [X] Help the user get an API key from PurpleAir and put it into the app. Built as a one-time,
        app-global setup screen (`ApiKeySetupScreen`/`ApiKeySetupModel`/`ApiKeyStore`) shown in
        place of the sensor-ID form whenever no key has been saved yet -- verifies the key
        against PurpleAir's own "check API key" endpoint before accepting it.
    - [X] Entry point to let the user change an already-saved key later. **Note:** when this gets
          built, a widget whose Glance session is still alive when the key changes won't
          reactively pick up the new value -- see the TODO comment in `ApiKeyStore.kt` for why
          (the key lives in a separate DataStore that Glance's `currentState()`/session
          reactivity doesn't observe, unlike per-widget sensor config). Will need the same kind
          of fix as the sensor-ID reactivity fix already in place.
    - [X] Entry point in the app's main screen to add a key before any widget triggers the
          setup flow (today it's only reachable via a widget's config screen).
- [ ] Release polish:
  - [X] App icon
  - [X] Make Google Play Store account
  - [X] About page (hamburger menu -> About): version, GitHub repo, license, report-an-issue,
        and privacy policy links. See `PRIVACY.md`.
- [ ] RELEASE IT PUBLICLY

## TODO - Further iteration
- [ ] Add some page to explain that this is FOSS and where to find the github page
- [ ] Make the adding experience even better... more like DaysUntil widget app (you can configure in the app first if you'd like)
- [ ] Long-pressing on the Contacts or TickTick widget gave me a Settings option... how can I get that too? If that's available, I'd prefer it over the gear icon.
- [ ] Add support for multiple widgets for multiple sensors
- [ ] Add really robust handling of errors (like the sensor not getting data anymore, inability to connect to PurpleAir, etc.).
- [ ] Add options for push notifications when the value changes past a certain threshold (ie: the first time you go over 100 without until you go back below 100 or 90 or something), etc..
