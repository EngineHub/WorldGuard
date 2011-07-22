WorldGuard buy/sell fork
========================

This [WorldGuard][wg] fork aims at implementing region buying/selling
as specified in <http://wiki.sk89q.com/wiki/Buying_and_Selling_Regions>.

We use Register to support all major economy plugins like iConomy or
BOSEconomy. Upon starting, WorldGuard should issue a notice to
console, similar to this:
 
    [INFO] [WorldGuard] Payment method found (iConomy version: 5)


How to use it
=============

Configuration
-------------

Set `register.enable` to true in order to enable `/region buy` command.

Selling
-------

Suppose you want to sell region with ID `lot9` for 19.45 economy
credits.

1. Set a price for your region:

    /region flag lot9 price 19.45

2. Mark you region for sale:
   
    /region flag lot9 buyable true

3. Now anybody who has the permission `worldguard.region.buy.lot9` (or
`worldguard.region.buy.*`) may run command

    /region buy lot9

And he will become the owner of the region. Region price is deducted
from his economy account and each of previous region owners will get
equal share of the payment if they have economy accounts.

All previous region owners will lose their rights for that region (an
option to just add a new owner instead may be implemented).

If a group is among owners of a region, they will not receive any
payment at all (to be implemented, perhaps).

`buyable` region flag is set back to `false` upon buying, but `price`
flag is preserved.

Issues
======

- WorldGuard stores region owner names in lowercase while economy
  plugins are case-sensitive for account names. As the result, we
  don't have an (easy) way to pay region owners when a their region is
  bought. We use Bukkit's `matchPlayer` coupled with `region.isOwner`
  to reverse search for region owners. Whatever action is finally
  taken, buyer/owner are notified. iConomy 6 is [promised][ico6-case]
  to have lowercase account names.

- Buy-on-claim is a separate feature which differs from buying since
  price depends on region size and there's no payment recipient. It
  will use Register integration as well.

Hacking notes
=============

Compiling the package using Maven:

    mvn clean package

Installing Register artifact in local Maven repository:

    mvn install:install-file -DgroupdId=com.nijikokun -DartifactId=register -Dversion=1.0 -Dpackaging=jar -Dfile=Register.jar

Possible package name clash issue is yet to be inspected.

[wg]: http://github.com/sk89q/worldguard
[ico6-case]: https://github.com/iConomy/Core/issues/95
