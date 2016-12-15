package at.buchberger.keyexchangeapp;

/**
 * Created by daniel on 07.11.2016.
 */

public class PubKey {

    private String schluessel;
    private String playerName;
    private String version;
    private String checksum;
    private String fingerprint;
    private Boolean signed;
    private byte[] signature;

    public String getPlayerName() {
        return playerName;
    }
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getSchluessel() {
        return schluessel;
    }
    public void setSchluessel(String score) {
        this.schluessel = score;
    }

    public String getVersion() {
        return version;
    }
    public void setVersion(String score) {
        this.version = score;
    }

    public String getFingerprint() {
        return fingerprint;
    }
    public void setFingerprint(String score) {
        this.fingerprint = score;
    }

    public String getChecksum() {
        return checksum;
    }
    public void setChecksum(String score) {
        this.checksum = score;
    }

    public Boolean getSigned(){return signed;}
    public void setSigned(Boolean sign){this.signed = sign;}

    public byte[] getSignature(){return signature;}
    public void setSignature(byte[] signature){this.signature=signature;}
}