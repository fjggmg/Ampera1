package com.ampex.main.transactions.addresses;

import com.ampex.amperabase.AddressLength;
import com.ampex.amperabase.IAddress;
import com.ampex.amperabase.KeyType;
import com.ampex.main.data.EncryptionManager;
import com.ampex.main.data.Utils;

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
    private final byte lengthIndicator;
    private final boolean p2sh;
    private final KeyType keyType;

    public NewAdd(byte version, String ID, String checksum, byte lengthIndicator, boolean p2sh, KeyType keyType) throws InvalidAddressException {
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
        //System.out.println("Created address with key type: " + keyType);
        this.keyType = keyType;
    }

    public NewAdd(byte version, String ID, String checksum, byte lengthIndicator, String prefix, boolean p2sh, KeyType keyType) throws InvalidAddressException {
        this(version, ID, checksum, lengthIndicator, p2sh, keyType);
        if (version != PREFIXED_VERSION) throw new InvalidAddressException("Prefix used with non prefix version");
        this.prefixed = true;
        if (prefix.length() != 5) throw new InvalidAddressException("Prefix is not 5 characters long");
        if (prefix.contains(" ")) throw new InvalidAddressException("Prefixes must not have spaces");
        try {
            byte[] bytes = prefix.getBytes("UTF-8");
            String checkString = new String(bytes, "UTF-8");
            if (!checkString.equals(prefix)) {
                throw new InvalidAddressException("Prefix failed to encode to UTF-8 correctly. Check special characters");
            }
        } catch (UnsupportedEncodingException e) {
            throw new InvalidAddressException("Prefix is not UTF-8 encodable");
        }
        this.prefix = prefix;
    }

    @Override
    public String encodeForChain() {
        if (prefixed)
            return version + Utils.byteToHex(lengthIndicator) + Utils.byteToHex(keyType.getValue()) + ((p2sh) ? 1 : 0) + prefix + ID + checksum;
        return version + Utils.byteToHex(lengthIndicator) + Utils.byteToHex(keyType.getValue()) + ((p2sh) ? 1 : 0) + ID + checksum;
    }

    public static IAddress decodeFromChain(String encoded) {
        char[] eChar = encoded.toCharArray();
        char[] vChar = {eChar[0], eChar[1], eChar[2]};
        byte version = Byte.parseByte(new String(vChar));
        char[] al = new char[2];
        al[0] = eChar[3];
        al[1] = eChar[4];
        AddressLength l = AddressLength.byIndicator(Utils.hexToByte(new String(al)));
        char[] kt = new char[2];
        kt[0] = eChar[5];
        kt[1] = eChar[6];
        KeyType type = KeyType.byValue(Utils.hexToByte(new String(kt)));
        //System.out.println("Decoded key code: " + Utils.hexToByte(new String(kt)));
        //System.out.println("Decoded key type: " + type);
        if (l == null) return null;
        boolean p2sh = (eChar[7] == '1');
        if (version == VERSION) {
            StringBuilder ID = new StringBuilder();
            for (int i = 8; i < eChar.length - 8; i++) {
                ID.append(eChar[i]);
            }
            char[] cChar = {eChar[eChar.length - 8], eChar[eChar.length - 7], eChar[eChar.length - 6], eChar[eChar.length - 5], eChar[eChar.length - 4], eChar[eChar.length - 3], eChar[eChar.length - 2], eChar[eChar.length - 1]};

            String checksum = new String(cChar);

            try {
                return new NewAdd(version, ID.toString(), checksum, l.getIndicator(), p2sh, type);
            } catch (InvalidAddressException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            StringBuilder prefix = new StringBuilder();
            for (int i = 8; i < 13; i++) {
                prefix.append(eChar[i]);
            }
            StringBuilder ID = new StringBuilder();
            for (int i = 13; i < eChar.length - 8; i++) {
                ID.append(eChar[i]);
            }
            char[] cChar = {eChar[eChar.length - 8], eChar[eChar.length - 7], eChar[eChar.length - 6], eChar[eChar.length - 5], eChar[eChar.length - 4], eChar[eChar.length - 3], eChar[eChar.length - 2], eChar[eChar.length - 1]};

            String checksum = new String(cChar);

            try {
                return new NewAdd(version, ID.toString(), checksum, l.getIndicator(), prefix.toString(), p2sh, type);
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
        if (checksum == null) return false;
        if (!checksum.equals(this.checksum)) return false;

        IAddress a = NewAdd.fromByteArray(toByteArray());
        if (a == null) return false;
        return a.encodeForChain().equals(encodeForChain());
    }

    @Override
    public boolean canSpend(String keys, String entropy, boolean p2sh, KeyType type) {
        IAddress a;
        if (hasPrefix()) return false;
        try {
            a = createNew(keys, entropy, AddressLength.byIndicator(lengthIndicator), p2sh, type);
        } catch (InvalidAddressException e) {
            return false;
        }
        if (!a.getID().equals(ID)) return false;
        if (!a.getChecksum().equals(checksum)) return false;
        return a.getVersion() == version;
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

    private static final byte VSIZE = 1;
    private static final byte LSIZE = 1;
    private static final byte P2SHSIZE = 1;
    private static final byte KTSIZE = 1;
    private static final byte PREFSIZE = 5;
    private static final byte CHECKSIZE = 6;

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
            array = new byte[VSIZE + LSIZE + KTSIZE + P2SHSIZE + PREFSIZE + CHECKSIZE + hashSize];
        } else {
            array = new byte[VSIZE + LSIZE + KTSIZE + P2SHSIZE + CHECKSIZE + hashSize];
        }
        array[0] = version;
        array[1] = lengthIndicator;
        array[2] = (p2sh) ? (byte) 1 : (byte) 0;
        array[3] = keyType.getValue();
        byte[] hash = Utils.fromBase64(ID);
        if (hasPrefix()) {
            try {
                byte[] prefix = getPrefix().getBytes("UTF-8");
                System.arraycopy(prefix, 0, array, 4, 5);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return new byte[0];
            }
            System.arraycopy(hash, 0, array, 9, hashSize);
        } else {
            System.arraycopy(hash, 0, array, 4, hashSize);
        }
        byte[] check = Utils.fromBase64(checksum);
        System.arraycopy(check, 0, array, array.length - 6, check.length);
        return array;
    }

    public static IAddress fromByteArray(byte[] array) {
        if (array.length == 0) return null;
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
        KeyType type = KeyType.byValue(array[3]);
        if (version == PREFIXED_VERSION) {
            byte[] pbytes = new byte[5];
            System.arraycopy(array, 4, pbytes, 0, 5);
            try {
                prefix = new String(pbytes, "UTF-8");
                System.arraycopy(array, 9, hash, 0, hashSize);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            System.arraycopy(array, 4, hash, 0, hashSize);
        }
        byte[] check = new byte[6];
        System.arraycopy(array, array.length - 6, check, 0, 6);
        String ID = Utils.toBase64(hash);
        String checksum = Utils.toBase64(check);
        if (version == PREFIXED_VERSION) {
            //System.out.println("Creating prefixed address");
            try {
                return new NewAdd(version, ID, checksum, l.getIndicator(), prefix, p2sh, type);
            } catch (InvalidAddressException e) {
                e.printStackTrace();
            }
        } else {
            //System.out.println("creating non prefixed address");
            try {
                return new NewAdd(version, ID, checksum, l.getIndicator(), p2sh, type);
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
    public boolean canSpendPrefixed(String key, String entropy, String prefix, boolean p2sh, KeyType keyType) {
        IAddress a;
        if (!hasPrefix()) return false;
        try {
            a = createNew(key, entropy, AddressLength.byIndicator(lengthIndicator), prefix, p2sh, keyType);
        } catch (InvalidAddressException e) {
            return false;
        }
        if (!a.getID().equals(ID)) return false;
        if (!a.getChecksum().equals(checksum)) return false;
        if (a.getVersion() != version) return false;
        if (!a.hasPrefix()) return false;
        return a.getPrefix().equals(getPrefix());
    }

    @Override
    public boolean isP2SH() {
        return p2sh;
    }

    @Override
    public KeyType getKeyType() {
        return keyType;
    }

    @Override
    public AddressLength getAddressLength() {
        return AddressLength.byIndicator(lengthIndicator);
    }

    public static NewAdd createNew(String key, String entropy, AddressLength length, boolean p2sh, KeyType type) throws InvalidAddressException {
        if (length == null) throw new InvalidAddressException("Null address length");
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
        return new NewAdd(VERSION, ID, checksum, length.getIndicator(), p2sh, type);
    }

    public static NewAdd createNew(String key, String entropy, AddressLength length, String prefix, boolean p2sh, KeyType type) throws InvalidAddressException {
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
        return new NewAdd(PREFIXED_VERSION, ID, checksum, length.getIndicator(), prefix, p2sh, type);
    }

}
