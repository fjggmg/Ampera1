package com.lifeform.main;

import com.lifeform.main.blockchain.IMiner;
import com.lifeform.main.data.JSONManager;
import com.lifeform.main.data.files.StringFileHandler;
import com.lifeform.main.network.TransactionPacket;
import com.lifeform.main.transactions.*;
import gpuminer.JOCL.constants.JOCLConstants;
import gpuminer.JOCL.context.JOCLContextAndCommandQueue;
import gpuminer.JOCL.context.JOCLDevices;
import gpuminer.miner.context.ContextMaster;
import gpuminer.miner.context.DeviceContext;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Copyright (C) Ampex Technologies LLC.
 *
 * All rights reserved. The code in this file and all linked art assets (found in the resources folder) involving the GUI
 * are not covered under the GPL license like the rest of the code. Permission must be
 * obtained in written form to duplicate, use, copy, or otherwise make use of the code
 * or files pertaining to the GUI from Ampex Technologies LLC or an active manager of
 * Ampex Technologies LLC. Distributing binaries of this code is permitted under a
 * no-derivative basis. This means you may distribute unmodified copies of Origin,
 * but you may not distribute modified copies that include the GUI code or files
 * mentioned above. If you intend to modify Origin or use it in your own program,
 * Omit the GUI code and you will be in compliance with the license.
 *
 *
 */
public class FXMLController {

