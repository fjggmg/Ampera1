package com.lifeform.main;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.lifeform.main.network.NewTransactionPacket;
import com.lifeform.main.transactions.*;


import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bryan on 7/18/2017.
 * <p>
 * Copyright (C) 2017  Bryan Sharpe
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
    private JComboBox coinSelectorView;
    private JTextField feeToPay;
    private JComboBox coinSelectorTransaction;
    private IKi ki;
    public static String blockPropped = "";

    public MainGUI(IKi ki) {
        continuousMiningButton.setEnabled(enableMining);
        this.ki = ki;
        version.setText(version.getText() + " " + Ki.VERSION);
        for (Token t : Token.values()) {
            coinSelectorView.addItem(t.name());
            coinSelectorTransaction.addItem(t.name());
        }

        sendCoinsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ki.getEncryptMan().getPublicKey() != null) {

                    Token token = Token.valueOf((String) coinSelectorTransaction.getSelectedItem());
                    double amt = Double.parseDouble(amountToSend.getText());
                    long lAmt = (long) (amt * 100000000D);
                    BigInteger amount = BigInteger.valueOf(lAmt);
                    int index = 0;
                    Address receiver = Address.decodeFromChain(walletToSendTo.getText());
                    Output output = new Output(amount, receiver, token, index);
                    java.util.List<Output> outputs = new ArrayList<>();
                    outputs.add(output);
                    java.util.List<String> keys = new ArrayList<>();
                    keys.add(ki.getEncryptMan().getPublicKeyString());
                    java.util.List<Input> inputs = new ArrayList<>();
                    double dFee = Double.parseDouble(feeToPay.getText());
                    long lFee = (long) (dFee * 100000000D);
                    BigInteger fee = BigInteger.valueOf(lFee);
                    ki.getMainLog().info("Fee is: " + fee.toString());
                    BigInteger totalInput = BigInteger.ZERO;
                    for (Address a : ki.getAddMan().getActive()) {
                        for (Output o : ki.getTransMan().getUTXOs(a)) {
                            if (o.getToken().equals(token)) {
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


                    ITrans trans = new Transaction("", 1, null, outputs, inputs, entropyMap, keys);
                    trans.makeChange(fee, ki.getAddMan().getMainAdd()); // TODO this just sends change back to the main address......will need to give option later
                    trans.addSig(ki.getEncryptMan().getPublicKeyString(), ki.getEncryptMan().sign(trans.toSign()));
                    ki.getTransMan().getPending().add(trans);

                    NewTransactionPacket ntp = new NewTransactionPacket();
                    ntp.trans = trans.toJSON();
                    ki.getNetMan().broadcastPacket(ntp);
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

    private volatile boolean mining = false;

    public void init(JFrame frame) {

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(frame,
                        "Are you sure to close this window?", "Close Origin?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    private BigInteger walletAmount = BigInteger.ZERO;
    private DecimalFormat format = new DecimalFormat("###,###,###,###,##0.0#######");

    public void tick() {

        if (ki.getEncryptMan().getPublicKey() != null) {

            publicKeyField.setText(ki.getAddMan().getMainAdd().encodeForChain());
            BigInteger amount = BigInteger.ZERO;
            java.util.List<Address> checked = new ArrayList<>();
            for (Address a : ki.getAddMan().getActive()) {
                if (checked.contains(a)) continue;
                checked.add(a);
                //ki.getMainLog().info("Getting info from Address: " + a.encodeForChain());
                if (ki.getTransMan().getUTXOs(a) != null) {
                    for (Output o : ki.getTransMan().getUTXOs(a)) {
                        if (o.getToken().equals(Token.valueOf((String) coinSelectorView.getSelectedItem())))
                            amount = amount.add(o.getAmount());
                    }
                }
            }
            accountInfo.setText("Welcome to the Origin built in wallet. Below you'll find the current amount of coins you have according to the type you've selected to the right" + "\n"
                    + coinSelectorView.getSelectedItem() + " : " + (double) (amount.longValueExact() / 100000000D) + "\n"
                    + "Current block height: " + ki.getChainMan().currentHeight());
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
        panelMain.setLayout(new GridLayoutManager(3, 6, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Origin Pre-Built Wallet");
        panelMain.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        accountInfo = new JTextPane();
        accountInfo.setEditable(false);
        panelMain.add(accountInfo, new GridConstraints(1, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        tabbedPane1 = new JTabbedPane();
        panelMain.add(tabbedPane1, new GridConstraints(2, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
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
        label2.setText("Current Address");
        panel1.add(label2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        copyButton = new JButton();
        copyButton.setText("Copy");
        panel1.add(copyButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Transactions", panel2);
        amountToSend = new JTextField();
        panel2.add(amountToSend, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        walletToSendTo = new JTextField();
        panel2.add(walletToSendTo, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sendCoinsButton = new JButton();
        sendCoinsButton.setText("Send Coins");
        panel2.add(sendCoinsButton, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Amount to send: ");
        panel2.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Address to send to: ");
        panel2.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        feeToPay = new JTextField();
        panel2.add(feeToPay, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Fee");
        panel2.add(label5, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        coinSelectorTransaction = new JComboBox();
        panel2.add(coinSelectorTransaction, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Miner", panel3);
        continuousMiningButton = new JButton();
        continuousMiningButton.setText("Start Mining");
        panel3.add(continuousMiningButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panelMain.add(spacer2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        coinSelectorView = new JComboBox();
        panelMain.add(coinSelectorView, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        version = new JLabel();
        version.setText("Version: ");
        panelMain.add(version, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }
}
