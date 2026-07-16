Open Source project to create a simple Android widget for viewing PurpleAir air quality data. The primary use-case is looking
at your own devices (that is relevant to API usage limits - you aren't really limited when looking at your own sensors, I think).

PLEASE NOTE: This project is not affiliated with nor endorsed by PurpleAir.



Note to AI:
* Keep in mind that we want this to support multiple sensors in the future (so someone who owns more than one sensor could have a widget for each one on his screen), so architect in a way to allow that.

## TODO - Initial release
* Create project and get it running.
* Get a widget to be extant. Hello World on it.
* Wire up Purple Air to make a hardcoded request to get the data we need. If this involves the API key, make sure that isn't committed to the project.
* Once proof-of-concept is done, get it rendering the way we'd expect.
* Set the correct update-cadence, caching, etc. behavior and test it.
* Pull out any API keys so that we can now commit the rest to github (could just have it in a gitignored file for now).
* Create a low-lift setup flow. This will be challenging, make it very clear to the user, and require minimal effort.
** Get the person's sensor ID
** Help the user get an API key from PurpleAir and put it into the app (v1 of this might still be able to be improved with cleverness later).  This should be stored locally for the user.
* Figure out if we can make it so that tapping the widget opens up the PurpleAir mobile-web page.
* Make it so opening the app, is a very understandable view to see configuration options. It should be designed in a way to accomodate multiple sensors, but still make tons of sense if there's only 1 sensor since that might be the main use-case.
* START USING IT & RELEASE IT PUBLICLY

## TODO - Further iteration
* Add some page to explain that this is FOSS and where to find the github page
* Add support for multiple widgets for multiple sensors
