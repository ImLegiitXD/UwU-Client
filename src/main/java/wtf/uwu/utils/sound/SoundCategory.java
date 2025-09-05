package wtf.uwu.utils.sound;


public enum SoundCategory {
    MASTER("master", 1.0f),
    MUSIC("music", 0.7f),
    AMBIENT("ambient", 1.0f),
    PLAYER("player", 0.9f),
    BLOCKS("blocks", 0.8f),
    HOSTILE("hostile", 1.0f),
    NEUTRAL("neutral", 1.0f);

    private final String name;
    private float defaultVolume;

    SoundCategory(String name, float defaultVolume) {
        this.name = name;
        this.defaultVolume = defaultVolume;
    }

    public String getName() {
        return name;
    }

    public float getDefaultVolume() {
        return defaultVolume;
    }
}