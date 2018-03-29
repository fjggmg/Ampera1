package com.lifeform.main.transactions;

import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.Utils;

import java.io.UnsupportedEncodingException;

/**
 * Spec:
 * v.125 (non prefixed) 124 (prefixed), may move to 2 byte versioning soon
 * ID is b64 28, 32, 48, or 64 bit hash
 * checksum is 48 bits
 */
public class NewAdd implements IAddress {
    public static final byte VERSION = 125;
    public static final byte PREFIXED_VERSION = 124;
    private byte version;
    private String ID;
    private String checksum;
    private boolean prefixed = false;
    private String prefix = "";
    private byte lengthIndicator;
    private boolean p2sh;

    public NewAdd(byte version, String ID, String checksum, byte lengthIndicator, boolean p2sh) throws InvalidAddressException {
        this.version = version;
        this.p2sh = p2sh;
        try {
            Utils.fromBase64(ID);
            Utils.fromBase64(checksum);
        } catch (Exception e) {
            throw new InvalidAddressException("ID or checksum not Base64 encoded");
        }
        this.ID = ID;
        this.lengthIndicator = lengthIndicator;
        this.checksum = checksum;
    }

    public NewAdd(byte version, String ID, String checksum, byte lengthIndicator, String prefix, boolean p2sh) throws InvalidAddressException {
        this(version, ID, checksum, lengthIndicator, p2sh);
        if (version != PREFIXED_VERSION) throw new InvalidAddressException("Prefix used with non prefix version");
        this.prefixed = true;
        if (prefix.length() != 5) throw new InvalidAddressException("Prefix is not 5 characters long");
        this.prefix = prefix;
    }

    @Override
    public String encodeForChain() {
        if (prefixed) return version + Utils.byteToHex(lengthIndicator) + ((p2sh) ? 1 : 0) + prefix + ID + checksum;
        return version + Utils.byteToHex(lengthIndicator) + ((p2sh) ? 1 : 0) + ID + checksum;
    }

