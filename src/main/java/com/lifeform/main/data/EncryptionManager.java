package com.lifeform.main.data;

import com.lifeform.main.IKi;
import com.lifeform.main.data.files.StringFileHandler;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by Bryan on 5/30/2017.
 */
public class EncryptionManager  implements IEncryptMan{

    public static final String KEY_FILE = "key";
    private IKi ki;
    public EncryptionManager(IKi ki){
        this.ki = ki;
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static void initStatic()
    {
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    static MessageDigest md;

    public static int sha256NoNew(String input,byte[] buf)
    {
        if(md == null) return 0;
        try {
            md.update(input.getBytes("UTF-8"));

            //logger.debug("Size of hash is: " + digest.length);
            int r = md.digest(buf,0,buf.length);
            md.reset();
            return r;
        } catch (UnsupportedEncodingException e) {


        } catch (DigestException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String sha256(String input)
    {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(input.getBytes("UTF-8"));
            byte[] digest = md.digest();
            //logger.debug("Size of hash is: " + digest.length);
            return Utils.toHexArray(digest);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {


        }
        return null;
    }


    private KeyPair pair;
    public KeyPair generateKeys() {
        try {
            //TODO: investigate if we should be using bouncycastle
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            gen.initialize(256,random);

            KeyPair pair = gen.generateKeyPair();
            this.pair = pair;
            return pair;
        } catch (NoSuchAlgorithmException e) {
            //logger.error(e.getMessage());
            //logger.debug("No EC algorithm found.");
        }
        return null;
    }


    public KeyPair getKeys() {
        return pair;
    }


    public PublicKey pubKeyFromString(String key) {

        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("ECDSA");

            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Utils.toByteArray(key));
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            return publicKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            //logger.error(e.getMessage());
            //logger.debug("Could not read public key from string");
        }
        return null;
    }


    public PrivateKey privKeyFromString(String key) {


        byte[] encodedPrivateKey = Utils.toByteArray(key);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("ECDSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                    encodedPrivateKey);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            return privateKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            //logger.error(e.getMessage());
            //logger.debug("Could not read private key from string");
        }
        return null;

    }


    public PublicKey getPublicKey() {
        if(pair == null) return null;
        return pair.getPublic();
    }


    public PrivateKey getPrivateKey() {
        if(pair == null) return null;
        return pair.getPrivate();
    }


    public KeyPair loadKeys() {

        //PublicKey pubKey = pubKeyFromString( trion.getDataMan().getStringFileHandler("pub." + KEY_FILE).getLine(0));
        StringFileHandler fh = new StringFileHandler(ki,KEY_FILE + ".pubk");
        PublicKey pubKey = pubKeyFromString(fh.getLine(0));
        fh = new StringFileHandler(ki,KEY_FILE + ".privk");
        PrivateKey privKey = privKeyFromString(fh.getLine(0));
        //PrivateKey privKey = privKeyFromString( trion.getDataMan().getStringFileHandler("priv." + KEY_FILE).getLine(0));
        KeyPair pair = new KeyPair(pubKey,privKey);
        this.pair = pair;
        return pair;
    }


    public void saveKeys()
    {
        StringFileHandler fh = new StringFileHandler(ki,KEY_FILE + ".pubk");
        fh.replaceLine(0,Utils.toHexArray(getPublicKey().getEncoded()));
        fh = new StringFileHandler(ki,KEY_FILE + ".privk");
        fh.replaceLine(0,Utils.toHexArray(getPrivateKey().getEncoded()));
        //trion.getDataMan().saveStringToFlatFile("pub." + KEY_FILE, Utils.toHexArray(getPublicKey().getEncoded()));
        //trion.getDataMan().saveStringToFlatFile("priv." + KEY_FILE, Utils.toHexArray(getPrivateKey().getEncoded()));
        //logger.debug("Saved keys.");
    }


    public String sign(String toSign) {
        try {
            Signature sig = Signature.getInstance("SHA1withECDSA");
            sig.initSign(getPrivateKey());
            sig.update(toSign.getBytes("UTF-8"));

            return Utils.toHexArray(sig.sign());

        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            //logger.error(e.getMessage());
            //logger.debug("Could not sign input string: " + toSign);
        }
        return null;
    }


    public boolean verifySig(String signed, String sig,String pubKey) {
        try {
            //logger.debug("Signed: " + signed);
            //logger.debug("Sig: " + sig);
            //logger.debug("PubKey: " + pubKey);
            Signature signature = Signature.getInstance("SHA1withECDSA");
            signature.initVerify(pubKeyFromString(pubKey));
            signature.update(signed.getBytes("UTF-8"));
            return signature.verify(Utils.toByteArray(sig));
        } catch (UnsupportedEncodingException | SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;

    }
}
