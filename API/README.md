## How to use

### <ins>Transfering A Player</ins>
```java
  LunarTransferAPI.getInstance().transfer(player.getUniqueId(), "hypixel.net", (reply) -> {
  
  });
```


### <ins>Getting a Player's Ping to a IP</ins>
```java 
  List<String> servers = new ArrayList<>();
  servers.add("hypixel.net");
  LunarTransferAPI.getInstance().ping(player.getUniqueId(), servers, (reply) -> {
       
   });
```
