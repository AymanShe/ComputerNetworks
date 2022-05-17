package com.aymanshe.shared;

import java.net.InetAddress;

public class SentPacket{
    private Packet packet;
    private boolean acknowledged;

    public SentPacket(Packet packet, boolean acknowledged) {
        this.packet = packet;
        this.acknowledged = acknowledged;
    }

    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }
}
