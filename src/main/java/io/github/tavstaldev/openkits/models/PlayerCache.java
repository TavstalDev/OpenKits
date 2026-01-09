package io.github.tavstaldev.openkits.models;

import org.bukkit.entity.Player;

/**
 * Represents the data associated with a player in the OpenKits plugin.
 */
public class PlayerCache {
    private final Player _player;
    private int _kitsPage;
    private int _previewPage;
    private Kit _previewKit;

    /**
     * Constructs a new PlayerCache object for the specified player.
     *
     * @param player the player associated with this data
     */
    public PlayerCache(Player player) {
        _player = player;
        _kitsPage = 0;
        _previewPage = 0;
    }

    /**
     * Gets the current page number of the 'kits menu'.
     *
     * @return the current kits page number
     */
    public int getKitsPage() {
        return _kitsPage;
    }

    /**
     * Sets the current page number of the 'kits menu'.
     *
     * @param kitsPage the new kits page number
     */
    public void setKitsPage(int kitsPage) {
        _kitsPage = kitsPage;
    }

    /**
     * Gets the current page number of the preview menu.
     *
     * @return the current preview page number
     */
    public int getPreviewPage() {
        return _previewPage;
    }

    /**
     * Sets the current page number of the preview menu.
     *
     * @param previewPage the new preview page number
     */
    public void setPreviewPage(int previewPage) {
        _previewPage = previewPage;
    }

    /**
     * Gets the kit currently being previewed by the player.
     *
     * @return the kit currently being previewed
     */
    public Kit getPreviewKit() {
        return _previewKit;
    }

    /**
     * Sets the kit to be previewed by the player.
     *
     * @param kit the kit to be previewed
     */
    public void setPreviewKit(Kit kit) {
        _previewKit = kit;
    }
}
