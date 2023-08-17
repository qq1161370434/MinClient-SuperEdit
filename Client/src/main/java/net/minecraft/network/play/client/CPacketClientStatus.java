package net.minecraft.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.io.IOException;

public class CPacketClientStatus implements Packet<INetHandlerPlayServer>
{
    private CPacketClientStatus.State status;

    public CPacketClientStatus()
    {
    }

    public CPacketClientStatus(CPacketClientStatus.State p_i46886_1_)
    {
        this.status = p_i46886_1_;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.status = buf.readEnumValue(State.class);
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeEnumValue(this.status);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.processClientStatus(this);
    }

    public CPacketClientStatus.State getStatus()
    {
        return this.status;
    }

    public enum State
    {
        PERFORM_RESPAWN,
        REQUEST_STATS
    }
}
