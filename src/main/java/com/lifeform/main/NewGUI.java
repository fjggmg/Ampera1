package com.lifeform.main;

import com.jfoenix.controls.*;
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition;
import com.jfoenix.validation.RequiredFieldValidator;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.XodusStringMap;
import com.lifeform.main.network.TransactionPacket;
import com.lifeform.main.transactions.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.Thread.sleep;
import static javafx.animation.Interpolator.EASE_BOTH;

public class NewGUI {

    private static final String FX_TEXT_FILL_WHITE = "-fx-text-fill:WHITE";
    private static final String ANIMATED_OPTION_BUTTON = "animated-option-button";
    private static final String ANIMATED_OPTION_SUB_BUTTON = "animated-option-sub-button";
    private static final String ANIMATED_OPTION_SUB_BUTTON2 = "animated-option-sub-button2";
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
    private IKi ki;

    private ObservableMap<Token, BigInteger> tokenValueMap = FXCollections.observableMap(new HashMap<Token, BigInteger>());
    private volatile boolean isFinal = false;
    private volatile boolean run = true;

    public NewGUI() {
        ki = Ki.getInstance();
        for (Token t : Token.values()) {
            tokenValueMap.put(t, BigInteger.ZERO);
        }
        ki.setGUIHook(this);
        if (guiMap.get("blocksFound") != null)
            blocksFoundInt = Integer.parseInt(guiMap.get("blocksFound"));

        Thread t = new Thread() {

            public void run() {
                while (run) {
                    //System.out.println("Starting loop");
                    isFinal = false;
                    for (Token t : Token.values()) {
                        tokenValueMap.put(t, BigInteger.ZERO);
                        //System.out.println("First for");
                    }
                    List<Output> utxos = ki.getTransMan().getUTXOs(ki.getAddMan().getMainAdd());
                    if (utxos != null) {
                        //System.out.println("second scope");
                        Set<Output> sUtxos = new HashSet<>();
                        sUtxos.addAll(utxos);
                        for (Output o : sUtxos) {
                            //System.out.println("second for");
                            if (tokenValueMap.get(o.getToken()) == null) {
                                tokenValueMap.put(o.getToken(), o.getAmount());
                            } else {
                                tokenValueMap.put(o.getToken(), tokenValueMap.get(o.getToken()).add(o.getAmount()));
                            }
                        }
                    }
                    isFinal = true;
                    try {
                        //System.out.println("sleeping");
                        //TODO cheap and stupid fix
                        System.gc();
                        sleep(1200);
                        //System.out.println("done sleeping");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        t.setName("GUI-Backend");
        t.start();


    }

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
    public JFXTreeTableView transactionTable;
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
    public Label helpText;
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
    private List<Timeline> btnAnimations = new ArrayList<>();
    private List<Timeline> btnAnimationsR = new ArrayList<>();
    private Label ch2dec = new Label(" Chain Height");
    private Label chainHeight2 = new Label(" 26554");
    private Label latency = new Label(" Latency - 125ms");
    private XodusStringMap pmap = new XodusStringMap("security");

    private String metaHash(String data) throws UnsupportedEncodingException {
        //System.out.println("STARTING AUTISM");
        StringBuilder totalhash = new StringBuilder();
        String hash = "";
        for (int i = 0; i < 1024; i++) {
            //System.out.println("AUTISM STEP 1 ROUND: " + i);
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
            //System.out.println("AUTISM STEP 2 ROUND: " + i);
            hash = EncryptionManager.sha512(hash + sr.nextInt() + sr.nextInt());
            totalhash.append(hash);
        }
        return hash;
    }

    private String superAutism(String hash, int numberOfHashes) {
        String superHash = "";
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
            for (Integer key : doneMap.keySet()) {

                if (!doneMap.get(key)) {
                    allDone = false;
                    break;
                }
            }
        }
        for (Integer i : tmap.keySet()) {
            superHash = superHash + hmap.get(i);
        }
        return superHash;
    }

    public void addTransaction(ITrans trans, BigInteger height) {


    }

    long maxH = 0;
    long minH = Long.MAX_VALUE;
    private DecimalFormat format2 = new DecimalFormat("###,###,###,###,###,###,###,###,##0.#########");
    private boolean mining = false;

    @FXML
    void initialize() {
        highSecurity.setSelected(ki.getSetting(Settings.HIGH_SECURITY));
        requirePassword.setSelected(ki.getSetting(Settings.REQUIRE_PASSWORD));
        debugMode.setSelected(ki.getSetting(Settings.DEBUG_MODE));
        if (requirePassword.isSelected())
            lockPane.setVisible(true);

        highSecurity.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ki.setSetting(Settings.HIGH_SECURITY, highSecurity.isSelected());
            }
        });
        requirePassword.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ki.setSetting(Settings.REQUIRE_PASSWORD, highSecurity.isSelected());
            }
        });
        debugMode.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ki.setSetting(Settings.DEBUG_MODE, highSecurity.isSelected());
            }
        });
        /*
        if(!ki.getOptions().poolRelay && !ki.getOptions().pool)
        tokenValueMap.addListener(new MapChangeListener<Token, BigInteger>() {
            @Override
            public void onChanged(Change<? extends Token, ? extends BigInteger> change) {
                ki.debug("values changed");
                while(!isFinal){}
                walletAmount.setText(format2.format((double)tokenValueMap.get(Token.byName(tokenBox.getSelectionModel().getSelectedItem().getText())).longValueExact()/100_000_000));

            }
        });*/
        //hashrateChart.setVerticalZeroLineVisible(false);\
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
        startMining.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (ki.getOptions().mining) {
                    if (!mining) {
                        ki.getMinerMan().startMiners();
                        startMining.setText("Stop Mining");
                        mining = true;
                        hashrateChart.getData().clear();
                        List<XYChart.Series<String, Number>> devs = new ArrayList<>();
                        for (String dev : ki.getMinerMan().getDevNames()) {
                            //ki.debug("Adding device: " + dev + " to graph");
                            XYChart.Series<String, Number> series = new XYChart.Series<>();
                            //hashrateChart.getData();
                            //series.getData().add(new XYChart.Data<String,Number>("0",100));
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
        addressLabel.setText("Address - " + ki.getAddMan().getMainAdd().encodeForChain());
        syncProgress.setProgress(0);
        submitPassword.setBackground(new Background(new BackgroundFill(Color.valueOf("#18BC9C"), CornerRadii.EMPTY, Insets.EMPTY)));
        passwordField.setLabelFloat(true);
        RequiredFieldValidator validator = new RequiredFieldValidator();
        validator.setMessage("Input Required");
        passwordField.getValidators().add(validator);
        passwordField.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) passwordField.validate();
        });
        tokenLabel.setMinWidth(walletAmount.getWidth());
        //tokenLabel.setPrefWidth(walletAmount.getWidth());
        //tokenLabel.setMaxWidth(walletAmount.getWidth());
        Timeline lockAnimation = new Timeline(new KeyFrame(Duration.millis(500), new KeyValue(lockPane.layoutYProperty(), 500, EASE_BOTH)), new KeyFrame(Duration.millis(500), new KeyValue(lockPane.opacityProperty(), 0, EASE_BOTH)));
        submitPassword.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                submitPassword.setDisable(true);
                passwordWaiter.setVisible(true);
                new Thread() {
                    public void run() {
                        if (pmap.get("fr") == null) {
                            String hash = "";


                            hash = superAutism(passwordField.getText() + hash, 64);


                            pmap.put(hash, "p");
                            pmap.put("fr", "fr");
                            Platform.runLater(new Thread() {
                                public void run() {
                                    lockAnimation.play();
                                    submitPassword.setDisable(false);
                                    passwordWaiter.setVisible(false);

                                }
                            });
                        } else

                        {
                            String hash = "";


                            hash = superAutism(passwordField.getText() + hash, 64);

                            String hash2 = hash;
                            Platform.runLater(new Thread() {
                                public void run() {
                                    if (pmap.get(hash2) != null)

                                    {
                                        lockAnimation.play();
                                    } else {

                                        Label l = new Label("Incorrect");
                                        l.setStyle("-fx-text-fill:RED");
                                        l.setLayoutX(passwordField.getLayoutX());
                                        l.setLayoutY(passwordField.getLayoutY() + 60);
                                        lockPane.getChildren().add(l);
                                    }

                                    submitPassword.setDisable(false);

                                    passwordWaiter.setVisible(false);
                                }
                            });
                        }
                    }
                }.start();
            }
        });
        versionLabel.setText("Version - " + Ki.VERSION);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.out.println("Close requested");
                System.exit(0);
            }
        });
        deleteAddress.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                addressList.getItems().remove(addressList.getSelectionModel().getSelectedIndex());
            }
        });

        addressList.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        List<Pane> content = new ArrayList<>();
        content.add(settingsPane);
        content.add(walletPane);
        content.add(miningTab);
        content.add(helpPane);
        content.add(blockExplorerPane);
        content.add(addressPane);
        content.add(blockPane);
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
        menuDrawer.close();
        borderPane.setStyle("-fx-background-color:" + "#252830");
        VBox vb = new VBox();
        vb.setMaxWidth(Double.MAX_VALUE);
        Image img = new Image(getClass().getResourceAsStream("/origin.png"));
        vb.setBackground(new Background(new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
        vb.setFillWidth(true);
        List<String> adds = new ArrayList<>();
        for (Address add : ki.getAddMan().getAll()) {
            if (!adds.contains(add.encodeForChain())) {
                addressList.getItems().add(add.encodeForChain());
                adds.add(add.encodeForChain());
            }
        }
        entropyLabel.setWrapText(true);
        entropyLabel.setMaxWidth(256);
        addressList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                entropyLabel.setText("Entropy of Selection: \n" + ki.getAddMan().getEntropyForAdd(Address.decodeFromChain(addressList.getSelectionModel().getSelectedItem())));
            }
        });
        createAddress.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Address a = ki.getAddMan().createNew(entropyField.getText());
                addressList.getItems().add(a.encodeForChain());
            }
        });
        setSpendAddress.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ki.getAddMan().setMainAdd(Address.decodeFromChain(addressList.getSelectionModel().getSelectedItem()));
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
        vb.getChildren().add(buildMainButton("Wallet", "/Wallet.png", 0, 0, content, walletPane));
        vb.getChildren().add(buildMainButton("Address", "/home.png", 200, 0, content, addressPane));
        //vb.getChildren().add(buildButton("Transactions","/Transactions.png",100));
        vb.getChildren().add(buildMainButton("Mining", "/Mining.png", 400, 1, content, miningTab));
        if (!ki.getOptions().lite)
            vb.getChildren().add(buildMainButton("Blocks", "/Block.png", 600, 0, content, blockExplorerPane));
        vb.getChildren().add(buildMainButton("Settings", "/Settings.png", 800, 0, content, settingsPane));
        vb.getChildren().add(buildMainButton("Help", "/Help.png", 1000, 7, content, helpPane));
        vb.getChildren().add(new Separator());
        if (!ki.getOptions().lite)
            fillMasonry(ki.getChainMan().currentHeight().subtract(BigInteger.valueOf(100)), ki.getChainMan().currentHeight());
        chainHeight2.setFont(Font.loadFont(getClass().getResourceAsStream("/ADAM.CG PRO.otf"), 10));
        ch2dec.setFont(Font.loadFont(getClass().getResourceAsStream("/ADAM.CG PRO.otf"), 10));
        //chainHeight2.setPrefHeight(80);
        ch2dec.setMinWidth(90);
        ch2dec.setTextAlignment(TextAlignment.CENTER);
        vb.getChildren().add(ch2dec);
        chainHeight2.setMinWidth(90);
        chainHeight2.setTextAlignment(TextAlignment.CENTER);
        vb.getChildren().add(chainHeight2);
        latency.setFont(Font.loadFont(getClass().getResourceAsStream("/ADAM.CG PRO.otf"), 10));
        vb.getChildren().add(latency);
        vb.setStyle("-fx-background-color:" + "#18BC9C");
        vb.setBackground(new Background(new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
        menuDrawer.getSidePane().add(vb);
        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
            //System.out.println("Height: " + stage.getHeight() + " Width: " + stage.getWidth());
            hashrateChart.setMinWidth(miningTab.getWidth());
            miningIntesity.setMinWidth(miningTab.getWidth() - 20);
            startMining.setLayoutX((miningTab.getWidth() / 2) - (startMining.getWidth() / 2) - 5);
            miLabel.setLayoutX((miningTab.getWidth() / 2) - (miLabel.getWidth() / 2) - 5);
            addressText.setLayoutX(walletPane.getWidth() - (addressText.getWidth() + 5));
            amountText.setLayoutX(walletPane.getWidth() - (amountText.getWidth() + 5));
            messageText.setLayoutX(walletPane.getWidth() - (messageText.getWidth() + 5));
            feeText.setLayoutX(walletPane.getWidth() - (feeText.getWidth() + 5));
            sendButton.setLayoutX(walletPane.getWidth() - (sendButton.getWidth() + 5));
            walletAmount.setLayoutX(walletPane.getWidth() - ((walletAmount.getWidth() + 15)));
            tokenLabel.setLayoutX(walletAmount.getLayoutX() + 10);
            transactionTable.setMinWidth(walletPane.getWidth() - (sendButton.getWidth() + 25));
            versionLabel.setLayoutX(helpPane.getWidth() / 2 - (versionLabel.getWidth() / 2));
            helpText.setLayoutX(helpPane.getWidth() / 2 - (helpText.getWidth() / 2));
            topPane2.setMinWidth(walletPane.getWidth());
            beScroll.setMinWidth(blockExplorerPane.getWidth());
            beScroll.setMinHeight(blockExplorerPane.getHeight() - 10);
            beScroll.setPrefHeight(blockExplorerPane.getHeight() - 10);
            lockPane.setMinHeight(borderPane.getHeight());

        };

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
            System.out.println("Color changed");
            if (colorCombos.getSelectionModel().getSelectedItem().getText().contains("Primary Color")) {
                System.out.println("Changing primary");
                menuHamburger.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));

                vb.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));

                sendButton.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                //sendButton.setOpacity(255);
                copyAddress.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                startMining.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                //System.out.println("style:" + miningIntesity.getStyle());
                String color = colorPicker.getValue().toString().replace("0x", "");
                color = "#" + color;
                //System.out.println("color: " + color);
                miningIntesity.setStyle("-jfx-default-thumb:" + color);
                createAddress.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                deleteAddress.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                setSpendAddress.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                changePassword.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                backToBE.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                nextBlock.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                previousBlock.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                //miningIntesity.getClip().setStyle("-fx-background-color:"+color);

            } else if (colorCombos.getSelectionModel().getSelectedItem().getText().contains("Secondary Color")) {
                topPane.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                borderPane.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
            } else if (colorCombos.getSelectionModel().getSelectedItem().getText().contains("Text Primary")) {
                for (Node n : vb.getChildren()) {
                    ((JFXButton) n).setTextFill(colorPicker.getValue());
                }
            } else if (colorCombos.getSelectionModel().getSelectedItem().getText().contains("Text Secondary")) {

            }

        };

        //colorPicker.setStyle("-fx-text-fill:BLACK");
        //colorPicker.backgroundProperty().addListener(cpListener);
        //colorPicker.styleProperty().addListener(cpListener);
        colorPicker.valueProperty().addListener(cpListener);
        colorPicker.setValue(Color.valueOf("#18BC9C"));
        menuHamburger.setStyle(vb.getStyle());
        //topPane.setStyle(topPane.getStyle() + vb.getStyle());
        menuDrawer.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (menuDrawer.isHiding()) {
                    burgerTask.setRate(burgerTask.getRate() * -1);
                    burgerTask.play();
                }
            }
        });

        List<XYChart.Series<String, Number>> devs = new ArrayList<>();
        for (String dev : ki.getMinerMan().getDevNames()) {
            //ki.debug("Adding device: " + dev + " to graph");
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            //hashrateChart.getData();
            //series.getData().add(new XYChart.Data<String,Number>("0",100));
            hashrateChart.getData().add(series);
            series.setName(dev);
        }
        /*
        XYChart.Series<String,Number> series = new XYChart.Series<>();
        //hashrateChart.getData();
        series.getData().add(new XYChart.Data<String,Number>("0",100));
        hashrateChart.getData().add(series);
        series.setName("gfx900");

        XYChart.Series<String,Number> series2 = new XYChart.Series<>();

        //hashrateChart.getData();
        series2.setName("1080 TI");
        hashrateChart.getData().add(series2);
        series2.getData().add(new XYChart.Data<String,Number>("0",400));
        XYChart.Series<String,Number> series3 = new XYChart.Series<>();
        //hashrateChart.getData();
        series3.getData().add(new XYChart.Data<String,Number>("0",series.getData().get(0).getYValue().intValue()+series2.getData().get(0).getYValue().intValue()));
        hashrateChart.getData().add(series3);
        series3.setName("Cumulative");
        */
        //hashrateChart.getXAxis().setSide(Side.TOP);
        hashrateChart.getXAxis().setLabel("");
        hashrateChart.setLegendVisible(true);
        hashrateChart.setCreateSymbols(false);
        //hashrateChart.setLegendSide(Side.TOP);
        hashrateChart.getXAxis().setAnimated(false);
        /*
        for(Node n:hashrateChart.getChildrenUnmodifiable())
        {
            if(n instanceof Legend)
            {
                for(Node n2:((Legend)n).getChildren())
                {
                    if(n2 instanceof Label)
                    {
                        System.out.println("found label");
                        ((Label)n2).setStyle("-fx-text-fill:BLACK");
                    }
                }
            }
        }
        */
        sendButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (ki.getEncryptMan().getPublicKey() != null) {

                    Token token = Token.byName(tokenBox.getSelectionModel().getSelectedItem().getText());


                    BigDecimal amt = new BigDecimal(amountText.getText());


                    BigInteger amount = amt.multiply(new BigDecimal("100000000.0")).toBigInteger();
                    int index = 0;
                    Address receiver = Address.decodeFromChain(addressText.getText());
                    Output output = new Output(amount, receiver, token, index, System.currentTimeMillis());
                    java.util.List<Output> outputs = new ArrayList<>();
                    outputs.add(output);
                    java.util.List<String> keys = new ArrayList<>();
                    keys.add(ki.getEncryptMan().getPublicKeyString());
                    java.util.List<Input> inputs = new ArrayList<>();
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

                    BigInteger totalInput = BigInteger.ZERO;
                    for (Address a : ki.getAddMan().getActive()) {
                        if (ki.getTransMan().getUTXOs(a) == null) return;
                        for (Output o : ki.getTransMan().getUTXOs(a)) {
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
                    for (Address a : ki.getAddMan().getActive()) {
                        //get inputs
                        if (feeInput.compareTo(fee) >= 0) break;
                        for (Output o : ki.getTransMan().getUTXOs(a)) {
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

                    Map<String, String> entropyMap = new HashMap<>();

                    for (Input i : inputs) {
                        if (entropyMap.containsKey(i.getAddress().encodeForChain())) continue;
                        entropyMap.put(i.getAddress().encodeForChain(), ki.getAddMan().getEntropyForAdd(i.getAddress()));
                        ki.getMainLog().info("Matching: " + i.getAddress().encodeForChain() + " with " + ki.getAddMan().getEntropyForAdd(i.getAddress()));
                    }


                    ITrans trans = new Transaction(messageText.getText(), 1, null, outputs, inputs, entropyMap, keys, TransactionType.STANDARD);
                    ki.debug("Transaction has: " + trans.getOutputs().size() + " Outputs before finalization");
                    trans.makeChange(fee, ki.getAddMan().getMainAdd()); // TODO this just sends change back to the main address......will need to give option later
                    trans.addSig(ki.getEncryptMan().getPublicKeyString(), ki.getEncryptMan().sign(trans.toSign()));
                    ki.debug("Transaction has: " + trans.getOutputs().size() + "Outputs after finalization");
                    if (ki.getTransMan().verifyTransaction(trans)) {
                        ki.getTransMan().getPending().add(trans);
                        for (Input i : trans.getInputs()) {
                            ki.getTransMan().getUsedUTXOs().add(i.getID());
                        }
                        TransactionPacket tp = new TransactionPacket();
                        tp.trans = trans.toJSON();
                        ki.getNetMan().broadcast(tp);
                        notification("Sent transaction");

                    } else {
                        ki.debug("Transaction did not verify, not sending and not adding to pending list");
                        notification("Transaction failed to send");
                    }
                }
            }
        });
        List<Long> average = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        new Thread() {
            public void run() {

                int i = 0;
                while (true) {
                    i++;
                    //startMining.layout();
                    //System.out.println("width: " + miningTab.getWidth());
                    int i2 = i;
                    if ((i % 10 == 0) && mining)
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

                    Platform.runLater(new Thread() {
                        public void run() {
                            startMining.setLayoutX((miningTab.getWidth() / 2) - (startMining.getWidth() / 2) - 5);
                            hashrateChart.setMinWidth(miningTab.getWidth());
                            miningIntesity.setMinWidth(miningTab.getWidth() - 20);
                            miLabel.setLayoutX((miningTab.getWidth() / 2) - (miLabel.getWidth() / 2) - 5);
                            addressText.setLayoutX(walletPane.getWidth() - (addressText.getWidth() + 5));
                            amountText.setLayoutX(walletPane.getWidth() - (amountText.getWidth() + 5));
                            messageText.setLayoutX(walletPane.getWidth() - (messageText.getWidth() + 5));
                            feeText.setLayoutX(walletPane.getWidth() - (feeText.getWidth() + 5));
                            sendButton.setLayoutX(walletPane.getWidth() - (sendButton.getWidth() + 5));
                            walletAmount.setLayoutX(walletPane.getWidth() - (walletAmount.getWidth() + 15));
                            tokenLabel.setLayoutX(walletAmount.getLayoutX() + 10);
                            transactionTable.setMinWidth(walletPane.getWidth() - (sendButton.getWidth() + 25));
                            versionLabel.setLayoutX(helpPane.getWidth() / 2 - (versionLabel.getWidth() / 2));
                            helpText.setLayoutX(helpPane.getWidth() / 2 - (helpText.getWidth() / 2));
                            topPane2.setMinWidth(walletPane.getWidth());
                            beScroll.setMinWidth(blockExplorerPane.getWidth());
                            beScroll.setMinHeight(blockExplorerPane.getHeight() - 10);
                            beScroll.setPrefHeight(blockExplorerPane.getHeight() - 10);
                            lockPane.setMinHeight(borderPane.getHeight());
                            heightLabel.setText("Chain Height - " + ki.getChainMan().currentHeight());
                            chainHeight2.setText(" " + ki.getChainMan().currentHeight().toString());
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
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        for (Token t : Token.values()) {
            if (t.name().contains("TOKEN")) continue;
            JFXButton button = new JFXButton(t.getName());
            button.setWrapText(true);
            button.setMinHeight(60);
            button.setPrefWidth(1000);
            button.setBackground(new Background(new BackgroundFill(Color.valueOf(getDefaultColor(new Random().nextInt(12))), CornerRadii.EMPTY, Insets.EMPTY)));
            Label l = new Label(t.getName());
            l.setStyle("-fx-text-fill:BLACK");
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
            if (i % 2 == 1)
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

    private void fillMasonry(BigInteger bottomRange, BigInteger topRange) {
        if (topRange.compareTo(ki.getChainMan().currentHeight()) > 0) {
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
        //mp.setLimitColumn(40);
        //mp.setMinSize(0,0);
        //mp.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);

        for (int i = 0; i < i2; i++) {
            Block b = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight().subtract(BigInteger.valueOf(i)));

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
            double height = (b.getTransactionKeys().size() / 10) * 20 + 50;
            sp.setPrefSize(width, height);
            sp.setMinSize(width, height);
            sp.setMaxSize(width, height);
            sp.autosize();
            sp.requestLayout();
            sp.getChildren().add(btn);
            //System.out.println("Brightness of button: " + c.getBrightness() + "   number: " + i);
            //btn.setStyle("-fx-text-fill: " + ((c.getBrightness() > 0.5) ? "Black":"White"));
            btn.setFont(f);
            children.add(sp);
        }

        mp.getChildren().addAll(children);
        JFXScrollPane.smoothScrolling(beScroll);
        Platform.runLater(() ->
                beScroll.requestLayout());
    }

    private void setupBlockPane(BigInteger height) {
        Block b = ki.getChainMan().getByHeight(height);
        blockHeight.setText("Height - " + b.height);
        blockID.setText("ID - " + b.ID);
        solver.setText("Solver - " + b.solver);
        numberOfTransactions.setText("Number of Transactions - " + b.getTransactionKeys().size());
        currentBlock = b.height;
        blockExplorerPane.setVisible(false);
        blockPane.setVisible(true);
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
    private XodusStringMap guiMap = new XodusStringMap("gui.data");

    public void blockFound() {
        if (ki.getChainMan().currentHeight().compareTo(latestBlock) > 0) {
            blocksFoundInt++;
            latestBlock = ki.getChainMan().currentHeight();
            guiMap.put("blocksFound", "" + blocksFoundInt);
            guiMap.put("latest", latestBlock.toString());
        }
        notification("Found a block");
    }

    private void notification(String text) {
        Platform.runLater(new Thread() {
            public void run() {
                JFXSnackbar sb = new JFXSnackbar(topPane2);
                sb.enqueue(new JFXSnackbar.SnackbarEvent(text));
            }
        });
    }
}
