# Card game base

*This is an archived project and won't be developed any further.*

libGDX-based framework for developing card game applications on desktop and Android.
Provides a set of UI components: menus, playing cards and card stacks, buttons, dialogs, etc.
along with utilities for their animation.
There is also some utilities for creating MCTS agents.

## Building

JDK 8 is required so that the built libraries are compatible with Android application that target JDK 8.
The Gradle project can be imported in IntelliJ IDEA and be built from there.

Sprites and texture packer files used are located in the `assets_raw` directory.
The GDX texture packer projects used to create the atlases are also available.
The resulting atlas will be produced in the appropriate `assets` directory.
Although the compression is set to PNGTastic, TinyPNG was used for some files in the repository
(core background & pcard) to reduce size with lossy compression.

The core and pcard librairies can be published to a local Maven repository for use in a project.
The librairies don't include the assets, which must be copied separatedly by the project that uses them.

### Testing

A few unit tests for the core module are available.
The `tests-core` directory contains a set of tests for UI components.
A frontend application for the desktop and for Android is also available, which allow selecting and executing a test.

## License & credits

- All code is licensed under Apache License 2.0.
- The overall UI design was heavily inspired from games made by [Eryod Soft](https://play.google.com/store/apps/dev?id=7071430644205651346&gl=US).
- The font is Roboto regular and MSDF files were generated using [mdsf-gdx-gen](https://github.com/maltaisn/msdf-gdx-gen).
- Playing card sprites were taken from: https://sourceforge.net/projects/vector-cards/ (LGPLv3.0)
- Card background sprite and card suit icons were taken from: https://sourceforge.net/projects/svg-cards/ (LGPLv2.0)
- Sound effects taken from: www.opengameart.org
