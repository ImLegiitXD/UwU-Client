package wtf.uwu.gui.font;

import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import wtf.uwu.UwU;

import java.awt.*;
import java.io.InputStream;

public enum Fonts {
    interBold("inter/Inter_Bold"),
    interMedium("inter/Inter_Medium"),
    interRegular("inter/Inter_Regular"),
    interSemiBold("inter/Inter_SemiBold"),
    psRegular("product-sans/Regular"),
    psBold("product-sans/Bold"),
    nursultan("others/Nursultan"),
    Tahoma("others/Exhi"),
    skeet("others/skeet"),
    noti("others/noti"),
    noti2("others/noti2"),
    session("others/session"),
    session2("others/session2"),
    neverlose("others/nlicon"),
    sfui("others/sfui");

    private final String file;
    private final Float2ObjectMap<FontRenderer> fontMap = new Float2ObjectArrayMap<>();

    Fonts(String file) {
        this.file = file;
    }

    public FontRenderer get(float size) {
        return this.fontMap.computeIfAbsent(size, font -> {
            try {
                return create(this.file, size, true);
            } catch (Exception var5) {
                throw new RuntimeException("Unable to load font: " + this, var5);
            }
        });
    }

    public FontRenderer get(float size, boolean antiAlias) {
        return this.fontMap.computeIfAbsent(size, font -> {
            try {
                return create(this.file, size, antiAlias);
            } catch (Exception var5) {
                throw new RuntimeException("Unable to load font: " + this, var5);
            }
        });
    }

    public FontRenderer create(String file, float size, boolean antiAlias) {
        try {
            String path = "/assets/minecraft/" + UwU.INSTANCE.getClientName().toLowerCase() + "/font/" + file + ".ttf";
            InputStream in = UwU.class.getResourceAsStream(path);

            if (in == null) {
                System.err.println("WARNING: nose encontro la fuente: " + path + " — se usara arial por defecto.");
                Font defaultFont = new Font("Arial", Font.PLAIN, (int) size);
                return new FontRenderer(defaultFont, antiAlias);
            }

            Font font = Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(Font.PLAIN, size);
            return new FontRenderer(font, antiAlias);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to create font", ex);
        }
    }
}
