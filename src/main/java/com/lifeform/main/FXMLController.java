package com.lifeform.main;

import com.lifeform.main.blockchain.CPUMiner;
import com.lifeform.main.blockchain.IMiner;
import com.lifeform.main.network.TransactionPacket;
import com.lifeform.main.transactions.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Copyright (C) Bryan Sharpe
 *
 * All rights reserved.
 *
 *
 */
public class FXMLController {

    public static Stage primaryStage;
    public static Application app;
    @FXML
    public Slider coresSlider;


    private IKi ki;
    public FXMLController()
    {
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

        ki = Ki.getInstance();

        Thread t = new Thread() {

            public void run() {
                java.util.List<Address> checked = new ArrayList<>();
                while (run) {
                    isFinal = false;
                    tokenValueMap.clear();
                    checked.clear();
                    for (Address a : ki.getAddMan().getActive()) {
                        if (checked.contains(a)) continue;
                        if (!ki.getTransMan().utxosChanged(a)) continue;
                        checked.add(a);
                        //ki.getMainLog().info("Getting info from Address: " + a.encodeForChain());
                        if (ki.getTransMan().getUTXOs(a) != null) {
                            for (Output o : ki.getTransMan().getUTXOs(a)) {

                                if (tokenValueMap.get(o.getToken()) == null) {
                                    tokenValueMap.put(o.getToken(), o.getAmount());
                                } else {
                                    tokenValueMap.put(o.getToken(), tokenValueMap.get(o.getToken()).add(o.getAmount()));
                                }
                            }
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    isFinal = true;
                    try {
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

    private boolean run = true;
    private boolean versionSet = false;

    private Token currentWallet = Token.ORIGIN;
    private Token currentTransaction = Token.ORIGIN;
    private ConcurrentMap<Token,BigInteger> tokenValueMap = new ConcurrentHashMap<>();
    private DecimalFormat format = new DecimalFormat("###,###,###,###,###,###,##0.0#######");
    private volatile boolean isFinal = false;

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
                        coresSlider.setMax(Runtime.getRuntime().availableProcessors());
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

                ki.getMinerMan().setUseCPU(useCPUcheck.isSelected());
                ki.getMinerMan().setUseGPU(useGPUcheck.isSelected());
                if (ki.getEncryptMan().getPublicKey() != null && isFinal) {

                    if(tokenValueMap.get(currentWallet) == null || tokenValueMap.get(currentWallet).compareTo(BigInteger.ZERO) == 0)
                    {
                        amountLabel.setText("0");
                    }else {
                        amountLabel.setText(format.format(tokenValueMap.get(currentWallet).longValueExact() / 100000000D));
                    }
                    heightLabel.setText("Height: " + ki.getChainMan().currentHeight());



                }
            }
            if(tokenLabel != null)
            {
                tokenLabel.setText(currentWallet.name());
            }
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
                ki.getMinerMan().startMiners(coresSlider.getValue());
                mining = true;
            } else {
                ki.getMinerMan().stopMiners();
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
    }

    public void addGenHoverOff(MouseEvent mouseEvent) {
    }

    public void addGenClicked(MouseEvent mouseEvent) {
    }

    public void addGenUnclicked(MouseEvent mouseEvent) {
    }

    public void addManHover(MouseEvent mouseEvent) {
    }

    public void addManHoverOff(MouseEvent mouseEvent) {
    }

    public void addManClicked(MouseEvent mouseEvent) {
    }

    public void addManUnclicked(MouseEvent mouseEvent) {
    }
}
