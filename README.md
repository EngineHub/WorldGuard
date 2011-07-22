WorldGuard buy/sell fork
========================

This [WorldGuard][wg] fork aims at implementing region buying/selling
as specified in <http://wiki.sk89q.com/wiki/Buying_and_Selling_Regions>.

We use Register to support all major economy plugins like iConomy or
BOSEconomy.

How to use it
=============

Configuration
-------------

Set `register.enable` to true.

Selling
-------

Set `buyable` flag for region to true and `price` flag to region cost
(in iConomy units). Now user can do `/region buy` to own the region
by paying this price.

Issues
======

- WorldGuard stores region owner names in lowercase while economy
  plugins are case-sensitive for account names. As the result, we
  don't have an easy way to pay region owners when a their region is
  bought.

  iConomy 6 is [promised][ico6-case] to have lowercase account names.

Hacking notes
=============

Compiling the package using Maven:

    mvn clean package

Installing Register artifact in local Maven repository:

    mvn install:install-file -DgroupdId=com.nijikokun -DartifactId=register -Dversion=1.0 -Dpackaging=jar -Dfile=Register.jar

Possible package name clash issue is yet to be inspected.

[wg]: http://github.com/sk89q/worldguard
[ico6-case]: https://github.com/iConomy/Core/issues/95
