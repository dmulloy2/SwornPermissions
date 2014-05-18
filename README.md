SwornPermissions
====

I. Installation
  - Remove any other permission or chat systems
  - Drop SwornPermissions.jar into /plugins/
  - Start the server
  - SwornPermissions will automatically convert from PermissionsEx or GroupManager

II. Integration
  1. Essentials
    - Essentials doesn't currently support SwornPerms
    - Remove the 'player-commands' section of the Essentials config.yml
    - Remove EssentialsChat (if installed)
  2. SwornNations / Factions
    - For chat formatting, append '[FACTION]' to the front of 'chat-format' of SwornPermissions config.yml
    - Make sure 'SwornPermissions' is in the 'softdepends' section of the SwornNations / Factions plugin.yml

III. Command Structure
  1. Information
    - /swornperms group <group> list users - List users in a group
    - /swornperms listgroups - List available groups
  2. Player Group Management
    - /swornperms user <user> set group <group> - Sets a player's main group
    - /swornperms user <user> add subgroup <group> - Gives a player a subgroup
    - /swornperms user <user> remove subgroup <subgroup> - Removes a subgroup
    - /swornperms user <user> has group <group> - In group / subgroup
  3. Permission Management
    - /swornperms user/group <user/group> add permission <permission> - Add a permission
    - /swornperms user/group <user/group> remove permission <permission> - Remove a permission
    - /swornperms user/group <user/group> has permission <group> - Permission check
    - /swornperms user/group <user/group> list permissions - List permissions
  4. Option Management
    - /swornperms user/group <user/group> set prefix <prefix> - Set a prefix
    - /swornperms user <user> set suffix <suffix> - Set a suffix
    - /swornperms user/group <user/group> set option <option> <value> - Set an option
    - /swornperms user/group <user/group> has option <option> - Has an option
  5. Prefix / Suffix Management
    - /prefix <prefix> - Change prefix
    - /prefixreset [player] - Reset prefix
    - /suffix <suffix> - Change suffix
    - /suffixreset [player] - Reset suffix
  6. Options
    - Syntax: String: "[value]", Boolean: "b:[value]", Integer: "i:[value]", Double: "d:[value]"

IV. File Structure
  1. Base Directory
    - Server Groups and Config
  2. Worlds
    - Users file and Groups file