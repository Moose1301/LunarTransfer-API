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

package me.moose.lunartransfer.api;

import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class LunarTransferAPI {
    private static LunarTransferAPI instance;
    @Getter private final Set<UUID> runningTransfer = new HashSet<>();
    public LunarTransferAPI() {
        instance = this;
    }

    public static LunarTransferAPI getInstance() {
        if(instance == null) {
            throw new RuntimeException("[Lunar Transfer] API Not Started");
        }
        return instance;
    }
    /**
     * @param uuid the user to check using the transfer packet
     * @returns If the user is running it
     */
    public boolean isRunning(UUID uuid) {
        return runningTransfer.contains(uuid);
    }

    /**
     *
     * @param uuid the uuid of the target player
     * @param serverIP the target server IP
     * @param callback The callback if the player accepted or denied the unverified transfer request
     */
    public abstract void transfer(UUID uuid, String serverIP, Consumer<Boolean> callback);

    /**
     *
     * @param uuid the uuid of the target player
     * @param servers list of servers to get the ping from the client (Max of 10)
     * @param callback The callback if the player accepted or denied the unverified transfer request
     *
     * @throws RuntimeException if the parameter "servers" is more the 10
     */
    public abstract void ping(UUID uuid, List<String> servers, Consumer<ServerPingReply> callback) throws RuntimeException;
}
