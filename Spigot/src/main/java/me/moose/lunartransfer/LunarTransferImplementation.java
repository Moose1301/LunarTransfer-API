/*
 * This file is part of LunarTransferAPI, licensed under the MIT License.
 *
 *  Copyright (c) Moose1301
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.moose.lunartransfer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.moose.lunartransfer.api.LunarTransferAPI;
import me.moose.lunartransfer.api.ServerPingReply;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LunarTransferImplementation extends LunarTransferAPI implements PluginMessageListener {

    private final LunarTransferPlugin plugin;

    private final Cache<UUID, Consumer<Boolean>> transferCallbacks;
    private final Cache<UUID, Consumer<ServerPingReply>> pingCallbacks;

    public LunarTransferImplementation(LunarTransferPlugin plugin) {
        this.plugin = plugin;
        this.transferCallbacks = createCache();
        this.pingCallbacks = createCache();
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "transfer:channel", this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "transfer:channel");
    }

    private static <K, V> Cache<K, V> createCache() {
        return CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).build();
    }

    @Override
    public void transfer(UUID uuid, String serverIP, Consumer<Boolean> callback) {
        if (!isRunning(uuid)) return;
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(0);
        byte[] target = serverIP.getBytes(StandardCharsets.UTF_8);

        out.writeInt(target.length);
        out.write(target);
        if (sendPacket(uuid, out.toByteArray())) {
            transferCallbacks.asMap().put(uuid, callback);
        }
    }


    @Override
    public void ping(UUID uuid, List<String> servers, Consumer<ServerPingReply> callback) throws RuntimeException {
        if (!isRunning(uuid)) return;
        if (servers.size() > 10) {
            throw new RuntimeException("[LunarTransferAPI] Tried to Ping over 10 Servers!");
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(2);
        out.writeInt(servers.size());
        for (String server : servers) {
            byte[] serverIP = server.getBytes(StandardCharsets.UTF_8);
            out.writeInt(serverIP.length);
            out.write(serverIP);
        }
        if (sendPacket(uuid, out.toByteArray())) {
            pingCallbacks.asMap().put(uuid, callback);
        }
    }

    /**
     * @param uuid UUID of the target player
     * @param data the data of the packet
     * @return if the packet was sent
     */
    public boolean sendPacket(UUID uuid, byte[] data) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return false;
        player.sendPluginMessage(plugin, "transfer:channel", data);
        return true;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        //The ID of the incoming Packet
        int id = in.readByte();


        if (id == 1) {  //Reply a Transfer Request
            boolean reply = in.readByte() == 1;
            Consumer<Boolean> consumer = transferCallbacks.getIfPresent(player.getUniqueId());
            if (consumer == null) return;
            //Remove it from the cache
            transferCallbacks.invalidate(player.getUniqueId());

            //Accept the consumer
            consumer.accept(reply);

        } else if (id == 3) {  //Reply from Server Ping
            Consumer<ServerPingReply> consumer = pingCallbacks.getIfPresent(player.getUniqueId());
            if (consumer == null) return;

            //The Number of Servers
            int serversCount = in.readInt();

            Map<String, Long> serverPings = new HashMap<>();
            for (int i = 0; i < serversCount; i++) {
                //The Length of String
                int ipLength = in.readInt();
                //Get the string from bytes using the length
                String ip = new String(readBytes(in, ipLength));
                long ping = in.readLong();
                serverPings.put(ip, ping);
            }
            //Remove it from the cache
            pingCallbacks.invalidate(player.getUniqueId());

            //Accept the consumer
            consumer.accept(new ServerPingReply(Collections.unmodifiableMap(serverPings)));
        }

    }

    public byte[] readBytes(ByteArrayDataInput input, int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = input.readByte();
        }
        return data;
    }


}
