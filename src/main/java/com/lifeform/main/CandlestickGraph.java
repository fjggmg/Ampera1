package com.lifeform.main;

import com.lifeform.main.adx.ExchangeData;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.Path;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class CandlestickGraph extends LineChart<String, Number> {

    /**
     * Constructs a XYChart given the two axes. The initial content for the chart
     * plot background and plot area that includes vertical and horizontal grid
     * lines and fills, are added.
     *
     * @param stringAxis X Axis for this XY chart
     * @param numberAxis Y Axis for this XY chart
     */
    public CandlestickGraph(Axis<String> stringAxis, Axis<Number> numberAxis, IKi ki) {
        super(stringAxis, numberAxis);
        numberAxis.autoRangingProperty().set(true);

        this.ki = ki;
    }

    private IKi ki;

    @Override
    protected void dataItemAdded(Series<String, Number> series, int itemIndex, Data<String, Number> item) {

        //System.out.println("Data item added");


    }

    @Override
    protected void dataItemRemoved(Data<String, Number> item, Series<String, Number> series) {

    }


    @Override
    protected void dataItemChanged(Data<String, Number> item) {

    }

    @Override
    protected void seriesAdded(Series<String, Number> series, int seriesIndex) {

    }

    @Override
    protected void seriesRemoved(Series<String, Number> series) {

    }

    private static final double DIV_CONSTANT = 100_000_000;
    private double x;
    private double y;
    private double close;
    private double high;
    private double low;

    @Override
    protected void layoutPlotChildren() {

        if (getData() == null) {
            return;
        }
        // update candle positions
        for (int seriesIndex = 0; seriesIndex < getData().size(); seriesIndex++) {
            Series<String, Number> series = getData().get(seriesIndex);
            Iterator<Data<String, Number>> iter = getDisplayedDataIterator(series);
            Path seriesPath = null;
            if (series.getNode() instanceof Path) {
                seriesPath = (Path) series.getNode();
                seriesPath.getElements().clear();
            }

            while (iter.hasNext()) {
                Data<String, Number> item = iter.next();
                x = getXAxis().getDisplayPosition(getCurrentDisplayedXValue(item));
                y = getYAxis().getDisplayPosition(getCurrentDisplayedYValue(item));
                Node itemNode = item.getNode();

                if (itemNode instanceof Candle && item.getYValue() != null) {
                    Candle candle = (Candle) itemNode;
                    ExchangeData data = candle.data;
                    if (getYAxis().getDisplayPosition(data.open.doubleValue() / DIV_CONSTANT) != y) {
                        y = getYAxis().getDisplayPosition(data.open.doubleValue() / DIV_CONSTANT);
                    }
                    //System.out.println("highData: " + data.high.doubleValue()/DIV_CONSTANT);
                    //System.out.println("lowData: " + data.low.doubleValue()/DIV_CONSTANT);
                    //System.out.println("closeData: " + data.close.doubleValue()/DIV_CONSTANT);
                    close = getYAxis().getDisplayPosition(data.close.doubleValue() / DIV_CONSTANT);
                    high = getYAxis().getDisplayPosition(data.high.doubleValue() / DIV_CONSTANT);
                    low = getYAxis().getDisplayPosition(data.low.doubleValue() / DIV_CONSTANT);
                    //System.out.println("close: " + close);
                    //System.out.println("high: " + high);
                    //System.out.println("low: " + low);
                    //System.out.println("close offset: " + (close-y));
                    candle.update(close - y, high - y, low - y);
                    // update candle
                    //candle.update(close - y, high - y, low - y, candleWidth);

                    // update tooltip content
                    //candle.updateTooltip(bar.getOpen(), bar.getClose(), bar.getHigh(), bar.getLow());

                    // position the candle
                    candle.setLayoutX(x);
                    candle.setLayoutY(y);
                }

            }
            //updateAxisRange();
            if (ki.getExMan().getOrderBook().hasNew()) {
                for (ExchangeData data : ki.getExMan().getOrderBook().collectRecentData(ki.getExMan().getOrderBook().newFrom())) {
                    ki.getExMan().getOrderBook().setRecent(data);
                    XYChart.Data<String, Number> item = new XYChart.Data<>(sdf.format(new Date(data.timestamp)), data.open);
                    getData().get(0).getData().add(item);
                    getPlotChildren().add(createCandle(item));
                    if (getPlotChildren().size() > 24) {
                        getPlotChildren().remove(0);
                        getData().get(0).getData().remove(0);
                        ki.debug("Removing plot items since size is > 24");
                    }
                }
            }
        }
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private Node createCandle(final Data item) {
        //System.out.println("Creating new candle");
        Node candle = item.getNode();
        if (candle instanceof Candle) {
            return candle;
        } else {
            candle = new Candle(ki.getExMan().getOrderBook().getRecent());
            item.setNode(candle);
        }
        return candle;
    }

    @Override
    protected void updateAxisRange() {
        if (getPlotChildren().isEmpty()) return;
        // For candle stick chart we need to override this method as we need to let the axis know that they need to be able
        // to cover the whole area occupied by the high to low range not just its center data value
        final Axis<String> xa = getXAxis();
        final Axis<Number> ya = getYAxis();
        List<String> xData = null;
        List<Number> yData = null;
        if (xa.isAutoRanging()) {
            xData = new ArrayList<>();
        }
        if (ya.isAutoRanging()) {
            yData = new ArrayList<>();
        }
        if (xData != null || yData != null) {
            //System.out.println("invalidating ranges:");
            for (Series<String, Number> series : getData()) {
                for (Data<String, Number> data : series.getData()) {
                    if (xData != null) {
                        xData.add(data.getXValue());
                    }
                    if (yData != null) {
                        ExchangeData extras = ((Candle) data.getNode()).data;
                        if (extras != null) {
                            //System.out.println("adding data to invalidate with: high: " + extras.high.doubleValue()/DIV_CONSTANT + " low: " + extras.low.doubleValue()/DIV_CONSTANT);
                            yData.add(extras.high.doubleValue() / DIV_CONSTANT);
                            yData.add(extras.low.doubleValue() / DIV_CONSTANT);
                        } else {
                            yData.add(data.getYValue());
                        }
                    }
                }
            }
            if (xData != null) {
                xa.invalidateRange(xData);
            }
            if (yData != null) {
                ya.invalidateRange(yData);
            }
        }
    }
}
