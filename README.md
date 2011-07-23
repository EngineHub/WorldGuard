WorldGuard buy/sell fork
========================

This [WorldGuard][wg] fork aims at implementing region buying/selling
as specified in [WorldGuard/Buying and Selling Regions][wg-buyspec]
spec.

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

Note that negative `price` flag value is not prohibited by WG.

Signs
-----

Signs may be used as a convenient way to put a region on sale through
`buy` and `price` flags.

Set `register.signs.enable` to `true` to enable this feature.

### Selling ###

Place a sign with lines

    [ForSale]
    lot_name
    price

To make region with ID `lot_name` buyable for this price.

- First line _must_ be equal to string set in `register.signs.tag`
  (default is `[ForSale]`).

- Second line _must_ contain region ID.

- Buyable flag will be set to `true` (if not set already). This
requires (requires `worldguard.region.flag.{own,member,}.lot_name`
*and* `worldguard.region.flag.flags.buyable.{own,member,}.lot_name`,
depending on whether player is owner, member or none for the region).

- If price line is missing, it is auto-filled from current `price`
flag value for the region (remember that if the flag is not set,
*price is considered zero credits*). Otherwise, `price` flag is set to
the value specified in the sign (requires
`worldguard.region.flag.{own,member,}.lot_name` *and*
`worldguard.region.flag.flags.price.{own,member,}.lot_name`,
depending on whether player is owner, member or none for the region).

- If last line of the sign is empty, it is auto-filled with the name
of the first member of owners list (name will be in lower case due to
the way WorldGuard stores owner lists)

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

- Perhaps we want to block buying when price is not set (instead of
  considering it zero).

- There used to be permissions nodes both with `.owner` and `.own` in
  upstream WG and [spec][wg-buyspec]. We streamlined them to `.own`.

Hacking notes
=============

Compiling the package using Maven:

    mvn clean package

Installing Register artifact in local Maven repository:

    mvn install:install-file -DgroupdId=com.nijikokun -DartifactId=register -Dversion=1.0 -Dpackaging=jar -Dfile=Register.jar

Possible package name clash issue is yet to be inspected.

[wg]: http://github.com/sk89q/worldguard
[ico6-case]: https://github.com/iConomy/Core/issues/95
[wg-buyspec]: http://wiki.sk89q.com/wiki/Buying_and_Selling_Regions
