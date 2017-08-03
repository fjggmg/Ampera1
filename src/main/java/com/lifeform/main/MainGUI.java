package com.lifeform.main;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.blockchain.ChainManager;
import com.lifeform.main.blockchain.Miner;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.JSONManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.network.NewBlockPacket;
import com.lifeform.main.network.NewTransactionPacket;
import com.lifeform.main.network.TransactionPacket;
import com.lifeform.main.transactions.MKiTransaction;


import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bryan on 7/18/2017.
 */
public class MainGUI {
    private JTextPane accountInfo;
    private JTabbedPane tabbedPane1;
    private JTextField publicKeyField;
    private JTextField amountToSend;
    private JTextField walletToSendTo;
    private JButton sendCoinsButton;
    private JPanel panelMain;
    private JButton continuousMiningButton;
    private JLabel version;
    private JButton copyButton;
    private IKi ki;
    public static String blockPropped = "";

    public MainGUI(IKi ki) {
        continuousMiningButton.setEnabled(enableMining);
        this.ki = ki;
        version.setText(version.getText() + " " + Ki.VERSION);


        sendCoinsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ki.getEncryptMan().getPublicKey() != null) {
                    String to = walletToSendTo.getText();
                    try {
                        ki.getEncryptMan().pubKeyFromString(to);
                    } catch (Exception ex) {
                        return;
                    }
                    BigInteger amount = null;
                    try {
                        double d = Double.parseDouble(amountToSend.getText());
                        long l = (long) ((double)d * 100000000D);
                        amount = BigInteger.valueOf(l);
                    } catch (Exception ex) {
                        return;
                    }

                    MKiTransaction trans = new MKiTransaction();
                    trans.receiver = to;
                    trans.amount = amount;
                    trans.sender = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());
                    //TODO: change this to an actual relay when network is up
                    //trans.relayer = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());
                    trans.relayer = ki.getRelayer();

                    trans.relayFee = amount.divide(BigInteger.valueOf(1000L));
                    trans.transactionFee = amount.divide(BigInteger.valueOf(500L));

                    trans.height = ki.getChainMan().currentHeight();


