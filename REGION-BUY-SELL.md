# WorldGuard buy/sell features

Region buying/selling is implemented as specified in
[WorldGuard/Buying and Selling Regions][wg-buyspec] spec.

We use [Register][] to support all major economy plugins like iConomy or
BOSEconomy. Upon starting, WorldGuard should issue a notice to
console, similar to this:
 
    [INFO] [WorldGuard] Payment method found (iConomy version: 5)


## How to use it

### Commands

Set `register.enable` to true in order to enable `/region buy` command.

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

### Signs

Signs may be used as a convenient way to put a region on sale through
`buy` and `price` flags.

Set `register.signs.enable` to `true` to enable this feature.

#### Selling

Place a sign with lines

    [ForSale]
    lot_name
    price

To make region with ID `lot_name` buyable for this price.

- First line _must_ be equal to string set in `register.signs.tag`
  (default is `'[ForSale]'`).

- Second line _must_ contain region ID.

- Buyable flag will be set to `true` (if not set already). This
requires (requires `worldguard.region.flag.{owner.,member.,}lot_name`
*and* `worldguard.region.flag.flags.buyable.{owner.,member.,}.lot_name`,
depending on whether player is owner, member or none for the region).

- If price line is missing, it is auto-filled from current `price`
flag value for the region (remember that if the flag is not set,
*price is considered zero credits*). Otherwise, `price` flag is set to
the value specified in the sign (requires
`worldguard.region.flag.{owner.,member.,}.lot_name` *and*
`worldguard.region.flag.flags.price.{owner.,member.,}.lot_name`,
depending on whether player is owner, member or none for the region).

- If last line of the sign is empty, it is auto-filled with the name
of the first member of owners list (it looks like it's always the
first name in list ordered alphabetically, name will be in lower case due to
the way WorldGuard stores owner lists). If last line is `#`, it's
replaced with region dimensions (width×length×height, which is *xzy*
in terms of Minecraft coordinates).

If sign is created successfully, its first line is colored with color
set in `register.signs.tag-color` (default is `'DARK_BLUE'`, any color
from ChatColor Bukkit enumeration is available for use).

#### Buying

By right-clicking a sign the user can buy the region. Sign is
destroyed afterwards.

## Important notes

- Negative `price` flag value is not prohibited by WG.

- When `price` flag is not set, it is considered *zero*.

- Destroying sell sign does not withdraw sell offer (`buyable` flag
  does not change).

- Buying a region by manually issuing `/region buy` command does not
  destroy sell sign, as well as changing region buyable state, price
  or owners is not reflected on the sign.

## Issues

- WorldGuard stores region owner names in lowercase while economy
  plugins are case-sensitive for account names. As the result, we
  don't have an (easy) way to pay region owners when a their region is
  bought. We use homebrew getPossiblePlayerNames function coupled with
  `region.isOwner` to reverse search for region owners. Whatever
  action is finally taken, buyer/owner are notified. iConomy 6 is
  [promised][ico6-case] to have lowercase account names.

- Buy-on-claim is a separate feature which differs from buying since
  price depends on region size and there's no payment recipient. It
  will use Register integration as well.

- Perhaps we want to block buying when price is not set (instead of
  considering it zero).

- Cannot pick up signs dropped by `dropSign`.

- `/region` commands are not disabled when `regions.enable` is
  `false`. In `WorldGuardPlugin.java`:
  
      commands.register(ProtectionCommands.class);

## Hacking notes

Compiling the package using Maven:

    mvn clean package

Installing Register artifact in local Maven repository:

    mvn install:install-file -DgroupId=com.nijikokun -DartifactId=register -Dversion=1.1 -Dpackaging=jar -Dfile=register-1.1.jar

We use Maven shade plugin to relocate `com.nijikokun.register.*`
classes under `com.sk89q.worldguard.register` to avoid conflicts with
other plugins using Register.

[wg]: http://github.com/sk89q/worldguard
[ico6-case]: https://github.com/iConomy/Core/issues/95
[register]: https://github.com/iConomy/Register
[wg-buyspec]: http://wiki.sk89q.com/wiki/Buying_and_Selling_Regions
