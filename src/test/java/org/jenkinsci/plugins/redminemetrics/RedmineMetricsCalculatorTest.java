package org.jenkinsci.plugins.redminemetrics;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mockit.NonStrictExpectations;

import org.junit.Test;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Version;

public class RedmineMetricsCalculatorTest {

  @Test
  public void testCalc() throws MetricsException, RedmineException {
    new NonStrictExpectations() {
      RedmineManager redmineManager;
      {
        redmineManager.getProjects();
        ArrayList<Project> projects = new ArrayList<Project>();
        Project p = new Project();
        p.setId(1);
        p.setName("Example");
        projects.add(p);
        returns(projects);
        redmineManager.getVersions(p.getId());
        ArrayList<Version> versions = new ArrayList<Version>();
        Version v = new Version();
        v.setName("v1");
        versions.add(v);
        returns(versions);
        Map<String, String> params = new HashMap<String, String>();
        params.put("project_id", p.getId().toString());
        params.put("fixed_version_id", "v1");
        params.put("status_id", "*");
        redmineManager.getIssues(params);
        returns(new ArrayList<Issue>());
      }
    };
    RedmineMetricsCalculator rmc = new RedmineMetricsCalculator(
        "http://example.com/", "APIKEY", "Example", "v1");
    assertEquals(0, rmc.calc().size());
  }
}
