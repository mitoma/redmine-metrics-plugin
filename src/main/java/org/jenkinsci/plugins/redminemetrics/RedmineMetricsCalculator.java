package org.jenkinsci.plugins.redminemetrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Version;

public class RedmineMetricsCalculator {

  private String url;
  private String apiKey;
  private String projectName;
  private String versions;

  public RedmineMetricsCalculator(String url, String apiKey,
      String projectName, String versions) {
    this.url = url;
    this.apiKey = apiKey;
    this.projectName = projectName;
    this.versions = versions;
  }

  public List<MetricsResult> calc() throws MetricsException {
    List<MetricsResult> result = new ArrayList<MetricsResult>();
    try {
      RedmineManager manager = new RedmineManager(url, apiKey);
      Project proj = getProject(manager);

      List<String> versionsList = getVersionsString(manager, proj);

      Map<String, Integer> tmpCalcMap = new HashMap<String, Integer>();
      for (String v : versionsList) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("project_id", proj.getId().toString());
        params.put("fixed_version_id", v);
        params.put("status_id", "*");

        for (Issue issue : manager.getIssues(params)) {
          String status = issue.getStatusName();
          if (!tmpCalcMap.containsKey(status)) {
            tmpCalcMap.put(status, 0);
          }
          Integer count = tmpCalcMap.get(status);
          tmpCalcMap.put(status, count + 1);
        }
      }
      for (Entry<String, Integer> e : tmpCalcMap.entrySet()) {
        result.add(new MetricsResult(e.getKey(), e.getValue()));
      }
    } catch (RedmineException e) {
      throw new MetricsException(e);
    }
    return result;
  }

  private Project getProject(RedmineManager manager) throws RedmineException {
    for (Project proj : manager.getProjects()) {
      if (!projectName.equals(proj.getName())) {
        continue;
      }
      return proj;
    }
    return null;
  }

  private List<String> getVersionsString(RedmineManager manager, Project proj)
      throws RedmineException {
    List<Version> allVersions = manager.getVersions(proj.getId());
    if (versions.isEmpty()) {
      return allVersionsWithNull(allVersions);
    }
    List<String> vs = new ArrayList<String>();
    String[] versionStrings = versions.split(",");
    for (String string : versionStrings) {
      for (Version v : allVersions) {
        if (string.trim().equals(v.getName())) {
          vs.add(String.valueOf(v.getId()));
        }
      }
    }
    return vs;
  }

  private List<String> allVersionsWithNull(List<Version> versionList) {
    List<String> vs = new ArrayList<String>();
    vs.add("!*");
    for (Version v : versionList) {
      vs.add(String.valueOf(v.getId()));
    }
    return vs;
  }
}
