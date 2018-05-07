package com.lifeform.main;

import amp.Amplet;
import amp.ByteTools;
import amp.HeadlessAmplet;
import amp.HeadlessPrefixedAmplet;
import amp.database.XodusAmpMap;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition;
import com.jfoenix.validation.RequiredFieldValidator;
import com.lifeform.main.adx.Order;
import com.lifeform.main.adx.OrderStatus;
import com.lifeform.main.adx.Pairs;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.blockchain.ChainManager;
import com.lifeform.main.blockchain.GPUMiner;
import com.lifeform.main.data.*;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.TransactionDataRequest;
import com.lifeform.main.network.TransactionPacket;
import com.lifeform.main.transactions.*;
import engine.binary.Binary;
import engine.data.WritableMemory;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static javafx.animation.Interpolator.EASE_BOTH;

/**
 * Fuck this file. Fuck JavaFX. Fuck Oracle. I fucking hate dealing with this horse shit and it's
 * very clear that my coding style does not coincide well with javafx, hence all the fucking retardation
 * that follows. This file makes fuck-all sense. You have been warned.
 */
public class NewGUI {
    //region javafx horsesh
    static boolean close = false;
    public JFXTextField entropyField;
    public Label blocksFoundLabel;
    public Pane blockPane;
    public JFXButton backToBE;
    public JFXButton previousBlock;
    public JFXButton nextBlock;
    public Label blockHeight;
    public Label blockID;
    public Label solver;
    public Label numberOfTransactions;
    public Pane poolPane;
    public JFXTextField ipField;
    public JFXButton poolConnect;
    public Label shares;
    public Label nextPayment;
    public HBox miningDataHbox;
    public JFXTextField fromBlock;
    public JFXTextField toBlock;
    public JFXButton goBE;
    public JFXTextField paytoAddress;
    public Pane poolRelay;
    public JFXTextField searchBox;
    public Label amountOfTransactions;
    public Label blockTimestamp;
    public Label poolConnected;
    public Label poolNOC;
    public JFXSlider poolDynamicFeeSlider;
    public JFXToggleButton poolDynamicFee;
    public JFXTextField poolStaticFee;
    public JFXButton poolDisconnect;
    public JFXButton resetColors;
    public JFXSlider payoutSlider;
    public Label poolHashrate;
    public Label localShares;
    public JFXToggleButton autoMine;
    public Label currentPoolShares;
    public Label estimatedNextPayout;
    public JFXToggleButton pplnsClient;
    public JFXToggleButton pplnsServer;
    public HBox devicesBox;
    public Hyperlink faqLink;
    public Hyperlink issuesPageLink;
    public Hyperlink discordServerLink;
    public JFXButton exchangeBuy;
    public JFXButton exchangeSell;
    public Pane exchangePane;
    public JFXTextField amtOnOffer;
    public Label lastTradeAmount;
    public JFXTextField exPrice;
    public JFXComboBox<Label> pairs;
    public JFXListView<Order> obBuyTotal;
    public JFXListView<Order> obBuySize;
    public JFXListView<Order> obBuyPrice;
    public JFXListView<Order> obSellPrice;
    public JFXListView<Order> obSellSize;
    public JFXListView<Order> obSellTotal;
    public Label buyLabel;
    public Label sellLabel;
    public JFXListView<Order> obRecentPrice;
    public JFXListView<Order> obRecentAmount;
    public JFXListView<Order> obRecentDirection;
    public JFXListView<Order> activePrice;
    public JFXListView<Order> activeAmount;
    public JFXPasswordField confirmPassword;
    public VBox addressControls;
    public JFXComboBox<Label> addressLength;
    public JFXTextField prefixField;
    public JFXButton copyPublicKey;
    public JFXButton loadMSAddress;
    public JFXButton saveMSAddress;
    public VBox walletBox;
    public JFXButton loadTransaction;
    public JFXButton copySelectedAdd;
    public Label exchangeTotalPurchase;
    private CandlestickGraph exchangeGraph;
    public VBox passwordVbox;
    public VBox exchangeGraphBox;
    public Label currentOpen;
    public Label currentHigh;
    public Label currentLow;
    public Label currentClose;
    public JFXComboBox<Label> unitSelectorPrice;
    public JFXComboBox<Label> unitSelectorAmount;
    public Label tokenAmount;
    public Label tokenPrice;
    public JFXButton orderHistory;
    public Pane ohPane;
    public JFXListView<String> ohPrice;
    public JFXListView<String> ohAmount;
    public JFXListView<String> ohDate;
    public JFXListView<String> ohDirection;
    public JFXListView<JFXButton> ohCancel;
    public JFXButton backToEx;
    public PieChart ohPortfolio;
    public VBox ohVbox;
    public HBox adxBox;
    public Label pnlLabel;
    public LineChart<String, Number> pnlGraph;
    public JFXButton marketBuy;
    public JFXButton limitBuy;
    public HBox priceHBox;
    public VBox exControlsBox;
    public JFXButton singleSig;
    public JFXButton multiSig;
    public JFXComboBox<Label> keyType;
    public VBox multiSigVbox;
    public JFXTextField addPubKey;
    public JFXButton addKeyBtn;
    public JFXComboBox<Label> sigsRequired;
    public JFXButton clearKeys;
    //endregion
    private BigInteger unitMultiplierPrice = BigInteger.valueOf(100);
    private BigInteger unitMultiplierAmount = BigInteger.valueOf(100);
    private IKi ki;
    private ObservableMap<Token, BigInteger> tokenValueMap = FXCollections.observableMap(new HashMap<Token, BigInteger>());
    private volatile boolean isFinal = false;
    private volatile boolean run = true;
    private Map<String, BigInteger> heightMap = new HashMap<>();
    private List<ITrans> sTrans = new CopyOnWriteArrayList<>();
    private boolean createMultiSig = false;

    //region multisig shit
    private ITrans multiSigTransCache = null;
    private Binary multiSigBinaryCache = null;
    private WritableMemory multiSigWMCache = null;

    //endregion
    public NewGUI() {
        ki = Ki.getInstance();
        transactions = FXCollections.observableArrayList();
        for (Token t : Token.values()) {
            tokenValueMap.put(t, BigInteger.ZERO);
        }
        ki.setGUIHook(this);
        try {
            if (guiXAM.getBytes("blocksFound") != null)
                blocksFoundInt = HeadlessAmplet.create((guiXAM.getBytes("blocksFound"))).getNextInt();

            if (guiXAM.getBytes("nGUI") == null) {
                guiXAM.putBytes("newGUI", "firstRun".getBytes("UTF-8"));
                if (guiXAM.getBytes("heightMap") != null) {
                    HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(guiXAM.getBytes("heightMap"));
                    while (hpa.hasNextElement()) {
                        heightMap.put(new String(hpa.getNextElement(), "UTF-8"), new BigInteger(hpa.getNextElement()));
                    }
                }

            }
        } catch (Exception e) {
            ki.getMainLog().error("Error in GUI data loading: ", e);
        }
        if (!ki.getOptions().pool) {
            Thread t = new Thread() {
                public void run() {
                    setDaemon(true);
                    while (run) {
                        if (close) {
                            try {
                                ki.close();
                            } catch (Exception e) {
                                ki.getMainLog().error("Could not close correctly ", e);
                            }
                            continue;
                        }
                        isFinal = false;
                        for (Token t : Token.values()) {
                            tokenValueMap.put(t, BigInteger.ZERO);
                        }
                        List<Output> utxos = null;
                        try {
                            utxos = ki.getTransMan().getUTXOs(ki.getAddMan().getMainAdd(), false);

                        } catch (Exception e) {
                            //environment inoperative or other issue, ignore for now
                            ki.getMainLog().error("Error retrieving utxos for wallet", e);
                        }

                        if (utxos != null) {
                            for (Output o : utxos) {
                                if (tokenValueMap.get(o.getToken()) == null) {
                                    tokenValueMap.put(o.getToken(), o.getAmount());
                                } else {
                                    tokenValueMap.put(o.getToken(), tokenValueMap.get(o.getToken()).add(o.getAmount()));
                                }
                            }
                        }
                        isFinal = true;

                        try {
                            sleep(1200);
                            //System.out.println("done sleeping");
                        } catch (InterruptedException e) {
                            //do nothing as close was called
                        }

                    }
                }
            };
            t.setName("GUI-Backend");
            threads.add(t);
            t.start();
        }
    }

    private List<Thread> threads = new ArrayList<>();

    public void close() {
        for (Thread t : threads) {
            t.interrupt();
        }
    }

    //region "JavaFX HorseShit 2:Electric FuckYou"
    @FXML
    public JFXHamburger menuHamburger;
    @FXML
    public JFXDrawer menuDrawer;
    @FXML
    public Pane topPane;
    @FXML
    public Pane walletPane;
    public volatile static Stage stage;
    @FXML
    public BorderPane borderPane;
    public JFXColorPicker colorPicker;
    public JFXComboBox<Label> colorCombos;
    public Pane settingsPane;
    public JFXComboBox<Label> tokenBox;
    public Label walletAmount;
    public Label tokenLabel;
    public JFXTextField addressText;
    public JFXTextField amountText;
    public JFXTextField feeText;
    public JFXTextField messageText;
    public JFXButton sendButton;
    public JFXTreeTableView<StoredTrans> transactionTable;
    public Label addressLabel;
    public Label heightLabel;
    public JFXButton copyAddress;
    public Pane miningTab;
    public JFXSlider miningIntesity;
    public Label currentHashrate;
    public Label averageHashrate;
    public Label minHashrate;
    public Label maxHashrate;
    public LineChart<String, Number> hashrateChart;
    public JFXButton startMining;
    public Pane helpPane;
    public Label versionLabel;
    public Label miLabel;
    public JFXToggleButton debugMode;
    public StackPane topPane2;
    public Pane addressPane;
    public Pane blockExplorerPane;
    public ScrollPane beScroll;
    public Label entropyLabel;
    public JFXButton setSpendAddress;
    public JFXButton deleteAddress;
    public JFXButton createAddress;
    public JFXListView<String> addressList;
    public JFXToggleButton highSecurity;
    public JFXProgressBar syncProgress;
    public JFXToggleButton requirePassword;
    public JFXPasswordField passwordField;
    public JFXButton submitPassword;
    public Pane lockPane;
    public JFXSpinner passwordWaiter;
    public JFXPasswordField cpCurrent;
    public JFXPasswordField cpNew;
    public JFXPasswordField cpConfirm;
    public JFXButton changePassword;
    //endregion
    private List<Timeline> btnAnimations = new ArrayList<>();
    private List<Timeline> btnAnimationsR = new ArrayList<>();
    private Label ch2dec = new Label(" Chain Height");
    private Label chainHeight2 = new Label(" 26554");
    private Label latency = new Label(" Latency - 125ms");
    private XodusStringMap pmap = new XodusStringMap("security");
    //private List<Order> activeOrders = new ArrayList<>();
    private boolean frg = true;
    static Application app;
    private int priceControlsIndex;

    /**
     * see superHash this thing is part of the same dumb system
     *
     * @param data data you want to corrupt beyond all recognition
     * @return horribly corrupted data
     * @throws UnsupportedEncodingException because I'm lazy
     */
    private String metaHash(String data) throws UnsupportedEncodingException {
        StringBuilder totalhash = new StringBuilder();
        String hash = "";
        for (int i = 0; i < 1024; i++) {
            hash = EncryptionManager.sha512(data + hash);
            totalhash.append(hash);
        }
        String th = totalhash.toString();
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(th.getBytes("UTF-8")[14]);
        bb.put(th.getBytes("UTF-8")[th.getBytes("UTF-8").length - 1]);
        short pointer = bb.getShort(0);
        Random sr = new Random();
        sr.setSeed(pointer / 2);
        for (int i = 0; i < th.getBytes("UTF-8")[pointer]; i++) {
            hash = EncryptionManager.sha512(hash + sr.nextInt() + sr.nextInt());
            totalhash.append(hash);
        }
        return hash;
    }

    private Timeline lockAnimation;

