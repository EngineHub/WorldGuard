Compiling
=========

You can compile WorldGuard as long as you have some version of Java greater than or equal to 21 installed. 
Gradle will download JDK 21 specifically if needed, but it needs some version of Java to bootstrap from.

The build process uses Gradle, which you do *not* need to download. WorldGuard is a multi-module project with three modules:

* `worldguard-core` contains the WorldGuard API
* `worldguard-bukkit` is the Bukkit plugin
* `worldguard-libs` contains library relocations

## To compile...

### On Windows

1. **Shift** + **right click** the folder with WorldGuard's files and click "Open PowerShell window here".
2. `gradlew build`

### On Linux, BSD, or Mac OS X

1. In your terminal, navigate to the folder with WorldGuard's files (`cd /folder/of/worldguard/files`)
2. `./gradlew build`

## Then you will find...

You will find:

* The core WorldGuard API in **worldguard-core/build/libs**
* WorldGuard for Bukkit in **worldguard-bukkit/build/libs**

If you want to use WorldGuard, use the `-dist` version.

(The -dist version includes WorldGuard + necessary libraries.)

## Other commands

* `gradlew idea` will generate an [IntelliJ IDEA](http://www.jetbrains.com/idea/) module for each folder.
* `gradlew eclipse` will generate an [Eclipse](https://www.eclipse.org/downloads/) project for each folder.
