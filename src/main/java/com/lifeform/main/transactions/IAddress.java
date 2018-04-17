package com.lifeform.main.transactions;

public interface IAddress {

    /**
     * The chain was originally entirely strings, so this was used to create save-able Strings for the chain, it is now
     * "mostly" used for displaying addresses, although there are still operations performed on Strings
     *
     * @return String representing the address that can be used to recreate this address as well
     */
    String encodeForChain();

    /**
     * Version of the address, currently only versions between 100 and 127 are acceptable and won't break anything, future
     * updates may allow for any byte value to be valid
     *
     * @return version in byte format
     */
    byte getVersion();

    /**
     * "Payload" of the Address object, created through cryptographically hashing the public key and a salt "entropy" together
     *
     * @return hash in b64 notation
     */
    String getID();

    /**
     * Checksum to prove this address hasn't been tampered with or is not incorrectly typed. Currently, version 127 uses
     * a 2 byte checksum encoded in hex and returned by this function. All other addresses use a 6 byte checksum returned
     * in b64 format. In the future the checksum may become even larger but should remain in b64 format
     *
     * @return String encoded checksum
     */
    String getChecksum();

    /**
     * Call on an already created address to ensure it is a valid address (checksum, version, etc verify)
     *
     * @return true if the address is valid
     */
    boolean isValid();

    /**
     * For non prefixed addresses, will return false immediately if called on a prefixed address
     *
     * @param key     key thought to be used to create this address
     * @param entropy entropy thought to be used to create this address
     * @param p2sh    if address is p2sh
     * @param type    type of key for this address
     * @return true if the key and entropy create the same address as this one
     */
    boolean canSpend(String key, String entropy, boolean p2sh, KeyType type);

    /**
     * This will regen the checksum based on the ID. It does not reassign the checksum and is really only used for testing
     *
     * @return Regenerated checksum
     */
    String getChecksumGen();

    /**
     * Returns byte representation of this address
     *
     * @return byte array that can recreate this address
     */
    byte[] toByteArray();

    /**
     * NewAdds support prefixing, the old address system (version 127) does not and this will always return false on those
     * addresses. NewAdds also use a separate version for prefixed addresses (124) so you could just go by that if it's easier
     * rather than checking this. In fact, this literally equates to if(version == PREFIXED_VERSION)
     *
     * @return true if this address has a prefix
     */
    boolean hasPrefix();

    /**
     * @return prefix if it exists
     */
    String getPrefix();

    /**
     * For prefixed addresses, will return false immediately if called on a non prefixed address
     *
     * @param key     key thought to be used to create this address
     * @param entropy entropy thought to be used to create this address
     * @param prefix  prefix on address
     * @param p2sh    if address is p2sh
     * @param type    type of key for this address
     * @return true if the key, entropy, and prefix create the same address as this one
     */
    boolean canSpendPrefixed(String key, String entropy, String prefix, boolean p2sh, KeyType type);

    /**
     * For new P2SH addresses, as with hasPrefix it will always return false on 127 addresses
     *
     * @return true if address is a P2SH address
     */
    boolean isP2SH();

    /**
     * @return type of key used to create this address
     */
    KeyType getKeyType();

    /**
     * @return length of this address, always 224 for old addresses
     */
    AddressLength getAddressLength();
}