    public static IAddress decodeFromChain(String encoded) {
        char[] eChar = encoded.toCharArray();
        char[] vChar = {eChar[0], eChar[1], eChar[2]};
        byte version = Byte.parseByte(new String(vChar));
        char[] al = new char[2];
        al[0] = eChar[3];
        al[1] = eChar[4];
        AddressLength l = AddressLength.byIndicator(Utils.hexToByte(new String(al)));
        if (l == null) return null;
        boolean p2sh = (eChar[5] == '1');
        if (version == VERSION) {
            StringBuilder ID = new StringBuilder();
            for (int i = 6; i < eChar.length - 8; i++) {
                ID.append(eChar[i]);
            }
            char[] cChar = {eChar[eChar.length - 8], eChar[eChar.length - 7], eChar[eChar.length - 6], eChar[eChar.length - 5], eChar[eChar.length - 4], eChar[eChar.length - 3], eChar[eChar.length - 2], eChar[eChar.length - 1]};

            String checksum = new String(cChar);

            try {
                return new NewAdd(version, ID.toString(), checksum, l.getIndicator(), p2sh);
            } catch (InvalidAddressException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            StringBuilder prefix = new StringBuilder();
            for (int i = 6; i < 11; i++) {
                prefix.append(eChar[i]);
            }
            StringBuilder ID = new StringBuilder();
            for (int i = 11; i < eChar.length - 8; i++) {
                ID.append(eChar[i]);
            }
            char[] cChar = {eChar[eChar.length - 8], eChar[eChar.length - 7], eChar[eChar.length - 6], eChar[eChar.length - 5], eChar[eChar.length - 4], eChar[eChar.length - 3], eChar[eChar.length - 2], eChar[eChar.length - 1]};

            String checksum = new String(cChar);

            try {
                return new NewAdd(version, ID.toString(), checksum, l.getIndicator(), prefix.toString(), p2sh);
            } catch (InvalidAddressException e) {
                e.printStackTrace();
                return null;
            }
        }
    }


    @Override
    public byte getVersion() {
        return version;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getChecksum() {
        return checksum;
    }

    @Override
    public boolean isValid() {
        AddressLength l = AddressLength.byIndicator(lengthIndicator);
        if (l == null) return false;
        switch (l) {
            case SHA224:
                if (Utils.fromBase64(ID).length != 28) return false;
                break;
            case SHA256:
                if (Utils.fromBase64(ID).length != 32) return false;
                break;
            case SHA384:
                if (Utils.fromBase64(ID).length != 48) return false;
                break;
            case SHA512:
                if (Utils.fromBase64(ID).length != 64) return false;
                break;
        }
        byte[] check = EncryptionManager.sha224(Utils.fromBase64(ID));
        byte[] bChecksum = new byte[6];
        bChecksum[0] = check[check.length - 6];
        bChecksum[1] = check[check.length - 5];
        bChecksum[2] = check[check.length - 4];
        bChecksum[3] = check[check.length - 3];
        bChecksum[4] = check[check.length - 2];
        bChecksum[5] = check[check.length - 1];
        String checksum = Utils.toBase64(bChecksum);
        if (!checksum.equals(this.checksum)) return false;
        //TODO byte thing here

        return true;
    }

    @Override
    public boolean canSpend(String keys, String entropy, boolean p2sh) {
        IAddress a;
        if (hasPrefix()) return false;
        try {
            a = createNew(keys, entropy, AddressLength.byIndicator(lengthIndicator), p2sh);
        } catch (InvalidAddressException e) {
            return false;
        }
        if (!a.getID().equals(ID)) return false;
        if (!a.getChecksum().equals(checksum)) return false;
        if (a.getVersion() != version) return false;
        return true;
    }


    @Override
    public String getChecksumGen() {
        byte[] check = EncryptionManager.sha224(Utils.fromBase64(ID));
        byte[] eoCheck = new byte[6];
        eoCheck[0] = check[check.length - 6];
        eoCheck[1] = check[check.length - 5];
        eoCheck[2] = check[check.length - 4];
        eoCheck[3] = check[check.length - 3];
        eoCheck[4] = check[check.length - 2];
        eoCheck[5] = check[check.length - 1];
        return Utils.toBase64(eoCheck);
    }

    private final byte VSIZE = 1;
    private final byte LSIZE = 1;
    private final byte P2SHSIZE = 1;
    private final byte PREFSIZE = 5;
    private final byte CHECKSIZE = 6;

    @Override
    public byte[] toByteArray() {
        int hashSize = 0;
        AddressLength l = AddressLength.byIndicator(lengthIndicator);
        if (l == null) return new byte[0];
        switch (l) {
            case SHA224:
                hashSize = 28;
                break;
            case SHA256:
                hashSize = 32;
                break;
            case SHA384:
                hashSize = 48;
                break;
            case SHA512:
                hashSize = 64;
                break;
        }
        byte[] array;
        if (hasPrefix()) {
            array = new byte[VSIZE + LSIZE + P2SHSIZE + PREFSIZE + CHECKSIZE + hashSize];
        } else {
            array = new byte[VSIZE + LSIZE + P2SHSIZE + CHECKSIZE + hashSize];
        }
        array[0] = version;
        array[1] = lengthIndicator;
        array[2] = (p2sh) ? (byte) 1 : (byte) 0;

        byte[] hash = Utils.fromBase64(ID);
        if (hasPrefix()) {
            try {
                byte[] prefix = getPrefix().getBytes("UTF-8");
                System.arraycopy(prefix, 0, array, 3, 5);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return new byte[0];
            }
            System.arraycopy(hash, 0, array, 8, hashSize);
        } else {
            System.arraycopy(hash, 0, array, 3, hashSize);
        }
        byte[] check = Utils.fromBase64(checksum);
        System.arraycopy(check, 0, array, array.length - 6, check.length);
        return array;
    }

    public static IAddress fromByteArray(byte[] array) {
        byte version = array[0];
        if (version != VERSION && version != PREFIXED_VERSION) return null;
        AddressLength l = AddressLength.byIndicator(array[1]);
        if (l == null) return null;
        int hashSize = 0;
        switch (l) {
            case SHA224:
                hashSize = 28;
                break;
            case SHA256:
                hashSize = 32;
                break;
            case SHA384:
                hashSize = 48;
                break;
            case SHA512:
                hashSize = 64;
                break;
        }
        byte[] hash = new byte[hashSize];
        String prefix = "";
        boolean p2sh = (array[2] == 1);
        if (version == PREFIXED_VERSION) {
            byte[] pbytes = new byte[5];
            System.arraycopy(array, 3, pbytes, 0, 5);
            try {
                prefix = new String(pbytes, "UTF-8");
                System.arraycopy(array, 8, hash, 0, hashSize);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            System.arraycopy(array, 3, hash, 0, hashSize);
        }
        byte[] check = new byte[6];
        System.arraycopy(array, array.length - 6, check, 0, 6);
        String ID = Utils.toBase64(hash);
        String checksum = Utils.toBase64(check);
        if (version == PREFIXED_VERSION) {
            //System.out.println("Creating prefixed address");
            try {
                return new NewAdd(version, ID, checksum, l.getIndicator(), prefix, p2sh);
            } catch (InvalidAddressException e) {
                e.printStackTrace();
            }
        } else {
            //System.out.println("creating non prefixed address");
            try {
                return new NewAdd(version, ID, checksum, l.getIndicator(), p2sh);
            } catch (InvalidAddressException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean hasPrefix() {
        return version == PREFIXED_VERSION;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean canSpendPrefixed(String key, String entropy, String prefix, boolean p2sh) {
        IAddress a;
        if (!hasPrefix()) return false;
        try {
            a = createNew(key, entropy, AddressLength.byIndicator(lengthIndicator), prefix, p2sh);
        } catch (InvalidAddressException e) {
            return false;
        }
        if (!a.getID().equals(ID)) return false;
        if (!a.getChecksum().equals(checksum)) return false;
        if (a.getVersion() != version) return false;
        if (!a.hasPrefix()) return false;
        if (!a.getPrefix().equals(getPrefix())) return false;
        return true;
    }

    @Override
    public boolean isP2SH() {
        return p2sh;
    }

    public static NewAdd createNew(String key, String entropy, AddressLength length, boolean p2sh) throws InvalidAddressException {
        byte[] keyB = Utils.fromBase64(key);
        byte[] ent = {};
        try {
            ent = entropy.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] keyEnt = new byte[keyB.length + ent.length];

        for (int i = 0; i < keyB.length; i++) {
            keyEnt[i] = keyB[i];
        }
        int p = 0;
        //System.out.println("size of ent array: " + ent.length);
        //System.out.println("size of keyEnt array: " + keyEnt.length);
        //System.out.println("Size of keyB array: " + keyB.length);
        for (int i = keyB.length; i < keyEnt.length; i++) {
            keyEnt[i] = ent[p];
            p++;
        }

        byte[] hash = {};
        switch (length) {
            case SHA224:
                hash = EncryptionManager.sha224(keyEnt);
                break;
            case SHA256:
                hash = EncryptionManager.sha3256(keyEnt);
                break;
            case SHA384:
                hash = EncryptionManager.sha384(keyEnt);
                break;
            case SHA512:
                hash = EncryptionManager.sha512(keyEnt);
                break;
        }
        byte[] check = EncryptionManager.sha224(hash);

        String ID = Utils.toBase64(hash);
        byte[] eoCheck = new byte[6];
        eoCheck[0] = check[check.length - 6];
        eoCheck[1] = check[check.length - 5];
        eoCheck[2] = check[check.length - 4];
        eoCheck[3] = check[check.length - 3];
        eoCheck[4] = check[check.length - 2];
        eoCheck[5] = check[check.length - 1];

        String checksum = Utils.toBase64(eoCheck);
        return new NewAdd(VERSION, ID, checksum, length.getIndicator(), p2sh);
    }

    public static NewAdd createNew(String key, String entropy, AddressLength length, String prefix, boolean p2sh) throws InvalidAddressException {
        byte[] keyB = Utils.fromBase64(key);
        byte[] ent = {};
        try {
            ent = entropy.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] keyEnt = new byte[keyB.length + ent.length];

        for (int i = 0; i < keyB.length; i++) {
            keyEnt[i] = keyB[i];
        }
        int p = 0;
        //System.out.println("size of ent array: " + ent.length);
        //System.out.println("size of keyEnt array: " + keyEnt.length);
        //System.out.println("Size of keyB array: " + keyB.length);
        for (int i = keyB.length; i < keyEnt.length; i++) {
            keyEnt[i] = ent[p];
            p++;
        }
        byte[] hash = {};
        switch (length) {

            case SHA224:
                hash = EncryptionManager.sha224(keyEnt);
                break;
            case SHA256:
                hash = EncryptionManager.sha3256(keyEnt);
                break;
            case SHA384:
                hash = EncryptionManager.sha384(keyEnt);
                break;
            case SHA512:
                hash = EncryptionManager.sha512(keyEnt);
                break;
        }

        byte[] check = EncryptionManager.sha224(hash);

        String ID = Utils.toBase64(hash);
        byte[] eoCheck = new byte[6];
        eoCheck[0] = check[check.length - 6];
        eoCheck[1] = check[check.length - 5];
        eoCheck[2] = check[check.length - 4];
        eoCheck[3] = check[check.length - 3];
        eoCheck[4] = check[check.length - 2];
        eoCheck[5] = check[check.length - 1];

        String checksum = Utils.toBase64(eoCheck);
        return new NewAdd(PREFIXED_VERSION, ID, checksum, length.getIndicator(), prefix, p2sh);
    }

}
