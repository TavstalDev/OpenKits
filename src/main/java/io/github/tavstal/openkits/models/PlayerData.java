package io.github.tavstal.openkits.models;

import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstal.openkits.gui.KitsGUI;
import io.github.tavstal.openkits.gui.PreviewGUI;
import org.bukkit.entity.Player;

/**
 * Represents the data associated with a player in the OpenKits plugin.
 */
public class PlayerData {
    private final Player _player;
    private boolean _isGUIOpened;
    private SGMenu _kitsMenu;
    private SGMenu _previewMenu;
    private int _kitsPage;
    private int _previewPage;
    private Kit _previewKit;

    /**
     * Constructs a new PlayerData object for the specified player.
     *
     * @param player the player associated with this data
     */
    public PlayerData(Player player) {
        _player = player;
        _isGUIOpened = false;
        _kitsMenu = null;
        _previewMenu = null;
        _kitsPage = 0;
        _previewPage = 0;
    }

    /**
     * Checks if the GUI is opened for the player.
     *
     * @return true if the GUI is opened, false otherwise
     */
    public boolean isGUIOpened() {
        return _isGUIOpened;
    }

    /**
     * Sets the GUI opened status for the player.
     *
     * @param isGUIOpened the new GUI opened status
     */
    public void setGUIOpened(boolean isGUIOpened) {
        _isGUIOpened = isGUIOpened;
    }

    /**
     * Gets the kits menu for the player. If the kits menu is not initialized, it creates a new one.
     *
     * @return the kits menu
     */
    public SGMenu getKitsMenu() {
        if (_kitsMenu == null) {
            _kitsMenu = KitsGUI.create(_player);
        }
        return _kitsMenu;
    }

    /**
     * Gets the preview menu for the player. If the preview menu is not initialized, it creates a new one.
     *
     * @return the preview menu
     */
    public SGMenu getPreviewMenu() {
        if (_previewMenu == null) {
            _previewMenu = PreviewGUI.create(_player);
        }
        return _previewMenu;
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
