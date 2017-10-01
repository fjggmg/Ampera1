package com.lifeform.main.data;

import com.lifeform.main.IKi;
import com.lifeform.main.data.files.StringFileHandler;
import org.bouncycastle.jcajce.provider.digest.SHA3;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by Bryan on 5/30/2017.
 */
public class EncryptionManager  implements IEncryptMan{

    public static final String KEY_FILE = "keys/key";
    public static final String KEY_PADDING = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE";
    public static final String KEY_PROTOCOL = "brainpoolp512t1";
    private IKi ki;
    public EncryptionManager(IKi ki){
        this.ki = ki;
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static void initStatic()
    {

    }


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

    static SHA3.DigestSHA3 md = new SHA3.Digest512();

    public static byte[] sha512NoNew(byte[] input) {

        md.update(input);
        byte[] hash = md.digest();
        md.reset();
        return hash;
    }

    public static String sha512(String input)
    {
        SHA3.DigestSHA3 md = null;
        try {
            md = new SHA3.Digest512();
            md.update(input.getBytes("UTF-8"));
            byte[] digest = md.digest();
            //logger.debug("Size of hash is: " + digest.length);
            return Utils.toBase64(digest);
        } catch (UnsupportedEncodingException e) {


        }
        return null;
    }

    public static byte[] sha512(byte[] input)
    {
        SHA3.DigestSHA3 md = new SHA3.Digest512();
        md.update(input);
        return md.digest();
    }

    public static String sha224(String input)
    {
        SHA3.DigestSHA3 md = null;
        try {
            md = new SHA3.Digest224();
            md.update(input.getBytes("UTF-8"));
            byte[] digest = md.digest();
            //logger.debug("Size of hash is: " + digest.length);
            return Utils.toBase64(digest);
        } catch (UnsupportedEncodingException e) {


        }
        return null;
    }
    public static String sha224Hex(String input)
    {
        SHA3.DigestSHA3 md = null;
        try {
            md = new SHA3.Digest224();
            md.update(input.getBytes("UTF-8"));
            byte[] digest = md.digest();
            //logger.debug("Size of hash is: " + digest.length);
            return Utils.toHexArray(digest);
        } catch (UnsupportedEncodingException e) {


        }
        return null;
    }

    public static String sha256(String input)
    {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(input.getBytes("UTF-8"));
            byte[] digest = md.digest();
            //logger.debug("Size of hash is: " + digest.length);
            return Utils.toBase64(digest);
        } catch (UnsupportedEncodingException e) {


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    private KeyPair pair;
    public KeyPair generateKeys() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC","BC");

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(KEY_PROTOCOL);
            gen.initialize(ecSpec,random);

            KeyPair pair = gen.generateKeyPair();
            this.pair = pair;
            return pair;
        } catch (NoSuchAlgorithmException e) {
            //logger.error(e.getMessage());
            //logger.debug("No EC algorithm found.");
        } catch (InvalidAlgorithmParameterException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }


    public KeyPair getKeys() {
        return pair;
    }

    public PublicKey pubKeyFromShortenedString(String key) {

        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("ECDSA","BC");
            key = KEY_PADDING + key;
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Utils.fromBase64(key));
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            return publicKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            e.printStackTrace();
            //logger.error(e.getMessage());
            //logger.debug("Could not read public key from string");
        }
        return null;
    }

    public static PublicKey pubKeyFromString(String key) {

        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("ECDSA","BC");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Utils.fromBase64(key));
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            return publicKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            e.printStackTrace();
            //logger.error(e.getMessage());
            //logger.debug("Could not read public key from string");
        }
        return null;
    }


    public PrivateKey privKeyFromString(String key) {


        byte[] encodedPrivateKey = Utils.fromBase64(key);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("ECDSA","BC");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                    encodedPrivateKey);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            return privateKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            //logger.error(e.getMessage());
            //logger.debug("Could not read private key from string");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
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
        fh.replaceLine(0,Utils.toBase64(getPublicKey().getEncoded()));
        fh = new StringFileHandler(ki,KEY_FILE + ".privk");
        fh.replaceLine(0,Utils.toBase64(getPrivateKey().getEncoded()));
        //trion.getDataMan().saveStringToFlatFile("pub." + KEY_FILE, Utils.toHexArray(getPublicKey().getEncoded()));
        //trion.getDataMan().saveStringToFlatFile("priv." + KEY_FILE, Utils.toHexArray(getPrivateKey().getEncoded()));
        //logger.debug("Saved keys.");
    }


    public String getPublicKeyString()
    {
        String key = Utils.toBase64(getPublicKey().getEncoded());
        key = key.replaceFirst(KEY_PADDING,"");
        return key;
    }
    public String sign(String toSign) {
        try {
            Signature sig = Signature.getInstance("SHA1withECDSA");
            sig.initSign(getPrivateKey());
            sig.update(toSign.getBytes("UTF-8"));

            return Utils.toBase64(sig.sign());

        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            //logger.error(e.getMessage());
            //logger.debug("Could not sign input string: " + toSign);
        }
        return null;
    }


    public static boolean verifySig(String signed, String sig,String pubKey) {
        try {
            //logger.debug("Signed: " + signed);
            //logger.debug("Sig: " + sig);
            //logger.debug("PubKey: " + pubKey);
            Signature signature = Signature.getInstance("SHA1withECDSA");
            signature.initVerify(pubKeyFromString(pubKey));
            signature.update(signed.getBytes("UTF-8"));
            return signature.verify(Utils.fromBase64(sig));
        } catch (UnsupportedEncodingException | SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;

    }
}
