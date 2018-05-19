package com.ampex.main.GUI;

import com.ampex.main.adx.ExchangeData;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

import java.text.DecimalFormat;

public class Candle extends Group {
    private Line highLow = new Line();
    private Line open = new Line();
    private Region bar = new Region();
    ExchangeData data;
    private boolean openAboveClose = false;
    private static final double DIV_CONSTANT = 100_000_000;
    private Tooltip tooltip = new Tooltip();

    public Candle(ExchangeData data) {
        this.data = data;
        setAutoSizeChildren(false);
        getChildren().addAll(highLow, bar, open);
        highLow.setStrokeWidth(2);
        highLow.relocate(-1, 0);
        open.setStrokeWidth(2);
        open.relocate(-1, -1);
        open.setStartX(-8);
        open.setEndX(8);
        //update();
        tooltip.setGraphic(new TooltipContent());
        Tooltip.install(bar, tooltip);
        //System.out.println("candle created");
    }

    private static final Color red = Color.valueOf("#c84128");
    private static final Color green = Color.valueOf("#18BC9C");
    private static final Background redBack = new Background(new BackgroundFill(red, CornerRadii.EMPTY, Insets.EMPTY));
    private static final Background greenBack = new Background(new BackgroundFill(green, CornerRadii.EMPTY, Insets.EMPTY));

    public void update(double closeOffset, double highOffset, double lowOffset) {
        //System.out.println("candle updated");
        openAboveClose = closeOffset > 0;

        highLow.setStartY(highOffset);
        highLow.setEndY(lowOffset);

        //this is purposefully reversed, not sure why it has to work this way
        if (openAboveClose) {
            open.setStroke(red);
            open.setFill(red);
            highLow.setStroke(red);
            highLow.setFill(red);
            bar.setBackground(redBack);
            bar.resizeRelocate(-10, 0, 20, closeOffset);
        } else {
            open.setStroke(green);
            open.setFill(green);
            highLow.setStroke(green);
            highLow.setFill(green);
            bar.setBackground(greenBack);

            bar.resizeRelocate(-10, closeOffset, 20, closeOffset * -1);
        }
        //System.out.println("candle created");
        TooltipContent tc = (TooltipContent) tooltip.getGraphic();
        tc.update(data.open.doubleValue() / DIV_CONSTANT, data.close.doubleValue() / DIV_CONSTANT, data.high.doubleValue() / DIV_CONSTANT, data.low.doubleValue() / DIV_CONSTANT, data.avg.doubleValue() / DIV_CONSTANT);
    }

    private DecimalFormat format2 = new DecimalFormat("###,###,###,###,###,###,###,###,##0.#########");

    private class TooltipContent extends GridPane {

        private final Label openValue = new Label();
        private final Label closeValue = new Label();
        private final Label highValue = new Label();
        private final Label lowValue = new Label();
        private final Label avgValue = new Label();

        private TooltipContent() {
            Label open = new Label("OPEN:");
            Label close = new Label("CLOSE:");
            Label high = new Label("HIGH:");
            Label low = new Label("LOW:");
            Label avg = new Label("VWAP:");
            open.setFont(Font.font(16));
            close.setFont(Font.font(16));
            high.setFont(Font.font(16));
            low.setFont(Font.font(16));
            avg.setFont(Font.font(16));
            openValue.setFont(Font.font(16));
            closeValue.setFont(Font.font(16));
            highValue.setFont(Font.font(16));
            lowValue.setFont(Font.font(16));
            avgValue.setFont(Font.font(16));
            open.getStyleClass().add("candlestick-tooltip-label");
            close.getStyleClass().add("candlestick-tooltip-label");
            high.getStyleClass().add("candlestick-tooltip-label");
            low.getStyleClass().add("candlestick-tooltip-label");
            avg.getStyleClass().add("candlestick-tooltip-label");
            setConstraints(open, 0, 0);
            setConstraints(openValue, 1, 0);
            setConstraints(close, 0, 1);
            setConstraints(closeValue, 1, 1);
            setConstraints(high, 0, 2);
            setConstraints(highValue, 1, 2);
            setConstraints(low, 0, 3);
            setConstraints(lowValue, 1, 3);
            setConstraints(avg, 0, 4);
            setConstraints(avgValue, 1, 4);
            getChildren().addAll(open, openValue, close, closeValue, high, highValue, low, lowValue, avg, avgValue);
        }

        public void update(double open, double close, double high, double low, double avg) {
            openValue.setText(format2.format(open));
            closeValue.setText(format2.format(close));
            highValue.setText(format2.format(high));
            lowValue.setText(format2.format(low));
            avgValue.setText(format2.format(avg));
        }
    }
}