    public static Stage primaryStage;
    public static Application app;
    @FXML
    public ListView<String> disabledDevList;
    @FXML
    public ListView<String> enabledDevList;
    @FXML
    public Pane addGenPanel;
    @FXML
    public Pane addManagePanel;
    @FXML
    public Label startMiningLabel;
    private volatile int blocksFoundInt = 0;
    private IKi ki;
    private StringFileHandler guiData;
    private Map<String, String> guiMap = new HashMap<>();
    private volatile List<ITrans> transactions = new ArrayList<>();
    public FXMLController()
    {
        ki = Ki.getInstance();
        ki.setGUIHook(this);
        guiData = new StringFileHandler(ki, "gui.data");
        if (guiData.getLine(0) != null && !guiData.getLine(0).isEmpty()) {
            guiMap = JSONManager.parseJSONtoMap(guiData.getLine(0));
            if (guiMap != null) {
                if (guiMap.get("blocksFound") != null)
                    blocksFoundInt = Integer.parseInt(guiMap.get("blocksFound"));
                if (guiMap.get("transactions") != null) {
                    List<String> transactions = JSONManager.parseJSONToList(guiMap.get("transactions"));
                    if (transactions != null)
                        for (String trans : transactions) {
                            this.transactions.add(Transaction.fromJSON(trans));
                        }
                }
                if (guiMap.get("heightMap") != null) {
                    heightMap = JSONManager.parseJSONtoMap(guiMap.get("heightMap"));
                }
            }
        }

        Task task = new Task<Void>() {
            @Override
            public Void call() {
                while(run) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            tick();
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }

        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.setName("JavaFX-Backend");
        thread.start();



        Thread t = new Thread() {

            public void run() {
                java.util.List<String> checked = new ArrayList<>();
                while (run) {
                    isFinal = false;
                    tokenValueMap.clear();
                    checked.clear();
                    for (Address a : ki.getAddMan().getActive()) {
                        if (checked.contains(a.encodeForChain())) continue;
                        //if (!ki.getTransMan().utxosChanged(a)) continue;
                        checked.add(a.encodeForChain());
                        //ki.getMainLog().info("Getting info from Address: " + a.encodeForChain());
                        List<Output> utxos = ki.getTransMan().getUTXOs(a);
                        if (utxos != null) {
                            for (Output o : utxos) {
                                if (tokenValueMap.get(o.getToken()) == null) {
                                    tokenValueMap.put(o.getToken(), o.getAmount());
                                } else {
                                    tokenValueMap.put(o.getToken(), tokenValueMap.get(o.getToken()).add(o.getAmount()));
                                }
                            }
                        }

                    }
                    isFinal = true;
                    try {
                        //TODO cheap and stupid fix
                        System.gc();
                        Thread.sleep(1200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.setName("GUI-Backend");
        t.start();

    }

    public void blockFound() {
        blocksFoundInt++;
        guiMap.put("blocksFound", "" + blocksFoundInt);
        guiData.replaceLine(0, JSONManager.parseMapToJSON(guiMap).toJSONString());
        guiData.save();
    }

    //TODO why are these public?
    @FXML
    public Label blocksFound;
    @FXML
    public Label rTrans1;

    @FXML
    public Label rTrans2;

    @FXML
    public Label rTrans3;

    @FXML
    public Label rTrans4;

    @FXML
    public Label rTrans5;

    @FXML
    public Label rTrans1a;

    @FXML
    public Label rTrans2a;

    @FXML
    public Label rTrans3a;

    @FXML
    public Label rTrans4a;

    @FXML
    public Label rTrans5a;

    @FXML
    public Label rTrans1m;

    @FXML
    public Label rTrans2m;

    @FXML
    public Label rTrans3m;

    @FXML
    public Label rTrans4m;

    @FXML
    public Label rTrans5m;

    @FXML
    public Label heightLabel;
    @FXML
    private Pane transactionOrigin;

    @FXML
    private Pane transactionKi;

    @FXML
    private Pane transactionGold;

    @FXML
    private Pane transactionSilver;

    @FXML
    private Pane transactionPlatinum;

    @FXML
    private Pane transactionPalladium;

    @FXML
    private Pane transactionUSD;

    @FXML
    private Pane transactionEUR;

    @FXML
    private Pane transactionGBP;

    @FXML
    private Pane transactionJPY;

    @FXML
    private Pane transactionCNY;

    @FXML
    private Label addressLabel;
    @FXML
    private Pane copyPanel;
    @FXML
    public ToolBar walletVbox;

    @FXML
    private Pane sendPane;
    @FXML
    private Label amountLabel;
    @FXML
    private Label tokenLabel;

    @FXML
    private Label versionLabel;

    @FXML
    private CheckBox debugButton;

    @FXML
    private ListView<String> tokenList;
    @FXML
    private CheckBox useCPUcheck;
    @FXML
    private CheckBox useGPUcheck;

    @FXML
    private Pane walletPane;
    @FXML
    private Pane transactionPane;
    @FXML
    private Pane miningPane;
    @FXML
    private Pane settingsPane;
    @FXML
    private Pane helpPane;

    @FXML
    private Pane walletContentPane;
    @FXML
    private Pane transactionContentPane;
    @FXML
    private Pane miningContentPane;
    @FXML
    private Pane settingsContentPane;
    @FXML
    private Pane helpContentPane;

    @FXML
    private Pane startMiningPane;

    @FXML
    private Pane walletOrigin;

    @FXML
    private Pane walletKi;

    @FXML
    private Pane walletGold;

    @FXML
    private Pane walletSilver;

    @FXML
    private Pane walletPlatinum;

    @FXML
    private Pane walletPalladium;

    @FXML
    private Pane walletUSD;

    @FXML
    private Pane walletEUR;

    @FXML
    private Pane walletGBP;

    @FXML
    private Pane walletJPY;

    @FXML
    private Pane walletCNY;

    @FXML
    private TextField addressToSend;

    @FXML
    private TextField amountToSend;

    @FXML
    private TextField feeToSend;

    @FXML
    private TextField messageToSend;

    @FXML
    private Pane exportPane;

    @FXML
    private Label cHashrate;

    @FXML
    private Label minHashrate;
    @FXML
    private Label maxHashrate;
    @FXML
    private Label aHashrate;

    private long minimumHash = Long.MAX_VALUE;
    private long maximumHash = 0;
    private List<Long> last25Hash = new ArrayList<>();
    private boolean run = true;
    private boolean versionSet = false;

    private Token currentWallet = Token.ORIGIN;
    private Token currentTransaction = Token.ORIGIN;
    private ConcurrentMap<Token,BigInteger> tokenValueMap = new ConcurrentHashMap<>();
    private DecimalFormat format = new DecimalFormat("###,###,###,###,###,###,##0.0#######");
    private volatile boolean isFinal = false;
    private ObservableList<String> enabledDevices = FXCollections.observableArrayList();
    private ObservableList<String> disabledDevices = FXCollections.observableArrayList();
    private Map<String, String> heightMap = new HashMap<>();

    public void addEnabledDevice(String dev) {
        enabledDevices.add(dev);
    }

    public void addDisabledDevice(String dev) {
        disabledDevices.add(dev);
    }

    private DecimalFormat format2 = new DecimalFormat("###,###,###,###,###,###,###,###,##0.#########");
    private boolean firstDevTick = true;
    public void tick()
    {
            if(versionLabel != null)
            {
                if(!versionSet)
                {
                    versionLabel.setText(versionLabel.getText() + " " + Ki.VERSION);
                    //amountLabel.setText("1,000.00");
                    //amountLabel.textProperty().bind(calculatedAmount);
                    versionSet = true;
                    if(startMiningPane != null)
                    {

                        if(!ki.getOptions().mining) {
                            startMiningPane.setOpacity(0.01);
                            miningPane.setOpacity(0.1);
                        }

                    }
                }
                if(addressLabel != null)
                {
                    addressLabel.setText("Address - " + ki.getAddMan().getMainAdd().encodeForChain());
                }
                if(debugButton.isSelected() != Ki.debug)
                {
                    Ki.debug = debugButton.isSelected();
                }

                if (ki.getEncryptMan().getPublicKey() != null && isFinal) {

                    if(tokenValueMap.get(currentWallet) == null || tokenValueMap.get(currentWallet).compareTo(BigInteger.ZERO) == 0)
                    {
                        amountLabel.setText("0");
                    }else {
                        amountLabel.setText(format.format(tokenValueMap.get(currentWallet).longValueExact() / 100000000D));
                    }

                }
                heightLabel.setText("Height: " + ki.getChainMan().currentHeight());
            }
            if(tokenLabel != null)
            {
                tokenLabel.setText(currentWallet.name());
            }

        if (enabledDevList != null && disabledDevList != null && enabledDevices != null && ki.getMinerMan().isSetup()) {
            if (firstDevTick) {
                firstDevTick = false;
                enabledDevList.setItems(enabledDevices);
                disabledDevList.setItems(disabledDevices);


                for (String dev : ki.getMinerMan().getDevNames()) {
                    //TODO probably pointless check as long as we're only running this once
                    if (!enabledDevices.contains(dev)) {
                        enabledDevices.add(dev);
                    }
                }
                JOCLContextAndCommandQueue.setWorkaround(false);
                //JOCLDevices.setDeviceFilter(JOCLConstants.ALL_DEVICES);

                ContextMaster jm = new ContextMaster();
                for (DeviceContext dev : jm.getContexts()) {
                    if (!enabledDevices.contains(dev.getDInfo().getDeviceName())) {
                        disabledDevices.add(dev.getDInfo().getDeviceName());
                    }
                }

                enabledDevList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getClickCount() == 2) {
                            ki.getMinerMan().disableDev(enabledDevList.getSelectionModel().getSelectedItem());
                            disabledDevices.add(enabledDevList.getSelectionModel().getSelectedItem());
                            enabledDevices.remove(enabledDevList.getSelectionModel().getSelectedItem());

                        }
                    }
                });
                disabledDevList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getClickCount() == 2) {
                            ki.getMinerMan().enableDev(disabledDevList.getSelectionModel().getSelectedItem());
                            enabledDevices.add(disabledDevList.getSelectionModel().getSelectedItem());
                            disabledDevices.remove(disabledDevList.getSelectionModel().getSelectedItem());

                        }
                    }
                });
            }
            if (cHashrate != null && minHashrate != null && maxHashrate != null && aHashrate != null) {

                long cumulativeHash = ki.getMinerMan().cumulativeHashrate();
                cHashrate.setText("Current Hashrate - " + format2.format(cumulativeHash) + " hashes/second");
                if (cumulativeHash < minimumHash || minimumHash == 0) {
                    minimumHash = ki.getMinerMan().cumulativeHashrate();
                }
                minHashrate.setText("Min - " + format2.format(minimumHash));
                if (cumulativeHash > maximumHash) {
                    maximumHash = ki.getMinerMan().cumulativeHashrate();
                }
                maxHashrate.setText("Max - " + format2.format(maximumHash));
                if (last25Hash.size() >= 25) {
                    last25Hash.remove(0);
                }
                if (cumulativeHash != 0) {
                    last25Hash.add(cumulativeHash);
                }
                long total = 0;
                if (last25Hash.size() != 0) {
                    for (long l : last25Hash) {
                        total += l;
                    }


                    total = total / last25Hash.size();
                }
                aHashrate.setText("Average - " + format2.format(total));


            }
            if (rTrans1 != null) {
                int i = 1;
                if (!transactions.isEmpty()) {
                    ITrans trans = transactions.get(transactions.size() - i);
                    String transInfo = getTransInfo(trans);
                    String[] split = transInfo.split("\n");
                    rTrans1.setText(split[0]);
                    rTrans1a.setText(split[1]);
                    rTrans1m.setText(split[2]);
                    i++;
                    if (transactions.size() >= i) {
                        trans = transactions.get(transactions.size() - i);
                        transInfo = getTransInfo(trans);
                        split = transInfo.split("\n");
                        rTrans2.setText(split[0]);
                        rTrans2a.setText(split[1]);
                        rTrans2m.setText(split[2]);
                    }
                    i++;
                    if (transactions.size() >= i) {
                        trans = transactions.get(transactions.size() - i);
                        transInfo = getTransInfo(trans);
                        split = transInfo.split("\n");
                        rTrans3.setText(split[0]);
                        rTrans3a.setText(split[1]);
                        rTrans3m.setText(split[2]);
                    }
                    i++;
                    if (transactions.size() >= i) {
                        trans = transactions.get(transactions.size() - i);
                        transInfo = getTransInfo(trans);
                        split = transInfo.split("\n");
                        rTrans4.setText(split[0]);
                        rTrans4a.setText(split[1]);
                        rTrans4m.setText(split[2]);
                    }
                    i++;
                    if (transactions.size() >= i) {
                        trans = transactions.get(transactions.size() - i);
                        transInfo = getTransInfo(trans);
                        split = transInfo.split("\n");
                        rTrans5.setText(split[0]);
                        rTrans5a.setText(split[1]);
                        rTrans5m.setText(split[2]);
                    }
                }


            }

        }
        if (blocksFound != null) {
            blocksFound.setText("Blocks Found - " + blocksFoundInt);
        }
    }

    public void addTransaction(ITrans trans, BigInteger height) {

        heightMap.put(trans.getID(), height.toString());
        guiMap.put("heightMap", JSONManager.parseMapToJSON(heightMap).toJSONString());
        guiData.save();
        for (ITrans t : transactions) {
            //in case of collision and our chain dying we check to make sure
            //we're not adding a second time, the other issue we may need to consider is
            //removing ones from the list if they fall off the current chain
            //although this should be rare enough that we will wait until a future date to accomplish this
            if (t.getID().equals(trans.getID())) {
                return;
            }
        }
        transactions.add(trans);
        List<String> sTrans = new ArrayList<>();
        for (ITrans t : transactions) {
            sTrans.add(t.toJSON());
        }
        guiMap.put("transactions", JSONManager.parseListToJSON(sTrans).toJSONString());
        guiData.replaceLine(0, JSONManager.parseMapToJSON(guiMap).toJSONString());
        guiData.save();

    }

    private String getTransInfo(ITrans trans) {

        boolean out = false;
        BigInteger amount = BigInteger.ZERO;
        for (Output o : trans.getOutputs()) {
            for (Address a : ki.getAddMan().getActive()) {

                if (o.getAddress().encodeForChain().equals(a.encodeForChain())) {
                    amount = amount.add(o.getAmount());
                    //ki.debug("Output from trans: " + trans.getID() + " is up to: " + amount.toString() + " from output: " + o.getID());
                }
            }
        }

        for (Input input : trans.getInputs()) {
            for (Address a : ki.getAddMan().getActive()) {
                if (input.getAddress().encodeForChain().equals(a.encodeForChain())) {
                    out = true;
                    amount = amount.subtract(input.getAmount());
                    amount = amount.add(trans.getFee());
                }
            }
        }
        return ((out) ? "Sent" : "Received") + "\n" + format.format(Math.abs(amount.longValueExact() / 100_000_000D)) + "\n" + " " + trans.getMessage();
    }

    @FXML
    public void topDragged(MouseEvent event) {
        primaryStage.setX(event.getScreenX() + xOffset);
        primaryStage.setY(event.getScreenY() + yOffset);

    }

    private double xOffset = 0;
    private double yOffset = 0;
    @FXML
    public void topPressed(MouseEvent event) {
        xOffset = primaryStage.getX() - event.getScreenX();
        yOffset = primaryStage.getY() - event.getScreenY();
    }

    public void walletHover(MouseEvent mouseEvent) {
        paneHover(walletPane);
    }

    public void walletHoverOff(MouseEvent mouseEvent) {
        paneHoverOff(walletPane);
    }

    public void walletClicked(MouseEvent mouseEvent) {
        paneClicked(walletPane, walletContentPane);

    }



    private void paneHover(Pane p)
    {
        p.setOpacity(p.getOpacity() + 0.15);
    }

    private void paneHoverOff(Pane p)
    {
        p.setOpacity(p.getOpacity() - 0.15);
    }
    private Pane currentPane;
    private Pane currentClicked;
    private void paneClicked(Pane p, Pane cP)
    {
        if(currentClicked != null)
        {
            if(currentClicked.getId().equals(p.getId())) return;
            currentClicked.setOpacity(currentClicked.getOpacity() + 0.05);
        }
        currentClicked = p;
        p.setOpacity(p.getOpacity() - 0.05);
        cP.setVisible(true);
        if(currentPane != null) currentPane.setVisible(false);
        currentPane = cP;
    }

    private Pane currentSubClicked;
    private void subPaneClicked(Pane p)
    {
        if(currentSubClicked != null)
        {
            if(currentSubClicked.getId().equals(p.getId())) return;
            currentSubClicked.setOpacity(currentSubClicked.getOpacity() + 0.1);
        }
        currentSubClicked = p;
        p.setOpacity(p.getOpacity() - 0.1);

    }

    private void nonPrimaryPaneClicked(Pane p)
    {
        p.setOpacity(p.getOpacity() - 0.05);
    }

    private void nonPrimaryPaneUnclicked(Pane p)
    {
        p.setOpacity(p.getOpacity() + 0.05);
    }

    public void transactionHover(MouseEvent mouseEvent) {
        paneHover(transactionPane);
    }

    public void transactionHoverOff(MouseEvent mouseEvent) {
        paneHoverOff(transactionPane);
    }

    public void transactionClicked(MouseEvent mouseEvent) {
        paneClicked(transactionPane,transactionContentPane);

    }


    public void miningHover(MouseEvent mouseEvent) {
        if(ki.getOptions().mining)
        paneHover(miningPane);
    }

    public void miningHoverOff(MouseEvent mouseEvent) {
        if(ki.getOptions().mining)
        paneHoverOff(miningPane);
    }

    public void miningClicked(MouseEvent mouseEvent) {
        if(ki.getOptions().mining)
        paneClicked(miningPane,miningContentPane);
    }

    public void settingsHover(MouseEvent mouseEvent) {
        paneHover(settingsPane);
    }

    public void settingsHoverOff(MouseEvent mouseEvent) {
        paneHoverOff(settingsPane);
    }

    public void settingsClicked(MouseEvent mouseEvent) {
        paneClicked(settingsPane,settingsContentPane);
    }

    public void helpHover(MouseEvent mouseEvent) {
        paneHover(helpPane);
    }

    public void helpHoverOff(MouseEvent mouseEvent) {
        paneHoverOff(helpPane);
    }

    public void helpClicked(MouseEvent mouseEvent) {
        paneClicked(helpPane,helpContentPane);
    }

    public void startMiningHover(MouseEvent mouseEvent) {
        if(ki.getOptions().mining)
        paneHover(startMiningPane);
    }

    public void startMiningHoverOff(MouseEvent mouseEvent) {
        if(ki.getOptions().mining)
        paneHoverOff(startMiningPane);
    }

    private boolean mining = false;
    public void startMiningClicked(MouseEvent mouseEvent) {
        if(ki.getOptions().mining) {
            nonPrimaryPaneClicked(startMiningPane);
            if (!mining) {
                ki.getMinerMan().startMiners();
                startMiningLabel.setText("Stop Mining");
                mining = true;
            } else {
                ki.getMinerMan().stopMiners();
                startMiningLabel.setText("Start Mining");
                mining = false;
            }

        }
    }


    public void startMiningUnclicked(MouseEvent mouseEvent) {
        if(ki.getOptions().mining)
        nonPrimaryPaneUnclicked(startMiningPane);
    }

    public void sendHover(MouseEvent mouseEvent) {
        paneHover(sendPane);
    }

    public void sendHoverOff(MouseEvent mouseEvent) {
        paneHoverOff(sendPane);
    }

    public void sendClicked(MouseEvent mouseEvent) {
        nonPrimaryPaneClicked(sendPane);
        if (ki.getEncryptMan().getPublicKey() != null) {

            Token token = currentTransaction;
            double amt = Double.parseDouble(amountToSend.getText());
            long lAmt = (long) (amt * 100000000D);
            BigInteger amount = BigInteger.valueOf(lAmt);
            int index = 0;
            Address receiver = Address.decodeFromChain(addressToSend.getText());
            Output output = new Output(amount, receiver, token, index,System.currentTimeMillis());
            java.util.List<Output> outputs = new ArrayList<>();
            outputs.add(output);
            java.util.List<String> keys = new ArrayList<>();
            keys.add(ki.getEncryptMan().getPublicKeyString());
            java.util.List<Input> inputs = new ArrayList<>();
            BigInteger fee;
            if(feeToSend.getText() == null || feeToSend.getText().isEmpty())
            {
                fee = BigInteger.ZERO;
            }else {
                double dFee = Double.parseDouble(feeToSend.getText());
                long lFee = (long) (dFee * 100000000D);
                fee = BigInteger.valueOf(lFee);
            }
            ki.getMainLog().info("Fee is: " + fee.toString());

            BigInteger totalInput = BigInteger.ZERO;
            for (Address a : ki.getAddMan().getActive()) {
                if (ki.getTransMan().getUTXOs(a) == null) return;
                for (Output o : ki.getTransMan().getUTXOs(a)) {
                    if (o.getToken().equals(token)) {
                        if(inputs.contains(Input.fromOutput(o))) continue;
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


            ITrans trans = new Transaction(messageToSend.getText(), 1, null, outputs, inputs, entropyMap, keys);
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
            } else {
                ki.debug("Transaction did not verify, not sending and not adding to pending list");
            }
        }
    }

    public void sendUnclicked(MouseEvent mouseEvent) {
        nonPrimaryPaneUnclicked(sendPane);
    }

    public void issuesPageLinkClicked(ActionEvent actionEvent) {
        app.getHostServices().showDocument("https://bitbucket.org/backspace119/ki-project-origin/issues?status=new&status=open");
    }

    public void closeClicked(MouseEvent mouseEvent) {

        System.exit(0);
    }

    @FXML
    void walletCNYClicked(MouseEvent event) {
        subPaneClicked(walletCNY);
        currentWallet = Token.CNY;
    }

    @FXML
    void walletCNYHover(MouseEvent event) {
        paneHover(walletCNY);
    }

    @FXML
    void walletCNYHoverOff(MouseEvent event) {
        paneHoverOff(walletCNY);
    }

    @FXML
    void walletEURClicked(MouseEvent event) {
        subPaneClicked(walletEUR);
        currentWallet = Token.EUR;
    }

    @FXML
    void walletEURHover(MouseEvent event) {
        paneHover(walletEUR);
    }

    @FXML
    void walletEURHoverOff(MouseEvent event) {
        paneHoverOff(walletEUR);
    }

    @FXML
    void walletGBPClicked(MouseEvent event) {
        subPaneClicked(walletGBP);
        currentWallet = Token.GBP;
    }

    @FXML
    void walletGBPHover(MouseEvent event) {
        paneHover(walletGBP);
    }

    @FXML
    void walletGBPHoverOff(MouseEvent event) {
        paneHoverOff(walletGBP);
    }

    @FXML
    void walletGoldClicked(MouseEvent event) {
        subPaneClicked(walletGold);
        currentWallet = Token.GOLD;
    }

    @FXML
    void walletGoldHover(MouseEvent event) {
        paneHover(walletGold);

    }

    @FXML
    void walletGoldHoverOff(MouseEvent event) {
        paneHoverOff(walletGold);
    }

    @FXML
    void walletJPYClicked(MouseEvent event) {
        subPaneClicked(walletJPY);
        currentWallet = Token.JPY;
    }

    @FXML
    void walletJPYHover(MouseEvent event) {
        paneHover(walletJPY);
    }

    @FXML
    void walletJPYHoverOff(MouseEvent event) {
        paneHoverOff(walletJPY);
    }

    @FXML
    void walletKiClicked(MouseEvent event) {
        subPaneClicked(walletKi);
        currentWallet = Token.KI;
    }

    @FXML
    void walletKiHover(MouseEvent event) {
        paneHover(walletKi);
    }

    @FXML
    void walletKiHoverOff(MouseEvent event) {
        paneHoverOff(walletKi);
    }

    @FXML
    void walletOriginClicked(MouseEvent event) {
        subPaneClicked(walletOrigin);
        currentWallet = Token.ORIGIN;
    }

    @FXML
    void walletOriginHover(MouseEvent event) {
        paneHover(walletOrigin);
    }

    @FXML
    void walletOriginHoverOff(MouseEvent event) {
        paneHoverOff(walletOrigin);
    }

    @FXML
    void walletPalladiumClicked(MouseEvent event) {
        subPaneClicked(walletPalladium);
        currentWallet = Token.PALADIUM;
    }

    @FXML
    void walletPalladiumHover(MouseEvent event) {
        paneHover(walletPalladium);
    }

    @FXML
    void walletPalladiumHoverOff(MouseEvent event) {
        paneHoverOff(walletPalladium);
    }

    @FXML
    void walletPlatinumClicked(MouseEvent event) {
        subPaneClicked(walletPlatinum);
        currentWallet = Token.PLATINUM;
    }

    @FXML
    void walletPlatinumHover(MouseEvent event) {
        paneHover(walletPlatinum);
    }

    @FXML
    void walletPlatinumHoverOff(MouseEvent event) {
        paneHoverOff(walletPlatinum);
    }

    @FXML
    void walletSilverClicked(MouseEvent event) {
        subPaneClicked(walletSilver);
        currentWallet = Token.SILVER;
    }

    @FXML
    void walletSilverHover(MouseEvent event) {
        paneHover(walletSilver);
    }

    @FXML
    void walletSilverHoverOff(MouseEvent event) {
        paneHoverOff(walletSilver);
    }

    @FXML
    void walletUSDClicked(MouseEvent event) {
        subPaneClicked(walletUSD);
        currentWallet = Token.USD;
    }

    @FXML
    void walletUSDHover(MouseEvent event) {
        paneHover(walletUSD);
    }

    @FXML
    void walletUSDHoverOff(MouseEvent event) {
        paneHoverOff(walletUSD);
    }

    public void copyHover(MouseEvent mouseEvent) {
        paneHover(copyPanel);
    }

    public void copyHoverOff(MouseEvent mouseEvent) {
        paneHoverOff(copyPanel);
    }

    public void copyClicked(MouseEvent mouseEvent) {
        nonPrimaryPaneClicked(copyPanel);
        StringSelection stringSelection = new StringSelection(ki.getAddMan().getMainAdd().encodeForChain());
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
    }

    public void copyUnclicked(MouseEvent mouseEvent) {
        nonPrimaryPaneUnclicked(copyPanel);
    }

    @FXML
    void transactionCNYClicked(MouseEvent event) {
        subPaneClicked(transactionCNY);
        currentTransaction = Token.CNY;
    }

    @FXML
    void transactionCNYHover(MouseEvent event) {
        paneHover(transactionCNY);
    }

    @FXML
    void transactionCNYHoverOff(MouseEvent event) {
        paneHoverOff(transactionCNY);
    }

    @FXML
    void transactionEURClicked(MouseEvent event) {
        subPaneClicked(transactionEUR);
        currentTransaction = Token.EUR;
    }

    @FXML
    void transactionEURHover(MouseEvent event) {
        paneHover(transactionEUR);
    }

    @FXML
    void transactionEURHoverOff(MouseEvent event) {
        paneHoverOff(transactionEUR);
    }

    @FXML
    void transactionGBPClicked(MouseEvent event) {
        subPaneClicked(transactionGBP);
        currentTransaction = Token.GBP;
    }

    @FXML
    void transactionGBPHover(MouseEvent event) {
        paneHover(transactionGBP);
    }

    @FXML
    void transactionGBPHoverOff(MouseEvent event) {
        paneHoverOff(transactionGBP);
    }

    @FXML
    void transactionGoldClicked(MouseEvent event) {
        subPaneClicked(transactionGold);
        currentTransaction = Token.GOLD;
    }

    @FXML
    void transactionGoldHover(MouseEvent event) {
        paneHover(transactionGold);

    }

    @FXML
    void transactionGoldHoverOff(MouseEvent event) {
        paneHoverOff(transactionGold);
    }

    @FXML
    void transactionJPYClicked(MouseEvent event) {
        subPaneClicked(transactionJPY);
        currentTransaction = Token.JPY;
    }

    @FXML
    void transactionJPYHover(MouseEvent event) {
        paneHover(transactionJPY);
    }

    @FXML
    void transactionJPYHoverOff(MouseEvent event) {
        paneHoverOff(transactionJPY);
    }

    @FXML
    void transactionKiClicked(MouseEvent event) {
        subPaneClicked(transactionKi);
        currentTransaction = Token.KI;
    }

    @FXML
    void transactionKiHover(MouseEvent event) {
        paneHover(transactionKi);
    }

    @FXML
    void transactionKiHoverOff(MouseEvent event) {
        paneHoverOff(transactionKi);
    }

    @FXML
    void transactionOriginClicked(MouseEvent event) {
        subPaneClicked(transactionOrigin);
        currentTransaction = Token.ORIGIN;
    }

    @FXML
    void transactionOriginHover(MouseEvent event) {
        paneHover(transactionOrigin);
    }

    @FXML
    void transactionOriginHoverOff(MouseEvent event) {
        paneHoverOff(transactionOrigin);
    }

    @FXML
    void transactionPalladiumClicked(MouseEvent event) {
        subPaneClicked(transactionPalladium);
        currentTransaction = Token.PALADIUM;
    }

    @FXML
    void transactionPalladiumHover(MouseEvent event) {
        paneHover(transactionPalladium);
    }

    @FXML
    void transactionPalladiumHoverOff(MouseEvent event) {
        paneHoverOff(transactionPalladium);
    }

    @FXML
    void transactionPlatinumClicked(MouseEvent event) {
        subPaneClicked(transactionPlatinum);
        currentTransaction = Token.PLATINUM;
    }

    @FXML
    void transactionPlatinumHover(MouseEvent event) {
        paneHover(transactionPlatinum);
    }

    @FXML
    void transactionPlatinumHoverOff(MouseEvent event) {
        paneHoverOff(transactionPlatinum);
    }

    @FXML
    void transactionSilverClicked(MouseEvent event) {
        subPaneClicked(transactionSilver);
        currentTransaction = Token.SILVER;
    }

    @FXML
    void transactionSilverHover(MouseEvent event) {
        paneHover(transactionSilver);
    }

    @FXML
    void transactionSilverHoverOff(MouseEvent event) {
        paneHoverOff(transactionSilver);
    }

    @FXML
    void transactionUSDClicked(MouseEvent event) {
        subPaneClicked(transactionUSD);
        currentTransaction = Token.USD;
    }

    @FXML
    void transactionUSDHover(MouseEvent event) {
        paneHover(transactionUSD);
    }

    @FXML
    void transactionUSDHoverOff(MouseEvent event) {
        paneHoverOff(transactionUSD);
    }

    public void minimizeClicked(MouseEvent mouseEvent) {
        primaryStage.setIconified(true);
    }

    public void addGenHover(MouseEvent mouseEvent) {
        paneHover(addGenPanel);
    }

    public void addGenHoverOff(MouseEvent mouseEvent) {
        paneHoverOff(addGenPanel);
    }

    public void addGenClicked(MouseEvent mouseEvent) {
    }

    public void addGenUnclicked(MouseEvent mouseEvent) {
    }

    public void addManHover(MouseEvent mouseEvent) {
        paneHover(addManagePanel);
    }

    public void addManHoverOff(MouseEvent mouseEvent) {
        paneHoverOff(addManagePanel);
    }

    public void addManClicked(MouseEvent mouseEvent) {
    }

    public void addManUnclicked(MouseEvent mouseEvent) {
    }

    public void exportClicked(MouseEvent mouseEvent) {
        StringFileHandler transFile = new StringFileHandler(ki, "transactions.xls");


        if (transFile.getLine(0) != null)
            if (!transFile.getLine(0).isEmpty()) {
                if (!transFile.delete()) {
                    ki.getMainLog().info("File could not be deleted, please close the transactions.xls file and retry exporting");
                    return;
                }
                transFile = new StringFileHandler(ki, "transactions.xls");
            }
        transFile.replaceLine(0, "Transaction \t timestamp \t message \t confirmations \t fee \n");
        for (ITrans trans : transactions) {
            StringBuilder t;
            t = new StringBuilder(trans.getID() + "\t" + new Date(ki.getChainMan().getByHeight(new BigInteger(heightMap.get(trans.getID()))).timestamp).toString() + "\t" + trans.getMessage() + "\t" + ki.getChainMan().currentHeight().subtract(new BigInteger(heightMap.get(trans.getID()))).toString() + "\t" + format.format(trans.getFee().longValueExact() / 100_000_000L) +
                    "\n" + "\t" + "output" + "\t" + "amount" + "\t" + "address");
            for (Output o : trans.getOutputs()) {
                t.append("\n" + "\t").append(o.getID()).append("\t").append(format.format(o.getAmount().longValueExact() / 100_000_000D)).append("\t").append(o.getAddress().encodeForChain());
            }
            t.append("\n" + "\t" + "input" + "\t" + "amount" + "\t" + "address");
            for (Input i : trans.getInputs()) {
                t.append("\n" + "\t").append(i.getID()).append("\t").append(format.format(i.getAmount().longValueExact() / 100_000_000D)).append("\t").append(i.getAddress().encodeForChain());
            }
            transFile.addLine(t.toString());
        }
        transFile.save();
        ki.getMainLog().info("exported transactions to transactions.xls");

    }

    public void exportHovered(MouseEvent mouseEvent) {
        paneHover(exportPane);
    }

    public void exportHoveredOff(MouseEvent mouseEvent) {
        paneHoverOff(exportPane);
    }
}
