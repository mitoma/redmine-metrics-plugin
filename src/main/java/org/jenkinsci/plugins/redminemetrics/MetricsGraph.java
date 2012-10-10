package org.jenkinsci.plugins.redminemetrics;

import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.util.Graph;
import hudson.util.RunList;
import hudson.util.ShiftedCategoryAxis;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;

public class MetricsGraph extends Graph {

  private AbstractProject<?, ?> project;

  private DefaultCategoryDataset createDataset() {
    List<MetricsAction> actions = new ArrayList<MetricsAction>();
    RunList<?> builds = project.getBuilds();
    for (Run<?, ?> run : builds) {
      MetricsAction action = run.getAction(MetricsAction.class);
      if (action == null) {
        continue;
      }
      actions.add(action);
    }

    Collections.reverse(actions);

    DefaultCategoryDataset ds = new DefaultCategoryDataset();
    for (MetricsAction action : actions) {
      List<MetricsResult> metricsList = action.getMetricsList();
      for (MetricsResult result : metricsList) {
        ds.addValue(result.getCount(), result.getStatus(), "#"
            + action.getBuild().getNumber());
      }
    }
    return ds;
  }

  public MetricsGraph(AbstractProject<?, ?> project) {
    super(Calendar.getInstance(), 640, 480);
    this.project = project;
  }

  @Override
  protected JFreeChart createGraph() {
    DefaultCategoryDataset dataset = createDataset();

    JFreeChart chart = ChartFactory.createStackedAreaChart("Ticket",
        "BuildNum", "Count", dataset, PlotOrientation.VERTICAL, true, true,
        false);
    chart.setBackgroundPaint(Color.white);

    CategoryPlot plot = (CategoryPlot) chart.getPlot();
    plot.setBackgroundPaint(Color.WHITE);
    plot.setOutlinePaint(null);
    plot.setForegroundAlpha(0.8f);
    plot.setRangeGridlinesVisible(true);
    plot.setRangeGridlinePaint(Color.black);

    CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
    plot.setDomainAxis(domainAxis);
    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
    domainAxis.setLowerMargin(0.0);
    domainAxis.setUpperMargin(0.0);
    domainAxis.setCategoryMargin(0.0);

    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    rangeAxis.setAutoRange(true);

    plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));
    return chart;
  }

}