                    Map<String, MKiTransaction> inputs = new HashMap<>(), all = new HashMap<>();
                    BigInteger inputAmount = BigInteger.ZERO;
                    all = ki.getTransMan().getInputs(Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded()));
                    if (all == null) {
                        ki.getMainLog().info("No inputs to make a transaction with.");
                        return;
                    }
                    for (String key : all.keySet()) {
                        if (all.get(key) == null) continue;
                        inputs.put(key, all.get(key));
                        inputAmount = inputAmount.add(all.get(key).amount);
                        ki.getMainLog().info("Amount of input is: " + all.get(key).amount);
                        if (inputAmount.compareTo(amount) >= 0) {
                            break;
                        }
                    }

                    if (inputAmount.compareTo(amount) < 0) {
                        ki.getMainLog().warn("Insufficient funds to complete transaction");
                        return;
                    }

                    trans.inputs = inputs;
                    trans.change = trans.calculateChange();
                    trans.ID = EncryptionManager.sha256(trans.preSigAll());
                    trans.preSig = ki.getEncryptMan().sign(trans.preSigAll());
                    //trans.relaySignature = ki.getEncryptMan().sign(trans.preSigAll());
                    //trans.signature = ki.getEncryptMan().sign(trans.all());

                    ki.getMainLog().info("Amount of transaction is: " + trans.amount);

                    NewTransactionPacket packet = new NewTransactionPacket();
                    packet.trans = trans;
                    //ki.getTransMan().getPending().put(trans.ID, trans);
                    /*TransactionPacket packet = new TransactionPacket(ki);
                    Map<String, String> data = new HashMap<>();
                    data.put("transaction", trans.toJSON());
                    packet.setData(data);
                    */
                    ki.getNetMan().broadcastPacket(packet);
                }
            }
        });
        continuousMiningButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mining = !mining;

                if (mining) {
                    Miner.mining = true;
                    BigInteger guess = BigInteger.ZERO;
                    for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
                        ki.getMainLog().info("Starting miner: " + i);
                        Miner miner = new Miner(ki, guess, guess.add(BigInteger.valueOf(1000000L)));
                        guess = guess.add(BigInteger.valueOf(1000000L));
                        miners.add(miner);
                        miner.start();
                    }

                } else {
                    Miner.mining = false;
                }
            }
        });
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                StringSelection stringSelection = new StringSelection(publicKeyField.getText());
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                clpbrd.setContents(stringSelection, null);
            }
        });
    }

    private java.util.List<Miner> miners = new ArrayList<>();
    private volatile boolean mining = false;

    public void init(JFrame frame) {

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(frame,
                        "Are you sure to close this window?", "Close Origin?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    System.exit(0);
                }
            }
        });
    }

    private BigInteger walletAmount = BigInteger.ZERO;
    private DecimalFormat format = new DecimalFormat("###,###,###,###,##0.0#######");
    public void tick() {
        walletAmount = BigInteger.ZERO;
        if (ki.getEncryptMan().getPublicKey() != null) {

            publicKeyField.setText(Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded()));

            if (ki.getTransMan().getUTXOMap().get(Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded())) != null) {
                java.util.List<String> inputList = JSONManager.parseJSONToList(ki.getTransMan().getUTXOMap().get(Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded())));
                if (inputList != null)
                    for (String input : inputList) {
                        //ki.getMainLog().info("Input: " + input);
                        if (!ki.getTransMan().getUTXOSpentMap().get(input)) {
                            walletAmount = walletAmount.add(new BigInteger(ki.getTransMan().getUTXOValueMap().get(input)));
                        }
                    }

                accountInfo.setText("Welcome to the Origin built in wallet. Below is some of your account information" + "\n" + "Current Origin: " + format.format(((double) walletAmount.longValueExact() / 100000000D)) +
                "\n" + "Current Blockchain Height: " + ki.getChainMan().currentHeight() + "\n");
            }
        }
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static boolean enableMining = false;
    public static MainGUI guiFactory(IKi ki) {

        enableMining = ki.getOptions().mining;
        MainGUI gui = new MainGUI(ki);
        JFrame frame = new JFrame("MainGUI");
        gui.init(frame);
        gui.init(frame);
        frame.setContentPane(gui.panelMain);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        return gui;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panelMain = new JPanel();
        panelMain.setLayout(new GridLayoutManager(3, 5, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Origin Pre-Built Wallet");
        panelMain.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        accountInfo = new JTextPane();
        accountInfo.setEditable(false);
        panelMain.add(accountInfo, new GridConstraints(1, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        tabbedPane1 = new JTabbedPane();
        panelMain.add(tabbedPane1, new GridConstraints(2, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Keys", panel1);
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        publicKeyField = new JTextField();
        publicKeyField.setEditable(false);
        publicKeyField.setEnabled(true);
        panel1.add(publicKeyField, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Public Key: ");
        panel1.add(label2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        copyButton = new JButton();
        copyButton.setText("Copy");
        panel1.add(copyButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Transactions", panel2);
        amountToSend = new JTextField();
        panel2.add(amountToSend, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        walletToSendTo = new JTextField();
        panel2.add(walletToSendTo, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sendCoinsButton = new JButton();
        sendCoinsButton.setText("Send Coins");
        panel2.add(sendCoinsButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Amount to send: ");
        panel2.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Address to send to: ");
        panel2.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Miner", panel3);
        continuousMiningButton = new JButton();
        continuousMiningButton.setText("Start Mining");
        panel3.add(continuousMiningButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        version = new JLabel();
        version.setText("Version: ");
        panelMain.add(version, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panelMain.add(spacer2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }
}
