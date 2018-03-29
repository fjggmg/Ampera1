package com.lifeform.main.data;

import amp.ByteTools;
import amp.group_ids.GroupID;

public class AmpIDs {
    //Block
    public static final long TRANSACTIONS_CID = ByteTools.amplifyClassID(0);
    public static final GroupID PAYLOAD_GID = new GroupID(1, 1, "Payload");
    public static final GroupID BLOCK_ID_GID = new GroupID(2, 2, "BlockID");
    //TODO probably don't need the next one
    public static final GroupID MERKLE_ROOT_GID = new GroupID(3, 3, "MerkleRoot");
    public static final GroupID PREV_ID_GID = new GroupID(4, 4, "PrevID");
    public static final GroupID SOLVER_GID = new GroupID(5, 5, "Solver");
    public static final GroupID TIMESTAMP_GID = new GroupID(6, 6, "Timestamp");
    public static final GroupID COINBASE_GID = ByteTools.amplifyGroupID(new GroupID(7, 7, "Coinbase"));
    public static final GroupID HEIGHT_GID = new GroupID(8, 8, "Height");
    //Transaction

    public static final GroupID MESSAGE_ID_GID = new GroupID(9, 9, "Message");
    public static final GroupID SIGS_REQUIRED_GID = new GroupID(10, 10, "SigsRequired");
    //public static final GroupID INPUTS_GID = ByteTools.amplifyGroupID(new GroupID(11,11,"Inputs"));
    public static final long INPUTS_CID = ByteTools.amplifyClassID(11);
    public static final long OUTPUTS_CID = ByteTools.amplifyClassID(12);
    //public static final GroupID OUTPUTS_GID = ByteTools.amplifyGroupID(new GroupID(12,12,"Outputs"));
    public static final GroupID KEY_SIG_MAP_GID = new GroupID(13, 13, "KeySigMap");
    //public static final GroupID KEYS_GID = new GroupID(14,14,"KEYS");
    public static final long KEYS_CID = 14;
    public static final GroupID ENTROPY_MAP_GID = new GroupID(15, 15, "EntropyMap");
    public static final GroupID TYPE_GID = new GroupID(16, 16, "Type");
    //public static final GroupID SIGS_GID = new GroupID(24,24,"Sigs");
    //public static final GroupID ENTROPY_GID = new GroupID(25,25,"Entropy");

    //TXIOs
    public static final GroupID AMOUNT_GID = new GroupID(17, 17, "Amount");
    public static final GroupID RECEIVER_GID = new GroupID(18, 18, "Receiver");
    public static final GroupID TOKEN_GID = new GroupID(19, 19, "Token");
    public static final GroupID INDEX_GID = new GroupID(20, 20, "Index");
    public static final GroupID TXTIMESTAMP_GID = new GroupID(21, 21, "Timestamp");
    public static final GroupID ID_GID = new GroupID(22, 22, "ID_GID");
    public static final GroupID TXIO_VER_GID = new GroupID(23, 23, "TXIO Version");
}
