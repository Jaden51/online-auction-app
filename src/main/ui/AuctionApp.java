package ui;

import model.AuctionItemList;
import model.UserItemList;
import persistance.JsonReader;
import persistance.JsonWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.util.Scanner;

// The main page of the auction app. This class initializes the
// data and shows the main menu. The menu displayed here
// can lead users to the other parts of the app
public class AuctionApp extends JFrame implements ItemListener {

    JPanel storeArea;

    public static final int WIDTH = 1000;
    public static final int HEIGHT = 700;

    private UploadItem uploadItem;
    private UserStore userStore;
    private AuctionStore auctionStore;

    private UserItemList userItemList;
    private AuctionItemList auctionItemList;

    private Scanner keyboard;

    protected static final String JSON_USER_STORE = "./data/userStore.json";
    protected static final String JSON_AUCTION_STORE = "./data/auctionStore.json";

    protected JsonWriter jsonWriterUserStore;
    protected JsonReader jsonReaderUserStore;
    protected JsonWriter jsonWriterAuctionStore;
    protected JsonReader jsonReaderAuctionStore;

    private JPanel userStoreCard;
    private JPanel auctionStoreCard;
    private JPanel uploadItemCard;
    private JButton saveButton;
    private JButton loadButton;

    // EFFECTS: runs the app
    public AuctionApp() {
        initializeJson();
        initializeFields();
        initializeGraphics();
    }

    // MODIFIES: this
    // EFFECTS: initializes fields
    private void initializeFields() {
        keyboard = new Scanner(System.in);
        userItemList = new UserItemList("jh51");
        loadItemsAuctionStore();
    }

    // MODIFIES: this
    // EFFECTS: initializes the JSON objects
    private void initializeJson() {
        jsonWriterUserStore = new JsonWriter(JSON_USER_STORE);
        jsonReaderUserStore = new JsonReader(JSON_USER_STORE);
        jsonWriterAuctionStore = new JsonWriter(JSON_AUCTION_STORE);
        jsonReaderAuctionStore = new JsonReader(JSON_AUCTION_STORE);
    }

    private void initializeGraphics() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        createStores();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createStores() {
        JPanel switchCard = new JPanel();
        JComboBox cb = new JComboBox(new String[]{"Your Items", "Store", "Upload Item"});
        cb.setEditable(false);
        cb.addItemListener(this);
        switchCard.add(cb);

        userStoreCard = new JPanel(new GridLayout(8, 1));
        userStore = new UserStore(userItemList, auctionItemList, userStoreCard);

        auctionStoreCard = new JPanel(new GridLayout(8, 1));
        auctionStore = new AuctionStore(userItemList, auctionItemList, auctionStoreCard);

        uploadItemCard = new JPanel();
        uploadItem = new UploadItem(userItemList, auctionItemList, uploadItemCard);

        storeArea = new JPanel(new CardLayout());
        storeArea.add(userStoreCard, "Your Items");
        storeArea.add(auctionStoreCard, "Store");
        storeArea.add(uploadItemCard, "Upload Item");

        add(switchCard, BorderLayout.PAGE_START);
        add(storeArea, BorderLayout.CENTER);
        createPersistenceButtons();

    }

    private void createPersistenceButtons() {
        JPanel persistenceArea = new JPanel(new GridLayout(1, 2));
        saveButton = new JButton("Save Items");
        saveButton.addActionListener(e -> saveItemsUserStore());
        loadButton = new JButton("Load Items");
        loadButton.addActionListener(e -> loadItemsUserStore());
        persistenceArea.add(saveButton);
        persistenceArea.add(loadButton);
        add(persistenceArea, BorderLayout.SOUTH);
    }

    // MODIFIES: this
    // EFFECTS: loads the user store from file
    protected void loadItemsAuctionStore() {
        try {
            auctionItemList = jsonReaderAuctionStore.readAuctionList();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to read from file: " + JSON_AUCTION_STORE);
        }
    }

    // EFFECTS: saves the workroom to file
    protected void saveItemsAuctionStore() {
        try {
            jsonWriterAuctionStore.open();
            jsonWriterAuctionStore.write(auctionItemList);
            jsonWriterAuctionStore.close();
        } catch (FileNotFoundException e) {
            System.out.println("Unable to write to file: " + JSON_AUCTION_STORE);
        }
    }

    // MODIFIES: this
    // EFFECTS: loads the user store from file
    protected void loadItemsUserStore() {
        try {
            userItemList = jsonReaderUserStore.readUserList();
            updateLists();
            JOptionPane.showMessageDialog(this, "Loaded " + userItemList.getUsername() + " from " + JSON_USER_STORE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to read to file: " + JSON_USER_STORE);
        }
    }

    // EFFECTS: saves the workroom to file
    protected void saveItemsUserStore() {
        try {
            jsonWriterUserStore.open();
            jsonWriterUserStore.write(userItemList);
            jsonWriterUserStore.close();
            JOptionPane.showMessageDialog(this, "Saved " + userItemList.getUsername() + " to " + JSON_USER_STORE);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Unable to write to file: " + JSON_USER_STORE);
        }
    }

    // EFFECTS: updates the lists in the stores to match the data persistence
    public void updateLists() {
        userStore.updateLists(userItemList, auctionItemList);
        uploadItem.updateLists(userItemList, auctionItemList);
        auctionStore.updateLists(userItemList, auctionItemList);
    }

    // EFFECTS: switches store cards
    @Override
    public void itemStateChanged(ItemEvent e) {
        CardLayout cl = (CardLayout) (storeArea.getLayout());
        cl.show(storeArea, (String) e.getItem());
        Object item = e.getItem();
        if (("Your Items").equals(item)) {
            userStore.updateJList();
            userStore.updateLabels();
        } else if (("Store").equals(item)) {
            auctionStore.updateJList();
            auctionStore.updateLabels();
            updateLists();
            saveItemsAuctionStore();
        } else if (("Upload Item").equals(item)) {
            saveItemsAuctionStore();
        }
    }
}
