# LunarTransferAPI
This LunarTransferAPI allows you to utilise the transfer packet that has been added to LunarClient to send players to other servers and also receive the ping of the player to that server

## Handling the promot screen
To ignore the prompt by the client to the player, the server you are being switched to needs to have a TXT record with the domain of the server you are being sent from.

### Example: 
If the player is being sent from localhost to hypixel.net, hypixel.net needs to create a TXT record with localhost under "mc_transfer_accept_from="
So it will look like this under a TXT record: "mc_transfer_accept_from=localhost"

## Reporting an issue

If you find an issue you can submit it [here](https://github.com/Moose1301/LunarTransferAPI/issues).

## Get the Source

1. Install maven `sudo apt-get install maven`
2. Verify installation `mvn -v`
3. Clone the repository `git clone git@github.com:Moose1301/LunarTransferAPI.git`
4. Navigate to the new folder `cd lunartransferapi`
5. Import `pom.xml` into your IDE

## Contributing

You can submit a [pull request](https://github.com/Moose1301/LunarTransferAPI/pulls) with your changes.