    /**
     * This is possibly the most fucked up hash setup ever. it does a bunch of bullshit with
     * SHA3-512 hashing and combinations of hashes and other shit, it's dumb and I should change it
     *
     * @param hash           uh, a hash or something
     * @param numberOfHashes number of iterations to do the retard function
     * @return even more horribly corrupted data than metaHash
     */
    private String superHash(String hash, int numberOfHashes) {
        StringBuilder superHash = new StringBuilder();
        ConcurrentMap<Integer, Boolean> doneMap = new ConcurrentHashMap<>();
        ConcurrentMap<Integer, Thread> tmap = new ConcurrentHashMap<>();
        ConcurrentMap<Integer, String> hmap = new ConcurrentHashMap<>();
        for (int i = 0; i < numberOfHashes; i++) {
            int i2 = i;
            new Thread() {
                private String hash2;

                public void run() {
                    Random rand = new Random();

                    tmap.put(i2, this);
                    doneMap.put(i2, false);
                    try {
                        rand.setSeed(i2 + hash.getBytes("UTF-8")[hash.getBytes("UTF-8").length - 1]);
                        hash2 = metaHash(hash + rand.nextLong());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    hmap.put(i2, hash2);
                    doneMap.put(i2, true);
                }
            }.start();
        }

        boolean allDone = false;
        while (!allDone) {
            allDone = true;
            for (Map.Entry<Integer, Boolean> v : doneMap.entrySet()) {

                if (!v.getValue()) {
                    allDone = false;
                    //break;
                }
            }
        }
        for (int i = 0; i < tmap.keySet().size(); i++) {
            superHash.append(hmap.get(i));
        }
        return superHash.toString();
    }

    public synchronized void addTransaction(ITrans trans, BigInteger height) {
        if (ki.getOptions().pool) return;
        try {
            BigInteger allout = BigInteger.ZERO;
            List<String> involved = new ArrayList<>();

            for (Output o : trans.getOutputs()) {
                List<String> checked = new ArrayList<>();
                for (IAddress a : ki.getAddMan().getAll()) {
                    if (o.getAddress().encodeForChain().equals(a.encodeForChain())) {
                        if (!involved.contains(a.encodeForChain())) {
                            involved.add(a.encodeForChain());
                        }
                        if (!checked.contains(a.encodeForChain())) {
                            checked.add(a.encodeForChain());
                            allout = allout.add(o.getAmount());
                        }

                    }
                }
            }

            BigInteger allin = BigInteger.ZERO;
            for (Input o : trans.getInputs()) {
                List<String> checked = new ArrayList<>();
                for (IAddress a : ki.getAddMan().getAll()) {
                    if (o.getAddress().encodeForChain().equals(a.encodeForChain())) {
                        if (!involved.contains(a.encodeForChain())) {
                            involved.add(a.encodeForChain());
                        }
                        if (!checked.contains(a.encodeForChain())) {
                            checked.add(a.encodeForChain());
                            allin = allin.add(o.getAmount());
                        }
                    }
                }
            }
            double amount = 0;
            try {
                amount = Math.abs(allout.subtract(allin).longValueExact() / 100_000_000D);
            } catch (Exception e) {
                //TODO big int out of range....why
                ki.getMainLog().warn("Can not add transaction because amount is over Long.MAX_VALUE");
            }
            String direction;
            if (allout.compareTo(allin) > 0) {
                direction = "Received";
            } else {
                direction = "Sent";
                //amount += trans.getFee().longValueExact() / 100_000_000D;
            }

            String timestamp = sdf2.format(new Date(trans.getOutputs().get(0).getTimestamp()));
            List<String> outputs = new ArrayList<>();

            for (Output o : trans.getOutputs()) {
                outputs.add(o.getAddress().encodeForChain() + ":" + format2.format(o.getAmount().longValueExact() / 100_000_000D) + " " + o.getToken().getName());
            }
            ObservableList<String> obsOutputs = FXCollections.observableArrayList(outputs);
            List<String> inputs = new ArrayList<>();

            for (Input i : trans.getInputs()) {
                inputs.add(i.getAddress().encodeForChain() + ":" + format2.format(i.getAmount().longValueExact() / 100_000_000D) + " " + i.getToken().getName());
            }
            ObservableList<String> obsInputs = FXCollections.observableArrayList(inputs);
            for (String add : involved) {

                StoredTrans st = new StoredTrans(add, amount, direction, trans.getMessage(), trans.getInputs().get(0).getAddress().encodeForChain(), timestamp, height.toString(), obsOutputs, obsInputs, trans.getFee().longValueExact() / 100_000_000D);
                transactions.add(st);
                if (transactionTable != null && transactionTable.getRoot() != null) {
                    transactionTable.getRoot().getChildren().add(new TreeItem<>(st));
                    String sText = searchBox.getText();
                    searchBox.setText("refresh");
                    searchBox.setText(sText);
                }
                //final TreeItem<StoredTrans> root = new RecursiveTreeItem<StoredTrans>(transactions, RecursiveTreeObject::getChildren);
                //if(transactionTable != null)
                //transactionTable.setRoot(root);
            }
            if (!sTrans.contains(trans)) {
                sTrans.add(trans);
                HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
                for (ITrans t : sTrans) {
                    hpa.addElement(t.serializeToAmplet());
                }
                guiXAM.putBytes("transactions", hpa.serializeToBytes());
                heightMap.put(trans.getID(), height);
                HeadlessPrefixedAmplet hpa2 = HeadlessPrefixedAmplet.create();

                for (Map.Entry<String, BigInteger> key : heightMap.entrySet()) {

                    hpa2.addElement(key.getKey());
                    hpa2.addElement(key.getValue());
                }

                guiXAM.putBytes("heightMap", hpa2.serializeToBytes());
            }
        } catch (Exception e) {
            ki.getMainLog().warn("Adding a transaction to the table failed. This is not a critical error.", e);
        }


    }

    private long maxH = 0;
    private long minH = Long.MAX_VALUE;
    private DecimalFormat format2 = new DecimalFormat("###,###,###,###,###,###,###,###,##0.#########");
    private boolean mining = false;
    private BigInteger loadHeight;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private ObservableList<StoredTrans> transactions;
    private volatile boolean limit = true;

    private void expandTreeView(TreeItem<?> item) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(true);
            for (TreeItem<?> child : item.getChildren()) {
                expandTreeView(child);
            }
        }
    }

    @FXML
    void initialize() {
        if (!ki.getOptions().pool)
            loadHeight = ki.getChainMan().currentHeight();
        highSecurity.setSelected(ki.getSetting(Settings.HIGH_SECURITY));
        requirePassword.setSelected(ki.getSetting(Settings.REQUIRE_PASSWORD));
        debugMode.setSelected(ki.getSetting(Settings.DEBUG_MODE));
        if (requirePassword.isSelected()) {
            lockPane.setVisible(true);
        }
        highSecurity.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ki.setSetting(Settings.HIGH_SECURITY, highSecurity.isSelected());
            }
        });
        requirePassword.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ki.setSetting(Settings.REQUIRE_PASSWORD, requirePassword.isSelected());
            }
        });
        debugMode.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ki.setSetting(Settings.DEBUG_MODE, debugMode.isSelected());
            }
        });

        blocksFoundLabel.setText("Blocks Found - " + format2.format(blocksFoundInt));
        tokenBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Label>() {
            @Override
            public void changed(ObservableValue<? extends Label> observable, Label oldValue, Label newValue) {
                tokenLabel.setText(tokenBox.getSelectionModel().getSelectedItem().getText());
                walletAmount.setText(format2.format((double) tokenValueMap.get(Token.byName(tokenBox.getSelectionModel().getSelectedItem().getText())).longValueExact() / 100_000_000));
            }
        });
        copyAddress.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                StringSelection stringSelection = new StringSelection(ki.getAddMan().getMainAdd().encodeForChain());
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                clpbrd.setContents(stringSelection, null);
            }
        });
        copySelectedAdd.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                StringSelection stringSelection = new StringSelection(addressList.getSelectionModel().getSelectedItem());
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                clpbrd.setContents(stringSelection, null);
            }
        });
        copyPublicKey.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                StringSelection stringSelection = new StringSelection(ki.getEncryptMan().getPublicKeyString(KeyType.valueOf(keyType.getSelectionModel().getSelectedItem().getText())));
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                clpbrd.setContents(stringSelection, null);
            }
        });
        startMining.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (ki.getOptions().mining) {
                    if (ki.getNetMan().getConnections().size() == 0) return;
                    if (ki.getOptions().pool && ki.getPoolData().currentWork == null) return;
                    if (!mining) {
                        ki.getMinerMan().startMiners();
                        startMining.setText("Stop Mining");
                        mining = true;
                        hashrateChart.getData().clear();
                        //List<XYChart.Series<String, Number>> devs = new ArrayList<>();
                        for (String dev : ki.getMinerMan().getDevNames()) {
                            XYChart.Series<String, Number> series = new XYChart.Series<>();
                            hashrateChart.getData().add(series);
                            series.setName(dev);
                        }
                    } else {
                        ki.getMinerMan().stopMiners();
                        startMining.setText("Start Mining");
                        mining = false;
                    }
                }
            }
        });

        obSellSize.setSkin(new RefreshableListViewSkin(obSellSize));
        obSellPrice.setSkin(new RefreshableListViewSkin(obSellPrice));
        obSellTotal.setSkin(new RefreshableListViewSkin(obSellTotal));
        obBuyPrice.setSkin(new RefreshableListViewSkin(obBuyPrice));
        obBuySize.setSkin(new RefreshableListViewSkin(obBuySize));
        obBuyTotal.setSkin(new RefreshableListViewSkin(obBuyTotal));
        obRecentPrice.setSkin(new RefreshableListViewSkin(obRecentPrice));
        obRecentAmount.setSkin(new RefreshableListViewSkin(obRecentAmount));
        obRecentDirection.setSkin(new RefreshableListViewSkin(obRecentDirection));
        activePrice.setSkin(new RefreshableListViewSkin(activePrice));
        activeAmount.setSkin(new RefreshableListViewSkin(activeAmount));
        unitSelectorPrice.getItems().add(new Label("BTC"));
        unitSelectorPrice.getItems().add(new Label("mBTC"));
        unitSelectorPrice.getItems().add(new Label("µBTC"));
        unitSelectorPrice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Label>() {
            @Override
            public void changed(ObservableValue<? extends Label> observable, Label oldValue, Label newValue) {
                if (newValue.getText().equals("µBTC")) {
                    unitMultiplierPrice = BigInteger.valueOf(100);
                } else if (newValue.getText().equals("mBTC")) {
                    unitMultiplierPrice = BigInteger.valueOf(100_000);
                } else if (newValue.getText().equals("BTC")) {
                    unitMultiplierPrice = BigInteger.valueOf(100_000_000);
                }
                tokenPrice.setText(newValue.getText());
                ((RefreshableListViewSkin) obSellSize.getSkin()).refresh();
                ((RefreshableListViewSkin) obSellPrice.getSkin()).refresh();
                ((RefreshableListViewSkin) obSellTotal.getSkin()).refresh();
                ((RefreshableListViewSkin) obBuySize.getSkin()).refresh();
                ((RefreshableListViewSkin) obBuyPrice.getSkin()).refresh();
                ((RefreshableListViewSkin) obBuyTotal.getSkin()).refresh();
                ((RefreshableListViewSkin) obRecentAmount.getSkin()).refresh();
                ((RefreshableListViewSkin) obRecentDirection.getSkin()).refresh();
                ((RefreshableListViewSkin) obRecentPrice.getSkin()).refresh();
                ((RefreshableListViewSkin) activeAmount.getSkin()).refresh();
                ((RefreshableListViewSkin) activePrice.getSkin()).refresh();

            }
        });
        unitSelectorPrice.getSelectionModel().select(0);
        unitSelectorAmount.getItems().add(new Label("ORA"));
        unitSelectorAmount.getItems().add(new Label("mORA"));
        unitSelectorAmount.getItems().add(new Label("µORA"));
        unitSelectorAmount.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Label>() {
            @Override
            public void changed(ObservableValue<? extends Label> observable, Label oldValue, Label newValue) {
                if (newValue.getText().equals("µORA")) {
                    unitMultiplierAmount = BigInteger.valueOf(100);
                } else if (newValue.getText().equals("mORA")) {
                    unitMultiplierAmount = BigInteger.valueOf(100_000);
                } else if (newValue.getText().equals("ORA")) {
                    unitMultiplierAmount = BigInteger.valueOf(100_000_000);
                }
                tokenAmount.setText(newValue.getText());
                ((RefreshableListViewSkin) obSellSize.getSkin()).refresh();
                ((RefreshableListViewSkin) obSellPrice.getSkin()).refresh();
                ((RefreshableListViewSkin) obSellTotal.getSkin()).refresh();
                ((RefreshableListViewSkin) obBuySize.getSkin()).refresh();
                ((RefreshableListViewSkin) obBuyPrice.getSkin()).refresh();
                ((RefreshableListViewSkin) obBuyTotal.getSkin()).refresh();
                ((RefreshableListViewSkin) obRecentAmount.getSkin()).refresh();
                ((RefreshableListViewSkin) obRecentDirection.getSkin()).refresh();
                ((RefreshableListViewSkin) obRecentPrice.getSkin()).refresh();
                ((RefreshableListViewSkin) activeAmount.getSkin()).refresh();
                ((RefreshableListViewSkin) activePrice.getSkin()).refresh();

            }
        });
        unitSelectorAmount.getSelectionModel().select(0);
        NumberAxis priceAxis = new NumberAxis();
        CategoryAxis timeAxis = new CategoryAxis();
        exchangeGraph = new CandlestickGraph(timeAxis, priceAxis, ki);
        exchangeGraphBox.getChildren().add(exchangeGraph);
        exchangeGraph.setLegendVisible(false);
        changePassword.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (cpNew.getText().equals(cpConfirm.getText())) {
                    if (cpNew.getText().isEmpty()) {
                        notification("Password cannot be empty");
                        return;
                    }
                    if (verifyPassword(cpCurrent.getText())) {
                        deleteOldPassword(cpCurrent.getText());

                        createNewPassword(cpNew.getText());
                        notification("Password Changed");
                    } else {
                        notification("Wrong password");
                    }
                } else {
                    notification("Passwords don't match");
                }
            }
        });
        pairs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Label>() {
            @Override
            public void changed(ObservableValue<? extends Label> observable, Label oldValue, Label newValue) {
                Pairs pair = Pairs.byName(newValue.getText());
                if (pair != null) {
                    exchangeBuy.setText("Buy (" + pair.accepting().getName() + ")");
                    exchangeSell.setText("Sell (" + pair.accepting().getName() + ")");
                }
            }
        });
        priceControlsIndex = exControlsBox.getChildren().indexOf(priceHBox);
        limitBuy.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                exPrice.setText("");
                limit = true;
                if (!exControlsBox.getChildren().contains(priceHBox)) {
                    exControlsBox.getChildren().add(priceControlsIndex, priceHBox);
                }
            }
        });
        marketBuy.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                exPrice.setText("");
                limit = false;
                if (exControlsBox.getChildren().contains(priceHBox)) {
                    exControlsBox.getChildren().remove(priceControlsIndex);
                }
            }
        });
        for (Pairs pair : Pairs.values()) {
            Label label = new Label(pair.getName());
            pairs.getItems().add(label);
        }
        pairs.getSelectionModel().select(0);
        exchangeBuy.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                BigInteger amount;
                try {
                    amount = new BigDecimal(amtOnOffer.getText()).multiply(BigDecimal.valueOf(unitMultiplierAmount.doubleValue())).toBigInteger();
                } catch (Exception e) {
                    notification("Invalid Amount");
                    return;
                }
                BigInteger stopPrice = null;
                if (!ki.getExMan().getOrderBook().isSorted()) {
                    ki.getExMan().getOrderBook().sort();
                }
                if (!ki.getExMan().getOrderBook().sells().isEmpty())
                    stopPrice = ki.getExMan().getOrderBook().sells().get(0).unitPrice();
                else if (!limit) {
                    notification("No order to market buy from");
                    return;
                }
                if (limit) {
                    try {
                        stopPrice = new BigDecimal(exPrice.getText()).multiply(BigDecimal.valueOf(unitMultiplierPrice.doubleValue())).toBigInteger();
                        if (stopPrice.compareTo(BigInteger.valueOf(500)) < 0) throw new Exception();
                    } catch (Exception e) {
                        notification("Invalid Price");
                        return;
                    }
                }

                OrderStatus status = ki.getExMan().placeOrder(true, amount, stopPrice, Pairs.byName(pairs.getSelectionModel().getSelectedItem().getText()), limit);
                if (!status.succeeded()) {
                    if (!status.partial()) notification("Order not completed");
                    else notification("Order partially completed");
                } else {
                    notification("Order complete!");
                }
            }
        });
        exchangeSell.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                BigInteger amount;
                try {
                    amount = new BigDecimal(amtOnOffer.getText()).multiply(BigDecimal.valueOf(unitMultiplierAmount.doubleValue())).toBigInteger();
                } catch (Exception e) {
                    notification("Invalid Amount");
                    return;
                }
                BigInteger stopPrice = null;
                if (!ki.getExMan().getOrderBook().isSorted()) {
                    ki.getExMan().getOrderBook().sort();
                }
                if (!ki.getExMan().getOrderBook().buys().isEmpty())
                    stopPrice = ki.getExMan().getOrderBook().buys().get(0).unitPrice();
                else if (!limit) {
                    notification("No order to market sell to");
                    return;
                }
                if (limit) {
                    try {
                        stopPrice = new BigDecimal(exPrice.getText()).multiply(BigDecimal.valueOf(unitMultiplierPrice.doubleValue())).toBigInteger();
                        if (stopPrice.compareTo(BigInteger.valueOf(500)) < 0) throw new Exception();
                    } catch (Exception e) {
                        notification("Invalid Price");
                        return;
                    }
                }
                OrderStatus status = ki.getExMan().placeOrder(false, amount, stopPrice, Pairs.byName(pairs.getSelectionModel().getSelectedItem().getText()), limit);
                if (!status.succeeded()) {
                    if (!status.partial()) notification("Order not completed");
                    else notification("Order partially completed");
                } else {
                    notification("Order complete!");
                }
            }
        });


        amtOnOffer.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!limit) {
                    BigInteger amount;
                    try {
                        amount = new BigDecimal(amtOnOffer.getText()).multiply(BigDecimal.valueOf(unitMultiplierAmount.doubleValue())).toBigInteger();
                    } catch (Exception e) {
                        //notification("Invalid Amount");
                        return;
                    }
                    BigInteger priceSell = ki.getExMan().getOrderBook().buys().get(0).unitPrice();
                    BigInteger priceBuy = ki.getExMan().getOrderBook().sells().get(0).unitPrice();
                    double totalBuy = (priceBuy.doubleValue() / 100_000_000D) * amount.doubleValue();
                    double totalSell = (priceSell.doubleValue() / 100_000_000D) * amount.doubleValue();
                    exchangeTotalPurchase.setText("Totals -" + "\n" + "Buy: " + format2.format(totalBuy / unitMultiplierPrice.doubleValue()) + unitSelectorPrice.getSelectionModel().getSelectedItem().getText() + "\n" + "Sell: " + format2.format(totalSell / unitMultiplierPrice.doubleValue()) + unitSelectorPrice.getSelectionModel().getSelectedItem().getText());
                } else {
                    BigInteger amount;
                    BigInteger stopPrice = null;
                    try {
                        amount = new BigDecimal(amtOnOffer.getText()).multiply(BigDecimal.valueOf(unitMultiplierAmount.doubleValue())).toBigInteger();
                    } catch (Exception e) {
                        //notification("Invalid Amount");
                        return;
                    }
                    try {
                        stopPrice = new BigDecimal(exPrice.getText()).multiply(BigDecimal.valueOf(unitMultiplierPrice.doubleValue())).toBigInteger();
                        if (stopPrice.compareTo(BigInteger.valueOf(500)) < 0) throw new Exception();
                    } catch (Exception e) {
                        //notification("Invalid Price");
                        return;
                    }
                    double total = (stopPrice.doubleValue() / 100_000_000) * amount.doubleValue();
                    exchangeTotalPurchase.setText("Total - " + format2.format(total / unitMultiplierPrice.doubleValue()) + unitSelectorPrice.getSelectionModel().getSelectedItem().getText());
                }
            }
        });

        exPrice.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                BigInteger amount;
                BigInteger stopPrice = null;
                try {
                    amount = new BigDecimal(amtOnOffer.getText()).multiply(BigDecimal.valueOf(unitMultiplierAmount.doubleValue())).toBigInteger();
                } catch (Exception e) {
                    //notification("Invalid Amount");
                    return;
                }
                try {
                    stopPrice = new BigDecimal(exPrice.getText()).multiply(BigDecimal.valueOf(unitMultiplierPrice.doubleValue())).toBigInteger();
                    if (stopPrice.compareTo(BigInteger.valueOf(500)) < 0) throw new Exception();
                } catch (Exception e) {
                    //notification("Invalid Price");
                    return;
                }
                double total = (stopPrice.doubleValue() / 100_000_000) * amount.doubleValue();
                exchangeTotalPurchase.setText("Total - " + format2.format(total / unitMultiplierPrice.doubleValue()) + unitSelectorPrice.getSelectionModel().getSelectedItem().getText());
            }
        });
        faqLink.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                app.getHostServices().showDocument("https://bitbucket.org/backspace119/ki-project-origin/wiki/FAQ");
            }
        });
        issuesPageLink.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                app.getHostServices().showDocument("https://bitbucket.org/backspace119/ki-project-origin/issues?status=new&status=open");
            }
        });
        discordServerLink.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                app.getHostServices().showDocument("https://discord.gg/6PjeMzd");
            }
        });
        JFXTreeTableColumn<StoredTrans, String> oaColumn = new JFXTreeTableColumn<>("Sender");
        oaColumn.setPrefWidth(150);
        oaColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<StoredTrans, String> param) -> {
            if (oaColumn.validateValue(param)) return param.getValue().getValue().otherAddress;
            else return oaColumn.getComputedValue(param);
        });

        JFXTreeTableColumn<StoredTrans, Double> amtColumn = new JFXTreeTableColumn<>("Amount");
        amtColumn.setPrefWidth(150);

        amtColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<StoredTrans, Double> param) -> {
            if (amtColumn.validateValue(param)) {
                return param.getValue().getValue().amount.asObject();
            } else return amtColumn.getComputedValue(param);
        });
        JFXTreeTableColumn<StoredTrans, String> sentColumn = new JFXTreeTableColumn<>("Direction");
        sentColumn.setPrefWidth(100);
        sentColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<StoredTrans, String> param) -> {
            if (sentColumn.validateValue(param)) return param.getValue().getValue().sent;
            else return sentColumn.getComputedValue(param);
        });
        JFXTreeTableColumn<StoredTrans, String> msgColumn = new JFXTreeTableColumn<>("Message");
        msgColumn.setPrefWidth(150);
        msgColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<StoredTrans, String> param) -> {
            if (msgColumn.validateValue(param)) return param.getValue().getValue().message;
            else return msgColumn.getComputedValue(param);
        });

        JFXTreeTableColumn<StoredTrans, String> tsColumn = new JFXTreeTableColumn<>("Timestamp");
        tsColumn.setPrefWidth(150);
        tsColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<StoredTrans, String> param) -> {
            if (tsColumn.validateValue(param)) return param.getValue().getValue().timestamp;
            else return tsColumn.getComputedValue(param);
        });

        JFXTreeTableColumn<StoredTrans, String> addColumn = new JFXTreeTableColumn<>("Address");
        addColumn.setPrefWidth(150);
        addColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<StoredTrans, String> param) -> {
            if (addColumn.validateValue(param)) return param.getValue().getValue().address;
            else return addColumn.getComputedValue(param);
        });

        JFXTreeTableColumn<StoredTrans, String> hColumn = new JFXTreeTableColumn<>("Height");
        hColumn.setPrefWidth(150);
        hColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<StoredTrans, String> param) -> {
            if (hColumn.validateValue(param)) return param.getValue().getValue().height;
            else return hColumn.getComputedValue(param);
        });
        JFXTreeTableColumn<StoredTrans, ObservableList<String>> outColumn = new JFXTreeTableColumn<>("Outputs");
        outColumn.setPrefWidth(400);
        outColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<StoredTrans, ObservableList<String>> param) -> {
            if (outColumn.validateValue(param)) return param.getValue().getValue().outputs;
            else return outColumn.getComputedValue(param);
        });
        JFXTreeTableColumn<StoredTrans, ObservableList<String>> inColumn = new JFXTreeTableColumn<>("Inputs");
        inColumn.setPrefWidth(400);
        inColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<StoredTrans, ObservableList<String>> param) -> {
            if (inColumn.validateValue(param)) return param.getValue().getValue().inputs;
            else return inColumn.getComputedValue(param);
        });

        JFXTreeTableColumn<StoredTrans, Double> feeColumn = new JFXTreeTableColumn<>("Fee");
        feeColumn.setPrefWidth(150);
        feeColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<StoredTrans, Double> param) -> {
            if (feeColumn.validateValue(param)) return param.getValue().getValue().fee.asObject();
            else return feeColumn.getComputedValue(param);
        });
        final TreeItem<StoredTrans> root = new RecursiveTreeItem<StoredTrans>(transactions, RecursiveTreeObject::getChildren);

        transactionTable.setRoot(root);
        transactionTable.setShowRoot(false);
        transactionTable.setEditable(false);
        transactionTable.getColumns().setAll(addColumn, oaColumn, amtColumn, sentColumn, msgColumn, outColumn, inColumn, feeColumn, tsColumn);
        transactionTable.group(addColumn);


        searchBox.textProperty().addListener((o, oldVal, newVal) -> {
            transactionTable.setPredicate(transFilter -> transFilter.getValue().address.get().contains(newVal)
                    || transFilter.getValue().timestamp.get().contains(newVal)
                    || transFilter.getValue().sent.get().contains(newVal)
                    || transFilter.getValue().amount.toString().contains(newVal)
                    || transFilter.getValue().otherAddress.get().contains(newVal)
                    || transFilter.getValue().message.get().contains(newVal));

        });
        transactionTable.currentItemsCountProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                expandTreeView(transactionTable.getRoot());
            }
        });
        amountOfTransactions.textProperty().bind(Bindings.createStringBinding(() -> transactionTable.getCurrentItemsCount() + "",
                transactionTable.currentItemsCountProperty()));
        miningIntesity.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                GPUMiner.miningIntensity = miningIntesity.getValue();
            }
        });
        backToBE.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                blockPane.setVisible(false);
                blockExplorerPane.setVisible(true);
            }
        });
        previousBlock.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (currentBlock.subtract(BigInteger.ONE).compareTo(BigInteger.ZERO) > 0)
                    setupBlockPane(currentBlock.subtract(BigInteger.ONE));
            }
        });
        nextBlock.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (currentBlock.add(BigInteger.ONE).compareTo(ki.getChainMan().currentHeight()) <= 0)
                    setupBlockPane(currentBlock.add(BigInteger.ONE));
            }
        });
        try {
            if (guiXAM.getBytes("transactions") != null) {
                HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(guiXAM.getBytes("transactions"));

                new Thread() {

                    public void run() {


                        while (hpa.hasNextElement()) {
                            try {
                                ITrans t = Transaction.fromAmplet(Amplet.create(hpa.getNextElement()));
                                if (t == null) continue;
                                sTrans.add(t);
                                addTransaction(t, (heightMap.get(t.getID()) == null) ? ki.getChainMan().currentHeight() : heightMap.get(t.getID()));
                            } catch (Exception e) {
                                ki.getMainLog().error("Error loading saved transactions for GUI: ", e);
                            }
                        }
                    }
                }.start();

            }
        } catch (Exception e) {
            ki.getMainLog().error("Error loading saved transactions for GUI: ", e);
        }
        addressControls.getChildren().remove(multiSigVbox);
        singleSig.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (createMultiSig) {
                    createMultiSig = false;
                    addressControls.getChildren().remove(multiSigVbox);
                }
            }
        });
        multiSig.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (!createMultiSig) {
                    createMultiSig = true;
                    addressControls.getChildren().add(multiSigVbox);
                }
            }
        });
        miningDataHbox.setSpacing(10);
        addressLabel.setText("Address - " + ki.getAddMan().getMainAdd().encodeForChain());
        syncProgress.setProgress(0);
        submitPassword.setBackground(new Background(new BackgroundFill(Color.valueOf(ki.getStringSetting(StringSettings.PRIMARY_COLOR)), CornerRadii.EMPTY, Insets.EMPTY)));
        passwordField.setLabelFloat(true);

        confirmPassword.setLabelFloat(true);
        if (pmap.get("fr") != null) {
            passwordVbox.getChildren().remove(confirmPassword);
            confirmPassword.setDisable(true);
        }
        RequiredFieldValidator validator = new RequiredFieldValidator();
        //validator.setMessage("Input Required");
        passwordField.getValidators().add(validator);
        confirmPassword.getValidators().add(validator);
        passwordField.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) passwordField.validate();
        });
        confirmPassword.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) confirmPassword.validate();
        });
        tokenLabel.setMinWidth(walletAmount.getWidth());
        //tokenLabel.setPrefWidth(walletAmount.getWidth());
        //tokenLabel.setMaxWidth(walletAmount.getWidth());
        lockAnimation = new Timeline(new KeyFrame(Duration.millis(500), new KeyValue(lockPane.layoutYProperty(), 500, EASE_BOTH)), new KeyFrame(Duration.millis(500), new KeyValue(lockPane.opacityProperty(), 0, EASE_BOTH)));
        submitPassword.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handlePassword();
            }
        });
        versionLabel.setText("Version - " + Ki.VERSION);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.out.println("Close requested");
                close = true;
            }
        });
        deleteAddress.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (ki.getAddMan().getMainAdd().encodeForChain().equals(addressList.getSelectionModel().getSelectedItem())) {
                    notification("Cannot delete main address");
                    return;
                }
                ki.getAddMan().deleteAddress(Address.decodeFromChain(addressList.getSelectionModel().getSelectedItem()));
                addressList.getItems().remove(addressList.getSelectionModel().getSelectedIndex());
            }
        });
        if (ki.getStringSetting(StringSettings.POOL_PAYTO) != null && !ki.getStringSetting(StringSettings.POOL_PAYTO).isEmpty())
            paytoAddress.setText(ki.getStringSetting(StringSettings.POOL_PAYTO));
        if (ki.getStringSetting(StringSettings.POOL_SERVER) != null && !ki.getStringSetting(StringSettings.POOL_SERVER).isEmpty())
            ipField.setText(ki.getStringSetting(StringSettings.POOL_SERVER));
        addressList.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        List<Pane> content = new ArrayList<>();
        content.add(settingsPane);
        content.add(walletPane);
        content.add(miningTab);
        content.add(helpPane);
        content.add(blockExplorerPane);
        content.add(addressPane);
        content.add(blockPane);
        content.add(poolPane);
        content.add(poolRelay);
        content.add(exchangePane);
        content.add(ohPane);
        Label pc = new Label("Primary Color");
        Label sc = new Label("Secondary Color");
        Label pt = new Label("Primary Text Color");
        Label st = new Label("Secondary Text Color");
        pc.setStyle("-fx-text-fill:BLACK");
        sc.setStyle("-fx-text-fill:BLACK");
        pt.setStyle("-fx-text-fill:BLACK");
        st.setStyle("-fx-text-fill:BLACK");
        colorCombos.getItems().add(pc);
        colorCombos.getItems().add(sc);
        colorCombos.getItems().add(pt);
        colorCombos.getItems().add(st);
        colorCombos.setEditable(false);
        colorCombos.getSelectionModel().select(0);
        menuHamburger.setScaleX(0);
        menuHamburger.setScaleY(0);
        Timeline animation = new Timeline(new KeyFrame(Duration.millis(400),
                new KeyValue(menuHamburger.scaleXProperty(),
                        1,
                        EASE_BOTH),
                new KeyValue(menuHamburger.scaleYProperty(),
                        1,
                        EASE_BOTH)));
        animation.setDelay(Duration.millis(500));
        animation.play();
        transactionTable.setStyle("-fx-background-color:DIMGRAY");
        debugMode.setTextFill(Color.WHITE);
        requirePassword.setTextFill(Color.WHITE);
        highSecurity.setTextFill(Color.WHITE);
        poolDynamicFee.setTextFill(Color.WHITE);
        autoMine.setTextFill(Color.WHITE);
        autoMine.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ki.setSetting(Settings.AUTO_MINE, autoMine.isSelected());
            }
        });
        pplnsClient.setSelected(ki.getSetting(Settings.PPLNS_CLIENT));
        pplnsServer.setSelected(ki.getSetting(Settings.PPLNS_SERVER));
        pplnsClient.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ki.setSetting(Settings.PPLNS_CLIENT, pplnsClient.isSelected());
            }
        });
        pplnsServer.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ki.setSetting(Settings.PPLNS_SERVER, pplnsServer.isSelected());
            }
        });
        poolDynamicFee.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (poolDynamicFee.isSelected()) {
                    poolDynamicFeeSlider.setDisable(false);
                    poolStaticFee.setDisable(true);
                    BigDecimal sd = new BigDecimal(GPUMiner.shareDiff);
                    BigDecimal cd = new BigDecimal(ki.getChainMan().getCurrentDifficulty());
                    long pps = (long) (((cd.divide(sd, 9, RoundingMode.HALF_DOWN).doubleValue() * ChainManager.blockRewardForHeight(ki.getChainMan().currentHeight()).longValueExact()) * (1 - (poolDynamicFeeSlider.getValue() / 100))));
                    ki.getPoolManager().updateCurrentPayPerShare(pps);
                } else {
                    poolDynamicFeeSlider.setDisable(true);
                    poolStaticFee.setDisable(false);
                }
                ki.setSetting(Settings.DYNAMIC_FEE, poolDynamicFee.isSelected());
            }
        });
        poolDynamicFeeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                BigDecimal sd = new BigDecimal(GPUMiner.shareDiff);
                BigDecimal cd = new BigDecimal(ki.getChainMan().getCurrentDifficulty());
                long pps = (long) (((cd.divide(sd, 9, RoundingMode.HALF_DOWN).doubleValue() * ChainManager.blockRewardForHeight(ki.getChainMan().currentHeight()).longValueExact()) * (1 - (poolDynamicFeeSlider.getValue() / 100))));
                ki.getPoolManager().updateCurrentPayPerShare(pps);
                ki.setStringSetting(StringSettings.POOL_FEE, poolDynamicFeeSlider.getValue() + "");
            }
        });
        poolStaticFee.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    double fee = Double.parseDouble(newValue);

                    long pps = (long) (fee * 100_000_000L);
                    ki.setStringSetting(StringSettings.POOL_STATIC_PPS, "" + pps);
                    ki.getPoolManager().updateCurrentPayPerShare(pps);
                } catch (Exception e) {
                    ki.getMainLog().warn("Could not parse static pool fee " + newValue);
                }
            }
        });
        menuDrawer.close();
        borderPane.setStyle("-fx-background-color:" + ki.getStringSetting(StringSettings.SECONDARY_COLOR));
        VBox vb = new VBox();
        vb.setMaxWidth(Double.MAX_VALUE);
        //Image img = new Image(getClass().getResourceAsStream("/origin.png"));
        //vb.setBackground(new Background(new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
        vb.setFillWidth(true);
        List<String> adds = new ArrayList<>();
        for (IAddress add : ki.getAddMan().getAll()) {
            if (!adds.contains(add.encodeForChain())) {
                addressList.getItems().add(add.encodeForChain());
                adds.add(add.encodeForChain());
            }
        }
        payoutSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                ki.getPoolManager().updateCurrentPayInterval(newValue.longValue() * 60_000L);
            }
        });

        orderHistory.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                exchangePane.setVisible(false);
                prepareOH();
                ohPane.setVisible(true);
            }
        });
        backToEx.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ohPane.setVisible(false);
                exchangePane.setVisible(true);
            }
        });
        entropyLabel.setWrapText(true);
        entropyLabel.setMaxWidth(256);

        for (KeyType type : KeyType.values()) {
            if (!type.equals(KeyType.NONE))
                keyType.getItems().add(new Label(type.toString()));
        }
        for (AddressLength l : AddressLength.values()) {
            addressLength.getItems().add(new Label(l.toString()));
        }
        addressLength.getSelectionModel().select(0);
        keyType.getSelectionModel().select(0);
        addressList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                entropyLabel.setText("Entropy of Selection: \n" + ki.getAddMan().getEntropyForAdd(Address.decodeFromChain(addressList.getSelectionModel().getSelectedItem())));
            }
        });
        Map<String, Byte> keys = new HashMap<>();
        addKeyBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    if (EncryptionManager.pubKeyFromString(addPubKey.getText(), KeyType.valueOf(keyType.getSelectionModel().getSelectedItem().getText())) == null) {
                        notification("Invalid Key");
                        return;
                    }

                    keys.put(addPubKey.getText(), KeyType.valueOf(keyType.getSelectionModel().getSelectedItem().getText()).getValue());
                    int selection;
                    if (sigsRequired.getItems().size() > 0)
                        selection = sigsRequired.getSelectionModel().getSelectedIndex();
                    else
                        selection = 0;
                    sigsRequired.getItems().clear();
                    for (int i = 0; i < keys.size(); i++) {
                        sigsRequired.getItems().add(new Label(i + 1 + " of " + keys.size()));
                    }
                    sigsRequired.getSelectionModel().select(selection);
                } catch (Exception e) {
                    notification("Invalid key");
                }
            }
        });
        clearKeys.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                keys.clear();
                sigsRequired.getItems().clear();
            }
        });
        createAddress.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                IAddress a;
                if (entropyField.getText().isEmpty()) {
                    notification("Entropy cannot be empty when creating an address");
                    return;
                }
                if (!createMultiSig) {

                    if (prefixField.getText() == null || prefixField.getText().isEmpty()) {
                        a = ki.getAddMan().createNew(ki.getEncryptMan().getPublicKeyString(KeyType.valueOf(keyType.getSelectionModel().getSelectedItem().getText())), entropyField.getText(), null, AddressLength.valueOf(addressLength.getSelectionModel().getSelectedItem().getText()), false, KeyType.valueOf(keyType.getSelectionModel().getSelectedItem().getText()));
                    } else {
                        if (prefixField.getText().length() != 5) {
                            notification("Prefixes must be exactly 5 characters long");
                            return;
                        }
                        if (prefixField.getText().contains(" ")) {
                            notification("Prefixes cannot contain spaces");
                            return;
                        }
                        a = ki.getAddMan().createNew(ki.getEncryptMan().getPublicKeyString(KeyType.valueOf(keyType.getSelectionModel().getSelectedItem().getText())), entropyField.getText(), prefixField.getText(), AddressLength.valueOf(addressLength.getSelectionModel().getSelectedItem().getText()), false, KeyType.valueOf(keyType.getSelectionModel().getSelectedItem().getText()));
                    }
                    addressList.getItems().add(a.encodeForChain());
                } else {
                    try {
                        if (keys.isEmpty()) {
                            notification("No keys have been added");
                            return;
                        }
                        Binary bin = ki.getScriptMan().buildMultiSig(keys, sigsRequired.getSelectionModel().getSelectedIndex() + 1, ki.getBCE8(), entropyField.getText().getBytes("UTF-8"), ki.getEncryptMan().getPublicKey(KeyType.valueOf(keyType.getSelectionModel().getSelectedItem().getText())).getEncoded());
                        bin.getProgram().seal();
                        if (prefixField.getText() == null || prefixField.getText().isEmpty()) {

                            a = ki.getAddMan().createNew(Utils.toBase64(bin.serializeToAmplet().serializeToBytes()), entropyField.getText(), null, AddressLength.valueOf(addressLength.getSelectionModel().getSelectedItem().getText()), true, KeyType.NONE);
                        } else {
                            if (prefixField.getText().length() != 5) {
                                notification("Prefixes must be exactly 5 characters long");
                                return;
                            }
                            if (prefixField.getText().contains(" ")) {
                                notification("Prefixes cannot contain spaces");
                                return;
                            }
                            a = ki.getAddMan().createNew(Utils.toBase64(bin.serializeToAmplet().serializeToBytes()), entropyField.getText(), prefixField.getText(), AddressLength.valueOf(addressLength.getSelectionModel().getSelectedItem().getText()), true, KeyType.NONE);

                        }
                        ki.getAddMan().associateBinary(a, bin);
                    } catch (UnsupportedEncodingException e) {
                        ki.getMainLog().warn("Unable to create multi-sig address", e);
                        return;
                    }
                    addressList.getItems().add(a.encodeForChain());
                }
            }
        });

        saveMSAddress.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (ki.getAddMan().getMainAdd().isP2SH()) {
                    BinALPreBucket bucket = new BinALPreBucket(ki.getAddMan().getBinary(ki.getAddMan().getMainAdd()), ki.getAddMan().getMainAdd().getAddressLength(), (ki.getAddMan().getMainAdd().hasPrefix()) ? ki.getAddMan().getMainAdd().getPrefix() : null);
                    try (FileOutputStream fos = new FileOutputStream("multisig.address")) {
                        fos.write(bucket.serializeToBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    notification("Set the multi-sig address as your spend address to save");
                }
            }
        });

        loadMSAddress.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    Path path = Paths.get("multisig.address");
                    BinALPreBucket bucket = BinALPreBucket.fromBytes(Files.readAllBytes(path));
                    for (int i = 0; i < 32; i++) {
                        KeyKeyTypePair kktp = KeyKeyTypePair.fromBytes(bucket.getBin().getConstantMemory().getElement(i).getData());
                        if (ki.getEncryptMan().getPublicKeyString(kktp.getKeyType()).equals(Utils.toBase64(kktp.getKey()))) {
                            IAddress a = ki.getAddMan().createNew(Utils.toBase64(bucket.getBin().serializeToAmplet().serializeToBytes()), new String(bucket.getBin().getEntropy(), "UTF-8"), bucket.getPrefix(), bucket.getAl(), true, KeyType.NONE);
                            ki.getAddMan().associateBinary(a, bucket.getBin());
                            addressList.getItems().add(a.encodeForChain());

                            return;
                        }
                    }
                    notification("Your keys don't exist in this address, not adding");
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    notification("Loading multisig from multisig.address file failed");
                    //fail quietly for now
                }
            }
        });

        setSpendAddress.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ki.getAddMan().setMainAdd(Address.decodeFromChain(addressList.getSelectionModel().getSelectedItem()));
                addressLabel.setText("Address - " + ki.getAddMan().getMainAdd().encodeForChain());
            }
        });
        blockExplorerPane.visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (blockExplorerPane.visibleProperty().get()) {
                    fillMasonry(ki.getChainMan().currentHeight().subtract(BigInteger.valueOf(100)), ki.getChainMan().currentHeight());
                }
            }
        });
        if (!ki.getOptions().pool) {
            vb.getChildren().add(buildMainButton("Wallet", "/Wallet.png", 0, 0, content, walletPane));
            vb.getChildren().add(buildMainButton("Address", "/home.png", 100, 0, content, addressPane));
            vb.getChildren().add(buildMainButton("ADX", "/exchange.png", 200, 5, content, exchangePane));
        } else if (ki.getOptions().pool && !ki.getOptions().poolRelay) {
            vb.getChildren().add(buildMainButton("Pool", "/pool.png", 100, 3, content, poolPane));
        }
        if (ki.getOptions().poolRelay) {
            vb.getChildren().add(buildMainButton("Pool", "/pool.png", 100, 3, content, poolRelay));
        }
        //vb.getChildren().add(buildButton("Transactions","/Transactions.png",100));
        if (!ki.getOptions().poolRelay)
            vb.getChildren().add(buildMainButton("Mining", "/gpu.png", 300, 1, content, miningTab));

        if (!ki.getOptions().lite && !ki.getOptions().pool)
            vb.getChildren().add(buildMainButton("Blocks", "/Block.png", 400, 0, content, blockExplorerPane));
        vb.getChildren().add(buildMainButton("Settings", "/Settings.png", 500, 0, content, settingsPane));
        vb.getChildren().add(buildMainButton("Help", "/Help.png", 600, 7, content, helpPane));
        vb.getChildren().add(new Separator());
        if (!ki.getOptions().lite && !ki.getOptions().pool)
            fillMasonry(ki.getChainMan().currentHeight().subtract(BigInteger.valueOf(100)), ki.getChainMan().currentHeight());
        chainHeight2.setFont(Font.loadFont(getClass().getResourceAsStream("/ADAM.CG PRO.otf"), 10));
        ch2dec.setFont(Font.loadFont(getClass().getResourceAsStream("/ADAM.CG PRO.otf"), 10));
        //chainHeight2.setPrefHeight(80);
        ch2dec.setMinWidth(90);
        ch2dec.setTextAlignment(TextAlignment.CENTER);
        if (!ki.getOptions().pool) {
            vb.getChildren().add(ch2dec);

            chainHeight2.setMinWidth(90);
            chainHeight2.setTextAlignment(TextAlignment.CENTER);
            vb.getChildren().add(chainHeight2);
        }
        resetColors.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                colorCombos.getSelectionModel().select(1);
                colorPicker.setValue(Color.valueOf("#252830"));
                colorCombos.getSelectionModel().select(0);
                colorPicker.setValue(Color.valueOf("#18BC9C"));
            }
        });

        latency.setFont(Font.loadFont(getClass().getResourceAsStream("/ADAM.CG PRO.otf"), 10));
        if (!ki.getOptions().pool)
            vb.getChildren().add(latency);
        //vb.setStyle("-fx-background-color:" + ki.getStringSetting(StringSettings.PRIMARY_COLOR));
        //vb.setBackground(new Background(new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
        menuDrawer.getSidePane().add(vb);
        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
            //System.out.println("Height: " + stage.getHeight() + " Width: " + stage.getWidth());
            hashrateChart.setMinWidth(miningTab.getWidth());
            miningIntesity.setMinWidth(miningTab.getWidth() - 20);
            startMining.setLayoutX((miningTab.getWidth() / 2) - (startMining.getWidth() / 2) - 5);
            miLabel.setLayoutX((miningTab.getWidth() / 2) - (miLabel.getWidth() / 2) - 5);
            //addressText.setLayoutX(walletPane.getWidth() - (addressText.getWidth() + 5));
            //amountText.setLayoutX(walletPane.getWidth() - (amountText.getWidth() + 5));
            //messageText.setLayoutX(walletPane.getWidth() - (messageText.getWidth() + 5));
            //feeText.setLayoutX(walletPane.getWidth() - (feeText.getWidth() + 5));
            walletBox.setLayoutX(walletPane.getWidth() - (walletBox.getWidth() + 5));
            walletAmount.setLayoutX(walletPane.getWidth() - ((walletAmount.getWidth() + 15)));
            tokenLabel.setLayoutX(walletAmount.getLayoutX() + 10);
            transactionTable.setMinWidth(walletPane.getWidth() - (walletBox.getWidth() + 65));
            transactionTable.setMinHeight(walletPane.getHeight() - 170);
            //versionLabel.setLayoutX(helpPane.getWidth() / 2 - (versionLabel.getWidth() / 2));
            //helpText.setLayoutX(helpPane.getWidth() / 2 - (helpText.getWidth() / 2));
            topPane2.setMinWidth(walletPane.getWidth());
            beScroll.setMinWidth(blockExplorerPane.getWidth());
            beScroll.setMinHeight(blockExplorerPane.getHeight() - 60);
            beScroll.setPrefHeight(blockExplorerPane.getHeight() - 60);
            lockPane.setMinHeight(borderPane.getHeight());
            miningDataHbox.setMinWidth(miningTab.getWidth());
            ohVbox.setMinHeight(ohPane.getHeight());
            ohVbox.setMinWidth(ohPane.getWidth() - 10);
            adxBox.setMinWidth(exchangePane.getWidth() - 20);
            adxBox.setMinHeight(exchangePane.getHeight() - 20);
        };
        goBE.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int start = 0;
                try {
                    start = Integer.parseInt(fromBlock.getText());
                } catch (Exception e) {
                    return;
                }
                int end = 0;
                try {
                    end = Integer.parseInt(toBlock.getText());
                } catch (Exception e) {
                    return;
                }

                if (start >= end) {
                    return;
                }
                if (start < 0) {
                    return;
                }
                if (BigInteger.valueOf(end).compareTo(ki.getChainMan().currentHeight()) > 0) {
                    return;
                }
                fillMasonry(BigInteger.valueOf(start), BigInteger.valueOf(end));
            }
        });
        stage.widthProperty().addListener(stageSizeListener);
        stage.heightProperty().addListener(stageSizeListener);
        HamburgerSlideCloseTransition burgerTask = new HamburgerSlideCloseTransition(menuHamburger);
        burgerTask.setRate(-1);
        menuHamburger.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                burgerTask.setRate(burgerTask.getRate() * -1);
                burgerTask.play();
                if (menuDrawer.isHidden() || menuDrawer.isHiding()) {
                    menuDrawer.open();
                    for (Timeline tl : btnAnimations) {
                        tl.play();
                    }
                } else {
                    menuDrawer.close();
                    for (Timeline tl : btnAnimationsR) {
                        tl.play();
                    }
                }
            }
        });

        ChangeListener<Color> cpListener = (observable, oldValue, newValue) -> {
            //System.out.println("Color changed");
            if (colorCombos.getSelectionModel().getSelectedItem().getText().contains("Primary Color")) {
                //System.out.println("Changing primary");
                menuHamburger.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));

                vb.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));

                sendButton.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                //sendButton.setOpacity(255);
                loadTransaction.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                copyAddress.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                copySelectedAdd.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                copyPublicKey.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                startMining.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                //System.out.println("style:" + miningIntesity.getStyle());
                String color = colorPicker.getValue().toString().replace("0x", "");
                color = "#" + color;
                //System.out.println("color: " + color);
                miningIntesity.setStyle("-jfx-default-thumb:" + color);
                createAddress.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                saveMSAddress.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                loadMSAddress.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                deleteAddress.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                setSpendAddress.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                changePassword.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                backToBE.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                backToEx.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                nextBlock.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                previousBlock.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                goBE.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                poolConnect.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                poolDisconnect.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                resetColors.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                exchangeBuy.setBackground(new Background(new BackgroundFill(Color.valueOf("#18BC9C"), CornerRadii.EMPTY, Insets.EMPTY)));
                orderHistory.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                limitBuy.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                marketBuy.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                exchangeSell.setBackground(new Background(new BackgroundFill(Color.valueOf("#c84128"), CornerRadii.EMPTY, Insets.EMPTY)));
                sellLabel.setTextFill(Color.valueOf("#c84128"));
                buyLabel.setTextFill(Color.valueOf("#18BC9C"));
                poolDynamicFeeSlider.setStyle("-jfx-default-thumb:" + color);
                payoutSlider.setStyle("-jfx-default-thumb:" + color);
                addKeyBtn.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                singleSig.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                multiSig.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                clearKeys.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));

                ki.setStringSetting(StringSettings.PRIMARY_COLOR, color);

                //miningIntesity.getClip().setStyle("-fx-background-color:"+color);

            } else if (colorCombos.getSelectionModel().getSelectedItem().getText().contains("Secondary Color")) {
                String color = colorPicker.getValue().toString().replace("0x", "");
                color = "#" + color;
                topPane.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                borderPane.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                for (Pane p : content) {
                    p.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                }
                ki.setStringSetting(StringSettings.SECONDARY_COLOR, color);
            } else if (colorCombos.getSelectionModel().getSelectedItem().getText().contains("Text Primary")) {
                for (Node n : vb.getChildren()) {
                    ((JFXButton) n).setTextFill(colorPicker.getValue());
                }
            } else if (colorCombos.getSelectionModel().getSelectedItem().getText().contains("Text Secondary")) {

            }

        };
        colorPicker.valueProperty().addListener(cpListener);
        colorPicker.setValue(Color.valueOf("#18BC9C"));
        menuHamburger.setStyle(vb.getStyle());
        menuDrawer.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (menuDrawer.isHiding()) {
                    burgerTask.setRate(burgerTask.getRate() * -1);
                    burgerTask.play();
                }
            }
        });

        poolConnect.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (paytoAddress.getText().isEmpty()) return;
                ki.getPoolData().payTo = Address.decodeFromChain(paytoAddress.getText());
                ki.getNetMan().attemptConnect(ipField.getText());
                ki.getPoolData().poolConn = ipField.getText();
                ki.setStringSetting(StringSettings.POOL_PAYTO, paytoAddress.getText());
                ki.setStringSetting(StringSettings.POOL_SERVER, ipField.getText());
            }
        });
        poolDisconnect.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                for (IConnectionManager connMan : ki.getNetMan().getConnections()) {
                    connMan.disconnect();
                    ki.getNetMan().getConnections().clear();
                    ki.getPoolData().poolConn = "";
                }
            }
        });
        for (String dev : ki.getMinerMan().getDevNames()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            hashrateChart.getData().add(series);
            series.setName(dev);
        }

        hashrateChart.getXAxis().setLabel("");
        hashrateChart.setLegendVisible(true);
        hashrateChart.setCreateSymbols(false);
        hashrateChart.getXAxis().setAnimated(false);

        sendButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (multiSigTransCache != null) {

                    multiSigTransCache.addSig(Utils.toBase64(multiSigBinaryCache.serializeToAmplet().serializeToBytes()), Utils.toBase64(multiSigWMCache.serializeToBytes()));

                    if (ki.getTransMan().verifyTransaction(multiSigTransCache)) {
                        ki.getTransMan().getPending().add(multiSigTransCache);
                        for (Input i : multiSigTransCache.getInputs()) {
                            ki.getTransMan().getUsedUTXOs().add(i.getID());
                        }
                        TransactionPacket tp = new TransactionPacket();
                        tp.trans = multiSigTransCache.serializeToAmplet().serializeToBytes();
                        ki.getNetMan().broadcast(tp);
                        notification("Sent transaction");

                    } else {
                        notification("Not enough signatures. Saving transaction to file.");
                        try (FileOutputStream fos = new FileOutputStream("Transaction.amp")) {
                            fos.write(multiSigTransCache.serializeToAmplet().serializeToBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (!ki.getAddMan().getMainAdd().isP2SH()) {
                    if (ki.getEncryptMan().getPublicKey(ki.getAddMan().getMainAdd().getKeyType()) != null) {

                        Token token = Token.byName(tokenBox.getSelectionModel().getSelectedItem().getText());

                        BigDecimal amt = new BigDecimal(amountText.getText());

                        BigInteger amount = amt.multiply(new BigDecimal("100000000.0")).toBigInteger();

                        IAddress receiver;
                        try {
                            receiver = Address.decodeFromChain(addressText.getText());
                        } catch (Exception e) {
                            notification("Send To Address Incorrect");
                            return;
                        }
                        // Output output = new Output(amount, receiver, token, index, System.currentTimeMillis(), (byte) 2);
                        //java.util.List<Output> outputs = new ArrayList<>();
                        //outputs.add(output);

                        //keys.add(ki.getEncryptMan().getPublicKeyString(ki.getAddMan().getMainAdd().getKeyType()));
                        //java.util.List<Input> inputs = new ArrayList<>();
                        BigInteger fee;
                        if (feeText.getText() == null || feeText.getText().isEmpty()) {
                            fee = BigInteger.TEN;
                        } else {
                            BigDecimal dFee = new BigDecimal(feeText.getText());
                            fee = dFee.multiply(new BigDecimal("100000000.0")).toBigInteger();
                            if (fee.compareTo(BigInteger.TEN) < 0) {
                                fee = BigInteger.TEN;
                            }
                        }
                        ki.getMainLog().info("Fee is: " + fee.toString());
                        /*
                        BigInteger totalInput = BigInteger.ZERO;
                        for (IAddress a : ki.getAddMan().getActive()) {
                            if (ki.getTransMan().getUTXOs(a, true) == null) return;
                            for (Output o : ki.getTransMan().getUTXOs(a, true)) {
                                if (o.getToken().equals(token)) {
                                    if (inputs.contains(Input.fromOutput(o))) continue;
                                    inputs.add(Input.fromOutput(o));
                                    totalInput = totalInput.add(o.getAmount());
                                    if (totalInput.compareTo(amount) >= 0) break;

                                }
                            }
                            if (totalInput.compareTo(amount) >= 0) break;

                        }
                        if (totalInput.compareTo(amount) < 0) {
                            ki.getMainLog().info("Not enough " + token.name() + " to do this transaction");
                            return; // not enough of this token to send;
                        }

                        BigInteger feeInput = (token.equals(Token.ORIGIN)) ? totalInput : BigInteger.ZERO;
                        for (IAddress a : ki.getAddMan().getActive()) {
                            //get inputs
                            if (feeInput.compareTo(fee) >= 0) break;
                            for (Output o : ki.getTransMan().getUTXOs(a, true)) {
                                if (o.getToken().equals(Token.ORIGIN)) {
                                    inputs.add(Input.fromOutput(o));
                                    feeInput = feeInput.add(o.getAmount());
                                    if (feeInput.compareTo(fee) >= 0) break;

                                }
                            }
                        }

                        if (feeInput.compareTo(fee) < 0) {
                            ki.getMainLog().info("Not enough origin to pay for this fee");
                            return; //not enough origin to send this kind of fee
                        }
                        */
                        ITrans trans = null;

                        try {
                            trans = ki.getTransMan().createSimple(receiver, amount, fee, token, messageText.getText());
                        } catch (InvalidTransactionException e) {
                            ki.getMainLog().error("Error creating transaction: ", e);
                            return;
                        }
                        if (ki.getTransMan().verifyTransaction(trans)) {
                            ki.getTransMan().getPending().add(trans);
                            for (Input i : trans.getInputs()) {
                                ki.getTransMan().getUsedUTXOs().add(i.getID());
                            }
                            TransactionPacket tp = new TransactionPacket();
                            tp.trans = trans.serializeToAmplet().serializeToBytes();
                            ki.getNetMan().broadcast(tp);
                            notification("Sent transaction");

                        } else {
                            ki.debug("Transaction did not verify, not sending and not adding to pending list");
                            notification("Transaction failed to send");
                        }
                    }
                } else {
                    //TODO find way to detect if this is a multi-sig wallet, for now we're just going to assume it is
                    Token token = Token.byName(tokenBox.getSelectionModel().getSelectedItem().getText());

                    BigDecimal amt = new BigDecimal(amountText.getText());

                    BigInteger amount = amt.multiply(new BigDecimal("100000000.0")).toBigInteger();

                    IAddress receiver;
                    try {
                        receiver = Address.decodeFromChain(addressText.getText());
                    } catch (Exception e) {
                        notification("Send To Address Incorrect");
                        return;
                    }

                    BigInteger fee;
                    if (feeText.getText() == null || feeText.getText().isEmpty()) {
                        fee = BigInteger.TEN;
                    } else {
                        BigDecimal dFee = new BigDecimal(feeText.getText());
                        fee = dFee.multiply(new BigDecimal("100000000.0")).toBigInteger();
                        if (fee.compareTo(BigInteger.TEN) < 0) {
                            fee = BigInteger.TEN;
                        }
                    }
                    ki.getMainLog().info("Fee is: " + fee.toString());

                    ITrans trans = null;
                    if (ki.getAddMan().getBinary(ki.getAddMan().getMainAdd()) == null) {
                        ki.getMainLog().warn("Do not have script for address: " + ki.getAddMan().getMainAdd().encodeForChain());
                        notification("Do not have script for this address");
                        return;
                    }
                    try {
                        trans = ki.getTransMan().createSimpleMultiSig(ki.getAddMan().getBinary(ki.getAddMan().getMainAdd()), receiver, amount, fee, token, messageText.getText(), 1);
                    } catch (InvalidTransactionException e) {
                        ki.getMainLog().error("Error creating transaction: ", e);
                        return;
                    }
                    //TODO this is kind of a weird hack, it relies on the transaction failing to verify to tell if there are enough sigs, we should be specifically checking the sigs.
                    if (ki.getTransMan().verifyTransaction(trans)) {
                        ki.getTransMan().getPending().add(trans);
                        for (Input i : trans.getInputs()) {
                            ki.getTransMan().getUsedUTXOs().add(i.getID());
                        }
                        TransactionPacket tp = new TransactionPacket();
                        tp.trans = trans.serializeToAmplet().serializeToBytes();
                        ki.getNetMan().broadcast(tp);
                        notification("Sent transaction");

                    } else {
                        notification("Not enough signatures. Saving transaction to file.");
                        try (FileOutputStream fos = new FileOutputStream("Transaction.amp")) {
                            fos.write(trans.serializeToAmplet().serializeToBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        loadTransaction.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                Path path = Paths.get("Transaction.amp");
                try {
                    ITrans trans = NewTrans.fromAmplet(Amplet.create(Files.readAllBytes(path)));
                    Binary bin = null;

                    for (IAddress a : ki.getAddMan().getAll()) {
                        if (trans.getInputs().get(0).getAddress().encodeForChain().equals(a.encodeForChain()) && a.isP2SH()) {
                            bin = ki.getAddMan().getBinary(a);
                        }
                    }
                    if (bin == null) {
                        notification("Do not have script for the address in this transaction");
                        return;
                    }
                    WritableMemory wm = WritableMemory.deserializeFromBytes(Utils.fromBase64(trans.getSig(Utils.toBase64(bin.serializeToAmplet().serializeToBytes()))));

                    int i = 0;
                    for (; i < 32; i++) {
                        try {
                            KeyKeyTypePair kktp = KeyKeyTypePair.fromBytes(bin.getConstantMemory().getElement(i).getData());
                            if (kktp == null) break;
                            if (kktp.getKey() == null) break;
                            if (kktp.getKeyType() == null) break;
                            if (ki.getEncryptMan().getPublicKeyString(kktp.getKeyType()).equals(Utils.toBase64(kktp.getKey()))) {
                                wm.setElement(ki.getEncryptMan().sign(trans.toSignBytes(), kktp.getKeyType()), i);
                            }
                        } catch (Exception e) {
                            //fail quietly
                            break;
                        }
                    }
                    multiSigTransCache = trans;
                    multiSigBinaryCache = bin;
                    multiSigWMCache = wm;
                    BigInteger amount = BigInteger.ZERO;
                    for (Output out : trans.getOutputs()) {
                        if (out.getAddress().encodeForChain().equals(trans.getInputs().get(0).getAddress().encodeForChain()))
                            continue;
                        addressText.setText(out.getAddress().encodeForChain());
                        amount = amount.add(out.getAmount());
                    }
                    messageText.setText(trans.getMessage());
                    amountText.setText("" + (amount.doubleValue() / 100_000_000D));

                    //trans.addSig(Utils.toBase64(bin.serializeToAmplet().serializeToBytes()),Utils.toBase64(wm.serializeToBytes()));

                } catch (IOException e) {
                    ki.getMainLog().error("Could not load transaction from file Transaction.amp", e);
                }
            }
        });
        addressList.getSelectionModel().select(0);
        List<Long> average = new ArrayList<>();
        XYChart.Series<String, Number> candleSeries = new XYChart.Series<>();
        exchangeGraph.getData().add(candleSeries);
        exchangeGraph.getXAxis().setAnimated(false);
        exchangeGraph.getYAxis().setAnimated(false);
        exchangeGraph.setAnimated(false);
        exchangeGraph.setCreateSymbols(false);
        exchangeGraph.setPrefHeight(5000);
        exchangeGraph.setPrefWidth(5000);
        pnlGraph.setLegendVisible(false);
        ohCancel.setFixedCellSize(40);
        ohAmount.setFixedCellSize(40);
        ohDirection.setFixedCellSize(40);
        ohDate.setFixedCellSize(40);
        ohPrice.setFixedCellSize(40);


        bindScrolls(obSellPrice, obSellSize);
        bindScrolls(obSellSize, obSellTotal);
        bindScrolls(obBuyPrice, obBuySize);
        bindScrolls(obBuySize, obBuyTotal);
        bindScrolls(obRecentPrice, obRecentAmount);
        bindScrolls(obRecentAmount, obRecentDirection);
        bindScrolls(activeAmount, activePrice);
        ListProperty<Order> sells = new SimpleListProperty<>();
        sells.set(FXCollections.observableArrayList());
        sells.bindContent(ki.getExMan().getOrderBook().sells());
        //sells.set(ki.getExMan().getOrderBook().sells());
        obSellPrice.itemsProperty().bind(sells);
        obSellPrice.setCellFactory(param -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.unitPrice() == null) {
                    setText(null);
                } else {
                    setText(format2.format(item.unitPrice().doubleValue() / (unitMultiplierPrice.doubleValue())));
                }
            }
        });
        obSellSize.itemsProperty().bind(sells);
        obSellSize.setCellFactory(param -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.amountOnOffer() == null) {
                    setText(null);
                } else {
                    setText(format2.format(item.amountOnOffer().doubleValue() / (unitMultiplierAmount.doubleValue())));
                }
            }
        });
        obSellTotal.itemsProperty().bind(sells);
        obSellTotal.setCellFactory(param -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.amountOnOffer() == null) {
                    setText(null);
                } else {
                    setText(format2.format(item.getOm().totalAtOrder.doubleValue() / (unitMultiplierAmount.doubleValue())));
                }
            }
        });

        ListProperty<Order> active = new SimpleListProperty<>();
        active.set(FXCollections.observableArrayList());
        active.bindContent(ki.getExMan().getOrderBook().active());
        activePrice.itemsProperty().bind(active);
        activeAmount.itemsProperty().bind(active);
        activePrice.setCellFactory(param -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.unitPrice() == null) {
                    setText(null);
                } else {
                    setText(format2.format(item.unitPrice().doubleValue() / (unitMultiplierPrice.doubleValue())));
                    setStyle("-fx-text-fill:" + ((!item.buy()) ? ("#c84128") : ("#18BC9C")));
                }
            }
        });
        //obSellSize.itemsProperty().bind(sells);
        activeAmount.setCellFactory(param -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.amountOnOffer() == null) {
                    setText(null);
                } else {
                    setText(format2.format(item.amountOnOffer().doubleValue() / (unitMultiplierAmount.doubleValue())));
                    setStyle("-fx-text-fill:" + ((!item.buy()) ? ("#c84128") : ("#18BC9C")));
                }

            }
        });
        ListProperty<Order> buys = new SimpleListProperty<>();
        //buys.set(ki.getExMan().getOrderBook().buys());
        buys.set(FXCollections.observableArrayList());
        buys.bindContent(ki.getExMan().getOrderBook().buys());
        obBuyPrice.itemsProperty().bind(buys);
        obBuySize.itemsProperty().bind(buys);
        obBuyPrice.setCellFactory(param -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.unitPrice() == null) {
                    setText(null);
                } else {
                    setText(format2.format(item.unitPrice().doubleValue() / (unitMultiplierPrice.doubleValue())));
                }
            }
        });
        //obSellSize.itemsProperty().bind(sells);
        obBuySize.setCellFactory(param -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.amountOnOffer() == null) {
                    setText(null);
                } else {
                    setText(format2.format(item.amountOnOffer().doubleValue() / (unitMultiplierAmount.doubleValue())));
                }

            }
        });
        obBuyTotal.itemsProperty().bind(buys);
        obBuyTotal.setCellFactory(param -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.getOm() == null) {
                    setText(null);
                } else {
                    setText(format2.format(item.getOm().totalAtOrder.doubleValue() / (unitMultiplierAmount.doubleValue())));
                }
            }
        });


        ListProperty<Order> matched = new SimpleListProperty<>();
        //matched.set(ki.getExMan().getOrderBook().matched());
        matched.set(FXCollections.observableArrayList());
        matched.bindContent(ki.getExMan().getOrderBook().matched());
        obRecentPrice.itemsProperty().bind(matched);
        obRecentAmount.itemsProperty().bind(matched);
        obRecentDirection.itemsProperty().bind(matched);

        obRecentPrice.setCellFactory(param -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.unitPrice() == null) {
                    setText(null);
                } else {
                    setText(format2.format(item.unitPrice().doubleValue() / (unitMultiplierPrice.doubleValue())));
                    setStyle("-fx-text-fill:" + ((item.buy()) ? ("#c84128") : ("#18BC9C")));
                }
            }
        });
        //obSellSize.itemsProperty().bind(sells);
        obRecentAmount.setCellFactory(param -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.amountOnOffer() == null) {
                    setText(null);
                } else {
                    setText(format2.format(item.amountOnOffer().doubleValue() / (unitMultiplierAmount.doubleValue())));
                    setStyle("-fx-text-fill:" + ((item.buy()) ? ("#c84128") : ("#18BC9C")));
                }
            }
        });
        obRecentDirection.setCellFactory(param -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText((item.buy()) ? "Sell" : "Buy");
                    setStyle("-fx-text-fill:" + ((item.buy()) ? ("#c84128") : ("#18BC9C")));
                }
            }
        });

        new Thread() {
            public void run() {
                setName("GUI-Updater");
                int i = 0;
                while (true) {
                    i++;
                    if (ki.getMinerMan().isMining()) {
                        mining = true;
                        Platform.runLater(new Thread() {
                            public void run() {
                                startMining.setText("Stop Mining");
                            }
                        });
                    }
                    if (i % 10 == 0) {


                        Platform.runLater(new Thread() {
                            public void run() {
                                setDaemon(true);
                                ki.getExMan().getOrderBook().sort();

                                /*
                                if (!ki.getExMan().getOrderBook().hasRun()) {
                                    for (int i = 0; i < ki.getExMan().getOrderBook().getData().size(); i++) {
                                        ExchangeData data = ki.getExMan().getOrderBook().getData().get(i);
                                        ki.getExMan().getOrderBook().setRecent(data);
                                        exchangeGraph.getData().get(0).getData().add(new XYChart.Data<>(sdf.format(new Date(data.timestamp)), data.open));
                                        ki.getExMan().getOrderBook().setRecent(null);
                                    }
                                    ki.getExMan().getOrderBook().run();
                                } else if (ki.getExMan().getOrderBook().hasNew()) {
                                    exchangeGraph.getData().get(0).getData().add(new XYChart.Data<>(sdf.format(new Date(ki.getExMan().getOrderBook().getRecentData().timestamp)), ki.getExMan().getOrderBook().getRecentData().open));

                                }
                                */
                                exchangeGraph.layoutPlotChildren();
                                exchangeGraph.updateAxisRange();
                                if (ki.getExMan().getOrderBook().getRecentData() != null) {
                                    currentOpen.setText("Open - " + ki.getExMan().getOrderBook().getRecentData().open.doubleValue() / unitMultiplierPrice.doubleValue());
                                    currentClose.setText("Close - " + ki.getExMan().getOrderBook().getRecentData().close.doubleValue() / unitMultiplierPrice.doubleValue());
                                    currentHigh.setText("High - " + ki.getExMan().getOrderBook().getRecentData().high.doubleValue() / unitMultiplierPrice.doubleValue());
                                    currentLow.setText("Low - " + ki.getExMan().getOrderBook().getRecentData().low.doubleValue() / unitMultiplierPrice.doubleValue());
                                    lastTradeAmount.setText(format2.format(ki.getExMan().getOrderBook().getRecentData().close.doubleValue() / unitMultiplierPrice.doubleValue()) + " " + unitSelectorPrice.getSelectionModel().getSelectedItem().getText());
                                }


                                /*
                                BigInteger sellTotal = BigInteger.ZERO;
                                obSellTotal.getItems().clear();
                                for(Order o:ki.getExMan().getOrderBook().sells())
                                {
                                    sellTotal = sellTotal.add(o.amountOnOffer());
                                    obSellTotal.getItems().add(format2.format(sellTotal.doubleValue()/(unitMultiplierPrice.doubleValue())));
                                }
                                BigInteger buyTotal = BigInteger.ZERO;
                                obBuyTotal.getItems().clear();
                                for(Order o:ki.getExMan().getOrderBook().buys())
                                {
                                    buyTotal = buyTotal.add(o.amountOnOffer());
                                    obBuyTotal.getItems().add(format2.format(buyTotal.doubleValue()/(unitMultiplierPrice.doubleValue())));
                                }
                                */
                            }
                        });
                    }
                    if ((i % 10 == 0) && mining) {

                        if (!ki.getMinerMan().isMining()) {
                            mining = false;
                            Platform.runLater(new Thread() {
                                public void run() {
                                    startMining.setText("Start Mining");
                                }
                            });
                        }
                        Platform.runLater(new Thread() {
                            public void run() {

                                setDaemon(true);
                                for (XYChart.Series<String, Number> series : hashrateChart.getData()) {
                                    try {
                                        ki.getMinerMan().getHashrate(series.getName());
                                    } catch (Exception e) {
                                        return;
                                    }
                                    if (series.getData().size() > 60) {
                                        series.getData().remove(0);
                                    }
                                    //ki.debug("Adding mining data: " + sdf.format(new Date(System.currentTimeMillis())) + " " + ki.getMinerMan().getHashrate(series.getName())/1_000_000);
                                    series.getData().add(new XYChart.Data<>(sdf.format(new Date(System.currentTimeMillis())), ki.getMinerMan().getHashrate(series.getName()) / 1_000_000));
                                    long chash = ki.getMinerMan().cumulativeHashrate() / 1_000_000;
                                    currentHashrate.setText("Current Hashrate - " + chash + " Mh/s");
                                    if (chash > maxH) {
                                        maxH = chash;
                                        maxHashrate.setText("Max - " + chash + " Mh/s");
                                    }
                                    if (chash < minH) {
                                        minH = chash;
                                        minHashrate.setText("Min - " + chash + " Mh/s");
                                    }
                                    blocksFoundLabel.setText("Blocks Found - " + format2.format(blocksFoundInt));
                                    average.add(chash);
                                    if (average.size() > 25) {
                                        average.remove(0);
                                    }
                                    long averageH = 0;
                                    for (Long l : average) {
                                        averageH += l;
                                    }
                                    averageH = averageH / average.size();
                                    averageHashrate.setText("Average - " + averageH + " Mh/s");
                                }

                            }
                        });
                    }

                    Platform.runLater(new Thread() {
                        public void run() {
                            startMining.setLayoutX((miningTab.getWidth() / 2) - (startMining.getWidth() / 2) - 5);
                            hashrateChart.setMinWidth(miningTab.getWidth());
                            miningIntesity.setMinWidth(miningTab.getWidth() - 20);
                            miLabel.setLayoutX((miningTab.getWidth() / 2) - (miLabel.getWidth() / 2) - 5);
                            //addressText.setLayoutX(walletPane.getWidth() - (addressText.getWidth() + 5));
                            //amountText.setLayoutX(walletPane.getWidth() - (amountText.getWidth() + 5));
                            //messageText.setLayoutX(walletPane.getWidth() - (messageText.getWidth() + 5));
                            //feeText.setLayoutX(walletPane.getWidth() - (feeText.getWidth() + 5));
                            walletBox.setLayoutX(walletPane.getWidth() - (walletBox.getWidth() + 5));
                            walletAmount.setLayoutX(walletPane.getWidth() - (walletAmount.getWidth() + 15));
                            tokenLabel.setLayoutX(walletAmount.getLayoutX() + 10);
                            transactionTable.setMinWidth(walletPane.getWidth() - (walletBox.getWidth() + 65));
                            transactionTable.setMinHeight(walletPane.getHeight() - 170);
                            //versionLabel.setLayoutX(helpPane.getWidth() / 2 - (versionLabel.getWidth() / 2));
                            //helpText.setLayoutX(helpPane.getWidth() / 2 - (helpText.getWidth() / 2));
                            topPane2.setMinWidth(walletPane.getWidth());
                            beScroll.setMinWidth(blockExplorerPane.getWidth());
                            beScroll.setMinHeight(blockExplorerPane.getHeight() - 60);
                            beScroll.setPrefHeight(blockExplorerPane.getHeight() - 60);
                            lockPane.setMinHeight(borderPane.getHeight());
                            ohVbox.setMinHeight(ohPane.getHeight());
                            ohVbox.setMinWidth(ohPane.getWidth() - 10);

                            adxBox.setMinWidth(exchangePane.getWidth() - 20);
                            adxBox.setMinHeight(exchangePane.getHeight() - 20);
                            heightLabel.setText("Chain Height - " + ki.getChainMan().currentHeight());
                            if (!ki.getOptions().pool)
                                chainHeight2.setText(" " + ki.getChainMan().currentHeight().toString());
                            miningDataHbox.setMinWidth(miningTab.getWidth());

                            if (startHeight != null && startHeight.compareTo(loadHeight) != 0) {
                                //System.out.println("================CURRENT SYNC: " + startHeight.subtract(ki.getChainMan().currentHeight()).multiply(BigInteger.valueOf(100)).divide(startHeight.subtract(loadHeight)).doubleValue() / 100);
                                syncProgress.setProgress(1 - (startHeight.subtract(ki.getChainMan().currentHeight()).multiply(BigInteger.valueOf(100)).divide(startHeight.subtract(loadHeight)).doubleValue() / 100));
                            } else if (startHeight != null && startHeight.compareTo(loadHeight) == 0)
                                syncProgress.setProgress(1);
                            for (IConnectionManager c : ki.getNetMan().getConnections()) {

                                //ki.debug("Latency - " + c.currentLatency());
                                latency.setText(" Latency - " + c.currentLatency());

                            }
                            startMining.setDisable(!GPUMiner.initDone);
                            if (!GPUMiner.initDone) {
                                startMining.setText("Autotuning...");
                            } else if (frg) {
                                frg = false;
                                for (String name : ki.getMinerMan().getDevNames()) {
                                    JFXToggleButton tb = new JFXToggleButton();
                                    tb.setText(name);
                                    tb.setTextFill(Color.WHITE);
                                    tb.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                                        @Override
                                        public void handle(MouseEvent event) {
                                            if (!tb.isSelected())
                                                ki.getMinerMan().disableDev(tb.getText());
                                            else
                                                ki.getMinerMan().enableDev(tb.getText());
                                        }
                                    });
                                    tb.setSelected(true);

                                    devicesBox.getChildren().add(tb);
                                }
                                startMining.setText("Start Mining");
                            }
                            if (ki.getOptions().pool) {
                                shares.setText("Accepted Shares - " + currentShares);
                                localShares.setText("Local Shares - " + localShare);
                                //ki.debug("Current PPS: " + currentPPS);
                                nextPayment.setText("Next Payment - " + format2.format(((currentShares * currentPPS)) / 100_000_000));
                                if (ki.getNetMan().getConnections().size() > 0) {
                                    poolConnected.setText("Connected");
                                    poolConnect.setDisable(true);
                                } else {
                                    poolConnected.setText("Not Connected");
                                    poolConnect.setDisable(false);
                                    ki.getMinerMan().stopMiners();
                                }
                            }
                            if (ki.getOptions().poolRelay) {
                                long totalHR = 0;
                                for (String ID : ki.getPoolData().hrMap.keySet()) {
                                    totalHR += (ki.getPoolData().hrMap.get(ID) / 1000000);
                                }
                                poolHashrate.setText("Current Pool Hashrate (MH/s) - " + totalHR);
                                poolNOC.setText("Number of Connections - " + ki.getPoolNet().getConnections().size());
                                currentPoolShares.setText("Current Pool Shares - " + ki.getPoolManager().getTotalSharesOfCurrentPayPeriod());
                                //ki.debug("Current PPS: " + ki.getPoolManager().getCurrentPayPerShare());
                                estimatedNextPayout.setText("Estimated Next Payout - " + format2.format((double) (ki.getPoolManager().getTotalSharesOfCurrentPayPeriod() * (double) ((double) ki.getPoolManager().getCurrentPayPerShare() / 100_000_000D))));
                            }


                        }
                    });
                    Platform.runLater(new Thread() {
                        public void run() {

                            if (!isFinal) return;
                            //System.out.println("Setting the shit");
                            walletAmount.setText(format2.format((double) tokenValueMap.get(Token.byName(tokenBox.getSelectionModel().getSelectedItem().getText())).longValueExact() / 100_000_000));
                        }
                    });
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        ki.getMainLog().error("GUI update thread interrupted ", e);
                    }
                }
            }
        }.start();

        for (Token t : Token.values()) {
            if (t.getName().contains("TOKEN")) continue;
            JFXButton button = new JFXButton(t.getName());
            button.setWrapText(true);
            button.setMinHeight(60);
            button.setPrefWidth(1000);
            button.setBackground(new Background(new BackgroundFill(Color.valueOf(getDefaultColor(new Random().nextInt(12))), CornerRadii.EMPTY, Insets.EMPTY)));
            Label l = new Label(t.getName());
            //l.setStyle("-fx-text-fill:BLACK");
            tokenBox.getItems().add(l);

        }
        tokenBox.getSelectionModel().select(0);
    }

    private JFXButton buildMainButton(String text, String image, int offset, int graphicOffset, List<Pane> content, Pane show) {
        JFXButton button = buildButton(text, image, offset, graphicOffset);
        button.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                for (Pane p : content) {
                    p.setVisible(false);
                }
                show.setVisible(true);
            }
        });
        return button;
    }

    private JFXButton buildButton(String text, String image, int offset, int graphicOffset) {
        for (int i = 0; i < graphicOffset; i++) {
            if (i % 2 != 0)
                text = " " + text;
            else
                text = text + " ";
        }
        JFXButton button = new JFXButton(text);
        button.setRipplerFill(Color.valueOf(getDefaultColor(new Random().nextInt(12))));
        Image img = new Image(getClass().getResourceAsStream(image));

        ImageView iv = new ImageView(img);
        //iv.setTranslateX(-20);
        iv.setFitWidth(20);
        iv.setPreserveRatio(true);
        //iv.setX(20);
        button.setGraphic(iv);
        //button.getGraphic().autosize();
        //button.setBackground(new Background(new BackgroundImage(img, BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,new BackgroundSize(75,75,true,true,true,false))));
        button.setPrefWidth(Double.MAX_VALUE);
        button.setPrefHeight(50);
        button.setDefaultButton(false);
        button.setGraphicTextGap(5);
        button.setTextAlignment(TextAlignment.LEFT);

        button.disarm();
        button.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                button.setRipplerFill(Color.valueOf(getDefaultColor(new Random().nextInt(12))));
            }
        });

        button.setTranslateX(-200);
        //button.setTranslateY(-200);
        Timeline animation = new Timeline(new KeyFrame(Duration.millis(300),
                new KeyValue(button.translateXProperty(),
                        0,
                        EASE_BOTH)));
        animation.setDelay(Duration.millis(200 + offset));
        Timeline animation2 = new Timeline(new KeyFrame(Duration.millis(200),
                new KeyValue(button.translateXProperty(),
                        -200,
                        EASE_BOTH)));
        animation.setDelay(Duration.millis(offset));
        btnAnimations.add(animation);
        btnAnimationsR.add(animation2);
        button.setFont(Font.loadFont(getClass().getResourceAsStream("/ADAM.CG PRO.otf"), 10));

        return button;
    }

    private BigInteger currentBlock = BigInteger.ZERO;

    /**
     * so, this method was built to fill in the block explorer masonry pane because I really liked the effect of
     * the masonry pane. It works, but the masonry effect never became what I actually wanted, mostly because
     * JFoenix (the library that allows this) is fucking retarded and does stupid shit with the masonry pane that
     * makes it really hard to do an interesting combination of sizes of blocks, so, most blocks are the same or
     * very similar in size.
     *
     * @param bottomRange from block
     * @param topRange    to block
     */
    private void fillMasonry(BigInteger bottomRange, BigInteger topRange) {
        if (topRange.compareTo(ki.getChainMan().currentHeight()) > 0) {
            if (ki.getChainMan().currentHeight().compareTo(BigInteger.valueOf(100)) < 0) return;
            notification("Invalid range");
            return;
        }
        if (bottomRange.compareTo(BigInteger.ZERO) < 0) {
            notification("Invalid range");
            return;
        }
        int i2 = topRange.subtract(bottomRange).intValueExact();

        JFXMasonryPane mp = new JFXMasonryPane();
        beScroll.setContent(mp);
        beScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        beScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        beScroll.setFitToHeight(true);
        beScroll.setFitToWidth(true);
        beScroll.setPannable(true);
        //beScroll.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        mp.setLayoutX(0);
        mp.setLayoutY(0);
        mp.setMinWidth(beScroll.getWidth());
        mp.setMinHeight(beScroll.getHeight());
        List<Node> children = new ArrayList<>();
        Font f = Font.loadFont(getClass().getResourceAsStream("/ADAM.CG PRO.otf"), 12);
        mp.setCellWidth(70);
        mp.setCellHeight(20);
        mp.setLayoutMode(JFXMasonryPane.LayoutMode.MASONRY);

        for (int i = 0; i < i2; i++) {
            Block b = ki.getChainMan().getByHeight(topRange.subtract(BigInteger.valueOf(i)));

            JFXButton btn = new JFXButton("Block\n" + b.height);
            btn.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    setupBlockPane(b.height);
                }
            });
            Color c = Color.valueOf(getDefaultColor(new Random().nextInt(12)));
            btn.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY)));

            btn.setPrefSize(400, 400);
            StackPane sp = new StackPane();
            double width = 70;
            double height = (b.getTransactionKeys().size() / 20) * 5 + 50;
            sp.setPrefSize(width, height);
            sp.setMinSize(width, height);
            sp.setMaxSize(width, height);
            sp.autosize();
            sp.requestLayout();
            sp.getChildren().add(btn);
            btn.setFont(f);
            children.add(sp);
        }

        mp.getChildren().addAll(children);
        JFXScrollPane.smoothScrolling(beScroll);
        Platform.runLater(() ->
                beScroll.requestLayout());
    }

    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy MMM dd hh:mm:ss a zzz");
    private SimpleDateFormat sdf3 = new SimpleDateFormat("MM/dd hh:mm:ss a");

    private void setupBlockPane(BigInteger height) {
        Block b = ki.getChainMan().getByHeight(height);
        blockHeight.setText("Height - " + b.height);
        blockID.setText("ID - " + Utils.toHexArray(Utils.fromBase64(b.ID)));
        solver.setText("Solver - " + b.solver);
        numberOfTransactions.setText("Number of Transactions - " + b.getTransactionKeys().size());
        blockTimestamp.setText(sdf2.format(new Date(b.timestamp)));
        currentBlock = b.height;
        blockExplorerPane.setVisible(false);
        blockPane.setVisible(true);
    }

    private int localShare = 0;

    public void bindScrolls(JFXListView list1, JFXListView list2) {
        Node n1 = list1.lookup(".scroll-bar");
        if (n1 instanceof ScrollBar) {
            ScrollBar sb1 = (ScrollBar) n1;
            Node n2 = list2.lookup(".scroll-bar");
            if (n2 instanceof ScrollBar) {
                ScrollBar sb2 = (ScrollBar) n2;
                sb1.valueProperty().bindBidirectional(sb2.valueProperty());
            }
        }
    }

    public void addShare() {
        localShare++;
    }

    private String getDefaultColor(int i) {
        String color = "#FFFFFF";
        switch (i) {
            case 0:
                color = "#8F3F7E";
                break;
            case 1:
                color = "#B5305F";
                break;
            case 2:
                color = "#CE584A";
                break;
            case 3:
                color = "#DB8D5C";
                break;
            case 4:
                color = "#DA854E";
                break;
            case 5:
                color = "#E9AB44";
                break;
            case 6:
                color = "#FEE435";
                break;
            case 7:
                color = "#99C286";
                break;
            case 8:
                color = "#01A05E";
                break;
            case 9:
                color = "#4A8895";
                break;
            case 10:
                color = "#16669B";
                break;
            case 11:
                color = "#2F65A5";
                break;
            case 12:
                color = "#4E6A9C";
                break;
            default:
                break;
        }
        return color;
    }

    private volatile int blocksFoundInt = 0;
    private BigInteger latestBlock = BigInteger.ZERO;
    //private XodusStringMap guiMap = new XodusStringMap("gui.data");
    private XodusAmpMap guiXAM = new XodusAmpMap("ngui");

    public void blockFound() {
        if (ki.getChainMan().currentHeight().compareTo(latestBlock) > 0) {
            blocksFoundInt++;
            latestBlock = ki.getChainMan().currentHeight();
            guiXAM.putBytes("blocksFound", ByteTools.deconstructInt(blocksFoundInt));
            guiXAM.putBytes("latest", latestBlock.toByteArray());
        }
        notification("Found a block");
    }

    private JFXSnackbar sb;
    private void notification(String text) {
        Platform.runLater(new Thread() {
            public void run() {
                if (sb == null) {
                    sb = new JFXSnackbar(topPane2);
                }
                sb.close();
                sb.enqueue(new JFXSnackbar.SnackbarEvent(text));
            }
        });
    }

    @FXML
    public void passwordEnter(ActionEvent event) {
        handlePassword();
    }

    private void createNewPassword(String password) {
        if (password == null || password.isEmpty()) return;
        String hash = "";
        hash = superHash(password + hash, 64);
        pmap.put(hash, "p");
    }

    private void deleteOldPassword(String password) {
        if (password == null || password.isEmpty()) return;
        String hash = "";
        hash = superHash(password + hash, 64);
        pmap.remove(hash);
    }

    private boolean verifyPassword(String password) {
        if (password == null || password.isEmpty()) return false;
        if (pmap.get("fr") == null) return true;
        String hash = "";
        hash = superHash(password + hash, 64);
        return pmap.get(hash) != null;
    }

    private void handlePassword() {
        submitPassword.setDisable(true);
        passwordWaiter.setVisible(true);
        new Thread() {
            public void run() {
                if (!passwordField.getText().isEmpty()) {
                    if (pmap.get("fr") == null) {
                        if (!confirmPassword.getText().equals(passwordField.getText())) {
                            Label l = new Label("Passwords don't match");
                            l.setStyle("-fx-text-fill:RED");
                            passwordVbox.getChildren().add(l);
                            return;
                        }
                        String hash = "";
                        hash = superHash(passwordField.getText() + hash, 64);
                        pmap.put("fr", "fr");
                        pmap.put(hash, "p");
                        Platform.runLater(new Thread() {
                            public void run() {
                                submitPassword.setDisable(false);
                                passwordWaiter.setVisible(false);
                                lockAnimation.play();
                            }
                        });
                    } else {
                        String hash = "";
                        hash = superHash(passwordField.getText() + hash, 64);
                        String hash2 = hash;
                        Platform.runLater(new Thread() {
                            public void run() {
                                if (pmap.get(hash2) != null) {
                                    lockAnimation.play();
                                } else {
                                    Label l = new Label("Incorrect");
                                    l.setStyle("-fx-text-fill:RED");
                                    passwordVbox.getChildren().add(l);
                                }

                                submitPassword.setDisable(false);

                                passwordWaiter.setVisible(false);
                            }
                        });
                    }
                } else {
                    Label l = new Label("Incorrect");
                    l.setStyle("-fx-text-fill:RED");
                    l.setLayoutX(passwordField.getLayoutX());
                    l.setLayoutY(passwordField.getLayoutY() + 60);
                }
            }
        }.start();
    }

    private BigInteger startHeight;
    private long currentShares = 0;
    private double currentPPS = 0;

    private void prepareOH() {
        while (!isFinal) {
        }
        ohPortfolio.getData().clear();
        for (Map.Entry<Token, BigInteger> t : tokenValueMap.entrySet()) {
            if (!t.getKey().getName().contains("TOKEN") && t.getValue().compareTo(BigInteger.ZERO) != 0)
                ohPortfolio.getData().add(new PieChart.Data(t.getKey().getName(), t.getValue().divide(BigInteger.valueOf(100_000_000)).doubleValue()));
        }
        ohAmount.getItems().clear();
        ohCancel.getItems().clear();
        ohDate.getItems().clear();
        ohDirection.getItems().clear();
        ohPrice.getItems().clear();

        for (Order o : ki.getExMan().getOrderBook().buys()) {
            for (IAddress a : ki.getAddMan().getAll()) {
                if (o.address().encodeForChain().equals(a.encodeForChain())) {
                    addOrderToOH(o, true);
                    break;
                }
            }
        }
        for (Order o : ki.getExMan().getOrderBook().sells()) {
            for (IAddress a : ki.getAddMan().getAll()) {
                if (o.address().encodeForChain().equals(a.encodeForChain())) {
                    addOrderToOH(o, true);
                    break;
                }
            }
        }
        pnlGraph.getData().clear();
        XYChart.Series<String, Number> pnlSeries = new XYChart.Series<>();
        List<XYChart.Data<String, Number>> realizedPNL = new ArrayList<>();
        BigInteger buyPrice = BigInteger.ZERO;
        BigInteger sellPrice = BigInteger.ZERO;
        for (Order o : ki.getExMan().getOrderBook().matched()) {
            for (IAddress a : ki.getAddMan().getAll()) {
                if (o.address().encodeForChain().equals(a.encodeForChain())) {
                    if (o.buy()) {
                        buyPrice = o.unitPrice();
                    } else {
                        if (buyPrice.compareTo(BigInteger.ZERO) != 0) {
                            sellPrice = o.unitPrice();
                            realizedPNL.add(0, new XYChart.Data<String, Number>(sdf3.format(new Date(o.timestamp().longValueExact())), 100 * ((sellPrice.doubleValue() / (buyPrice.doubleValue())) - 1)));
                        }
                    }
                    addOrderToOH(o, false);
                    break;
                }
            }
        }
        if (ki.getExMan().getOrderBook().getRecentData() != null) {
            double currentPrice = ki.getExMan().getOrderBook().getRecentData().close.doubleValue();
            pnlLabel.setText("Unrealized PNL - " + (((currentPrice / buyPrice.doubleValue()) - 1) * 100) + "%");
        }
        for (XYChart.Data<String, Number> data : realizedPNL) {
            pnlSeries.getData().add(data);
        }
        pnlGraph.getData().add(pnlSeries);
        bindScrolls(ohPrice, ohAmount);
        bindScrolls(ohAmount, ohDate);

        bindScrolls(ohDate, ohDirection);
        bindScrolls(ohDirection, ohCancel);
        pnlGraph.layout();
    }

    private void addOrderToOH(Order o, boolean cancellable) {
        ohAmount.getItems().add(format2.format(o.amountOnOffer().doubleValue() / (unitMultiplierAmount.doubleValue())) + " " + unitSelectorAmount.getSelectionModel().getSelectedItem().getText());
        ohDate.getItems().add(sdf2.format(new Date(o.timestamp().longValueExact())));
        ohDirection.getItems().add((o.buy()) ? "Buy" : "Sell");
        ohPrice.getItems().add(format2.format(o.unitPrice().doubleValue() / (unitMultiplierPrice.doubleValue())) + " " + unitSelectorPrice.getSelectionModel().getSelectedItem().getText());
        JFXButton cancelButton = new JFXButton("Cancel");
        cancelButton.setBackground(new Background(new BackgroundFill(Color.valueOf(ki.getStringSetting(StringSettings.PRIMARY_COLOR)), CornerRadii.EMPTY, Insets.EMPTY)));
        if (cancellable) {
            cancelButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (!ki.getExMan().cancelOrder(o)) {
                        notification("Order could not be cancelled");
                    } else {
                        ohAmount.getItems().remove(ohCancel.getItems().indexOf(cancelButton));
                        ohDate.getItems().remove(ohCancel.getItems().indexOf(cancelButton));
                        ohDirection.getItems().remove(ohCancel.getItems().indexOf(cancelButton));
                        ohPrice.getItems().remove(ohCancel.getItems().indexOf(cancelButton));
                        ohCancel.getItems().remove(cancelButton);
                        notification("Order successfully cancelled");
                    }
                }
            });
        } else {
            cancelButton.setDisable(true);
        }
        ohCancel.getItems().add(cancelButton);
    }

    public void setStart(BigInteger startHeight) {
        this.startHeight = startHeight;
    }

    public void updatePoolStats(long currentShares, double currentPPS) {
        this.currentShares = currentShares;
        this.currentPPS = currentPPS;
    }
}