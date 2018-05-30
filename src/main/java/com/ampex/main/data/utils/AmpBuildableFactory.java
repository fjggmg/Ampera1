package com.ampex.main.data.utils;


import amp.ByteTools;
import amp.HeadlessPrefixedAmplet;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.network.logic.InvalidCRCException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;

public class AmpBuildableFactory {

    public static AmpBuildable buildPacket(byte[] array) throws InvalidCRCException, InvalidAmpBuildException {
        try {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(array);
            String c = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
            AmpBuildable packet = (AmpBuildable) Class.forName(c).newInstance();
            byte[] crcArray = hpa.getNextElement();
            long crc = ByteTools.buildLong(crcArray[0], crcArray[1], crcArray[2], crcArray[3], crcArray[4], crcArray[5], crcArray[6], crcArray[7]);
            byte[] packetArray = hpa.getNextElement();

            if (!EncryptionManager.checkCRCValue(packetArray, crc)) {
                throw new InvalidCRCException("CRC on packet does not verify. Bad packet.");
            }
            packet.build(packetArray);
            return packet;
        } catch (ClassCastException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new InvalidAmpBuildException("Packet was unable to be deserialized.");
        }
    }

    public static ByteBuf finalizeBuildAsPacket(AmpBuildable packet) {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        byte[] p = packet.serializeToBytes();
        //System.out.println("Prepping packet for transfer of type: " + packet.getClass().getName());
        hpa.addElement(packet.getClass().getName());
        hpa.addElement(EncryptionManager.getCRCValue(p));
        hpa.addBytes(p);
        return Unpooled.wrappedBuffer(hpa.serializeToBytes());
    }
}
