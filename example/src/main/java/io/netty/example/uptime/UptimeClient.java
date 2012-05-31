/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.example.uptime;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioEventLoop;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;


/**
 * Connects to a server periodically to measure and print the uptime of the
 * server.  This example demonstrates how to implement reliable reconnection
 * mechanism in Netty.
 */
public class UptimeClient {

    // Sleep 5 seconds before a reconnection attempt.
    static final int RECONNECT_DELAY = 5;

    // Reconnect when the server sends nothing for 10 seconds.
    private static final int READ_TIMEOUT = 10;

    private final String host;
    private final int port;

    public UptimeClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        configureBootstrap(new Bootstrap()).connect();
    }

    private Bootstrap configureBootstrap(Bootstrap b) {
        return configureBootstrap(b, new NioEventLoop());
    }

    Bootstrap configureBootstrap(Bootstrap b, EventLoop l) {
        b.eventLoop(l)
         .channel(new NioSocketChannel())
         .remoteAddress(host, port)
         .initializer(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                        new ReadTimeoutHandler(READ_TIMEOUT),
                        new UptimeClientHandler(UptimeClient.this));
            }
         });

        return b;
    }

    public static void main(String[] args) throws Exception {
        // Print usage if no argument is specified.
        if (args.length != 2) {
            System.err.println(
                    "Usage: " + UptimeClient.class.getSimpleName() +
                    " <host> <port>");
            return;
        }

        // Parse options.
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        new UptimeClient(host, port).run();
    }
}