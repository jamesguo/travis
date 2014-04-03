package pt.ua.travis.gui.main;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class DrawerItem implements DrawerView {

    public final int itemID;

    public final int itemNameID;

    public final int itemImageID;

    public final String itemCounter;

    public DrawerItem(int itemID, int itemNameID, int itemImageID) {
        this.itemID = itemID;
        this.itemNameID = itemNameID;
        this.itemImageID = itemImageID;
        this.itemCounter = null;
    }

    public DrawerItem(int itemID, int itemNameID, int itemImageID, int itemCounter) {
        this.itemID = itemID;
        this.itemNameID = itemNameID;
        this.itemImageID = itemImageID;
        this.itemCounter = String.valueOf(itemCounter);
    }
}
