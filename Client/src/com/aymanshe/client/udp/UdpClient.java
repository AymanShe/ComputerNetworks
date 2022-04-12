package com.aymanshe.client.udp;

import com.aymanshe.shared.Packet;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UdpClient {

    private final SocketAddress routerAddress;
    private final DatagramChannel channel;


    public UdpClient(SocketAddress routerAddress) throws IOException {
        this.routerAddress = routerAddress;
        channel = DatagramChannel.open();
    }

    public void sendPacket(Packet packet) throws IOException {
        channel.send(packet.toBuffer(), routerAddress);
    }

    public Packet receivePacket() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
        channel.receive(buf);
        buf.flip();
        return Packet.fromBuffer(buf);
    }

    public void closeChannel() throws IOException {
        channel.close();
    }
}

