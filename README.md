# AdminToolbox

Effective Minecraft moderation tools designed to prevent cheating, maintaining staff integrity and community trust.

**AdminToolbox natively supports [Folia][folia].**

## Commands

### Spectate

`/spectate`, `/admin`, `/target`

Enter admin mode at current location. In this mode, the admin's inventory is cleared and they are put into spectator mode.

While spectating, the admin can use the command again to exit "admin mode". They will be teleported back to their original location and placed back into survival mode with their original inventory.

#### Target Locations

- `/target <player>` - Enter admin mode at a specified player's location
- `/target <x> <y> <z> [world]` - Enter admin mode at specific coordinates
- `/target <x> <z> [world]` - Enter admin mode at specific coordinates (uses the highest Y level at that location)

#### Navigation

- `/back` - Move to previous location in teleport history
- `/forward` - Move to next location in teleport history

### Reveal

`/reveal`, `/show`

While in admin mode, running this command places the admin into survival mode at their current location. This makes the admin visible to players during confrontations while maintaining:

- Empty inventory
- Immunity to damage
- No mob targeting

### Yell

`/yell <player> <message>`

Forcibly displays a large red title on the targeted player's screen.
Use the pipe character (`|`) to separate title and subtitle: `title | subtitle`.

Legacy ampersand [color codes] are supported. (i.e. `Don't steal! | Please reread the &e/rules&r.`)

![A large red title displays within Minecraft: "No stealing!" A smaller subtitle below it reads "Did you read the /rules?", and "/rules" is highlighted in yellow.](./.assets/demo-yell.jpg)
### Freeze

`/freeze <player>`

Stop a player from moving around, forcing them to stay locked in place. Frozen players:

- Can still look around
- Will fall if in midair
- Cannot place or break blocks
- Cannot interact with the world
- Cannot hurt entities
- Are immune to damage

To unfreeze a player, use `/unfreeze <player>`

## Permissions

| Permission                       | Command                       | Description                                 |
|----------------------------------|-------------------------------|---------------------------------------------|
| `admintoolbox.target`            | `/admin`, `/target`           | Allows entering admin mode                  |
| `admintoolbox.target.player`     | `/target <player>`            | Enter admin mode at a player's location     |
| `admintoolbox.target.location`   | `/target <x> [y] <z> [world]` | Enter admin mode at given coordinates       |
| `admintoolbox.reveal`            | `/reveal`                     | Allows becoming visible while in admin mode |
| `admintoolbox.yell`              | `/yell`                       | Allows sending forceful messages to players |
| `admintoolbox.freeze`            | `/freeze`                     | Allows freezing and unfreezing players      |
| `admintoolbox.broadcast.receive` |                               | Receive alerts about other admins' actions  |
| `admintoolbox.broadcast.exempt`  |                               | Actions will not alert other admins         |
| `admintoolbox.admin`             |                               | Access to all AdminToolbox features         |

## Integrations

- **[BlueMap](https://bluemap.bluecolored.de):**
  On servers with BlueMap, the plugin will hide admins who are [revealed](#reveal) from the map.

[folia]: https://papermc.io/software/folia
[color codes]: https://minecraft.wiki/w/Formatting_codes
