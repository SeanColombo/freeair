Open Source project to create a simple Android widget for viewing PurpleAir air quality data. The primary use-case is looking
at your own devices (that is relevant to API usage limits - you aren't really limited when looking at your own sensors, I think).

PLEASE NOTE: This project is not affiliated with nor endorsed by PurpleAir.



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
  - [ ] Get the person's sensor ID
  - [ ] Help the user get an API key from PurpleAir and put it into the app (v1 of this might still be able to be improved with cleverness later).  This should be stored locally for the user.
  - [ ] If the person adds from home screen and there are any configured widgets, we have to ask them if they want an existing widget, or create a new one. I'm not sure that was in the Plan
  - [ ] Need to mention how to delete them (ie: just long press on homescreen and remove)? Or have a drag-left option and trash can?
- [ ] Make it so opening the app, is a very understandable view to see configuration options. It should be designed in a way to accommodate multiple sensors, but still make tons of sense if there's only 1 sensor since that might be the main use-case.
- [ ] Release polish:
  - [ ] App icon
  - [ ] Make Google Play Store account
  - [ ] About page
- [ ] RELEASE IT PUBLICLY

## TODO - Further iteration
- [ ] Add some page to explain that this is FOSS and where to find the github page
- [ ] Make the adding experience even better... more like DaysUntil widget app (you can configure in the app first if you'd like)
- [ ] Long-pressing on the Contacts or TickTick widget gave me a Settings option... how can I get that too? If that's available, I'd prefer it over the gear icon.
- [ ] Add support for multiple widgets for multiple sensors
- [ ] Add really robust handling of errors (like the sensor not getting data anymore, inability to connect to PurpleAir, etc.).
- [ ] Add options for push notifications when the value changes past a certain threshold (ie: the first time you go over 100 without until you go back below 100 or 90 or something), etc..
