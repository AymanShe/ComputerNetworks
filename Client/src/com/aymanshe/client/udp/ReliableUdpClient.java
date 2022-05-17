package com.aymanshe.client.udp;

import com.aymanshe.shared.Packet;
import com.aymanshe.client.HttpRequest;
import com.aymanshe.client.HttpResponse;
import com.aymanshe.shared.PacketTypes;
import com.aymanshe.shared.SentPacket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ReliableUdpClient {
    private static final String ROUTER_HOST = "localhost";
    private static final int ROUTER_PORT = 3000;
    private int clientSequenceNumber;
    private SentPacket[] sentPackets;

    private final UdpClient udpClient;

    public ReliableUdpClient() throws IOException {
        SocketAddress routerAddress = new InetSocketAddress(ROUTER_HOST, ROUTER_PORT);

        this.udpClient = new UdpClient(routerAddress);
        clientSequenceNumber = 0;
    }

    public HttpResponse sendReliably(HttpRequest request) throws Exception {

        InetSocketAddress serverAddress = new InetSocketAddress(request.getAddress(), request.getPort());

        Handshake(serverAddress);

        //send packet
        Packet p = new Packet.Builder()
                .setType(PacketTypes.DATA)
                .setSequenceNumber(clientSequenceNumber)
                .setPortNumber(serverAddress.getPort())
                .setPeerAddress(serverAddress.getAddress())
                .setPayload(request.getPayload().getBytes())
                .create();
        udpClient.sendPacket(p);

        addToSentPackets(p);

        //receive response
        Packet response = udpClient.receivePacket();

        String payload = new String(response.getPayload(), StandardCharsets.UTF_8);
        Scanner in = new Scanner(payload);

        udpClient.closeChannel();

        return HttpResponse.parseResponse(in);
    }

    private void addToSentPackets(Packet packet) {
        int index = clientSequenceNumber % 10;
        sentPackets[index] = new SentPacket(packet, false);
    }

    private Packet receivePacket(long sequenceNumber){


    }

    private boolean findPacket(long sequenceNumber){
        for(Packet packet: sentPackets){
            if(singleItem.equalsIgnoreCase(itemToBeSearched)){
                isItemFound = true;
                return isItemFound;
            }
        }
    }
    private void Handshake(InetSocketAddress serverAddress) throws Exception {
        Packet p = new Packet.Builder()
                .setType(PacketTypes.SYN)
                .setSequenceNumber(clientSequenceNumber)
                .setPortNumber(serverAddress.getPort())
                .setPeerAddress(serverAddress.getAddress())
                .setPayload("".getBytes())
                .create();

        udpClient.sendPacket(p);

        Packet synAck = udpClient.receivePacket();
        if (synAck.getType() != PacketTypes.SYN_ACK || synAck.getSequenceNumber() != p.getSequenceNumber()+1){
            throw new Exception("packet received in handshake is not SYN ACK");
        }

        clientSequenceNumber++;

        Packet ack = new Packet.Builder()
                .setType(PacketTypes.ACK)
                .setSequenceNumber(clientSequenceNumber)
                .setPortNumber(serverAddress.getPort())
                .setPeerAddress(serverAddress.getAddress())
                .setPayload("".getBytes())
                .create();
        udpClient.sendPacket(ack);
    }
}
