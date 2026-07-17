# FreeAir

## Project goals

- Build a polished Android home screen widget for PurpleAir sensors.
- Use modern Android development practices.
- Keep the codebase small, readable, and maintainable.
- Prioritize battery efficiency and responsiveness.
- Prefer simple solutions over clever ones.
- Prefer a clean UI that's easy to understand and use, over adding more additional features while existing ones are unpolished.
- The widget defaults to a 2x1 grid-cell size and should stay resizable (`resizeMode="horizontal|vertical"`). When resized, keep the content's aspect ratio roughly constant rather than reflowing the layout — extra space should read as padding around the same design, not a different design.

## Technology

- Kotlin
- Jetpack Compose
- Jetpack Glance
- Material 3
- WorkManager
- Kotlin Coroutines

## Coding guidelines

- Keep UI, networking, and business logic separated.
- Prefer immutable data classes.
- Avoid unnecessary dependencies.
- Add comments only when they improve understanding.
- Never commit secrets or API keys!
- Refer to the TODOs in README.md for the rough idea of the project plan. If you complete something from that list, feel free to cross it off in the same diff.

## Development philosophy

Prefer code that can be verified automatically.

Structure the application so that as much behavior as possible can be tested without manual interaction.

Testing priority:

1. Fast JVM unit tests for business logic.
1. Compose UI tests for screens.
1. Glance widget tests for widget rendering and behavior.
1. A small number of end-to-end instrumentation tests for integration with Android.

Avoid putting business logic directly in Activities, Composables, Workers, or Widgets. Keep those layers thin and move logic into testable classes.

External services (such as the PurpleAir API) should be abstracted behind interfaces so fake implementations can be used during tests. In those cases, I also prefer an integration test.
As an example, if we were testing a function that gets a data blob from the PurpleAir API and parses it, some tests would be:
* Unit test of the business logic to parse the expected response, using a mocked implementation returning static data.
* An integration test that makes sure we're actually able to get data from PurpleAir (ie: our API key still works, they haven't changed the endpoint, the format of returned data still has the attributes that we rely on, etc.).

The integration tests (which do not need to be run as often because they relate primarily to things that will be changing less frequently, so we could run them once per commit rather than every iteration) should be in a separate model and do not need to be run at each build, but after we think everything is working, they should be run prior to commit.

When implementing new features, include or update automated tests whenever practical.


## Agent workflow

When implementing a feature:

1. Implement the smallest useful increment.
1. Run formatting, lint, and unit tests.
1. Fix any failures before considering the task complete.
1. Prefer adding tests over relying on manual verification.
1. Ensure the integration tests also pass, before committing code.
1. If a feature cannot be tested automatically, explain why.


## Architecture

Business logic should not depend on Android framework classes whenever possible.

Favor pure Kotlin classes that can be tested with ordinary JVM tests.

Android framework code should primarily wire together UI, widgets, WorkManager, and repositories.

## Before considering work complete

- Build succeeds.
- No lint warnings introduced.
- Code is formatted.
- Anything that can be tested automatically, passes. Anything new that _can_ be tested automatically (or something you notice that is missing tests) now has tests (that pass). 
- Keep changes as small and focused as practical.
