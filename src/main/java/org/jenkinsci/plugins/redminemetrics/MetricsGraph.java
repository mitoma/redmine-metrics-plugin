package org.jenkinsci.plugins.redminemetrics;

import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.util.Graph;
import hudson.util.RunList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class MetricsGraph extends Graph {

  private AbstractProject<?, ?> project;

  public MetricsGraph(AbstractProject<?, ?> project) {
    super(Calendar.getInstance(), 640, 480);
    this.project = project;
  }

  @Override
  protected JFreeChart createGraph() {
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

    JFreeChart areaChart = ChartFactory.createStackedAreaChart("Ticket",
        "BuildNum", "Count", ds, PlotOrientation.VERTICAL, true, true, false);
    CategoryPlot plot = (CategoryPlot) areaChart.getPlot();
    plot.getDomainAxis().setUpperMargin(0);
    plot.getDomainAxis().setLowerMargin(0);
    plot.getDomainAxis().setCategoryMargin(0);
    return areaChart;
  }
}
