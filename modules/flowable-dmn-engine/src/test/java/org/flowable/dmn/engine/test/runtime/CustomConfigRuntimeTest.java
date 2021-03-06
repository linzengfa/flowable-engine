/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.dmn.engine.test.runtime;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.dmn.api.DmnRuleService;
import org.flowable.dmn.api.RuleEngineExecutionResult;
import org.flowable.dmn.engine.DmnEngine;
import org.flowable.dmn.engine.test.DmnDeploymentAnnotation;
import org.flowable.dmn.engine.test.FlowableDmnRule;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Yvo Swillens
 */
public class CustomConfigRuntimeTest {

    public static String H2_TEST_JDBC_URL = "jdbc:h2:mem:flowable;DB_CLOSE_DELAY=1000";

    protected static final String ENGINE_CONFIG_1 = "custom1.flowable.dmn.cfg.xml";
    protected static final String ENGINE_CONFIG_2 = "custom2.flowable.dmn.cfg.xml";

    @Rule
    public FlowableDmnRule activitiRule1 = new FlowableDmnRule(ENGINE_CONFIG_1);

    @Rule
    public FlowableDmnRule activitiRule2 = new FlowableDmnRule(ENGINE_CONFIG_2);

    @Test
    @DmnDeploymentAnnotation(resources = "org/flowable/dmn/engine/test/deployment/post_custom_expression_function_expression_1.dmn")
    public void executeDecision_post_custom_expression_function() {

        DmnEngine dmnEngine = activitiRule1.getDmnEngine();
        DmnRuleService ruleService = dmnEngine.getDmnRuleService();

        Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate localDate = dateTimeFormatter.parseLocalDate("2015-09-18");

        processVariablesInput.put("input1", localDate.toDate());
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertSame(String.class, result.getResultVariables().get("output1").getClass());
        Assert.assertEquals("test2", result.getResultVariables().get("output1"));
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/flowable/dmn/engine/test/deployment/post_custom_expression_function_expression_1.dmn")
    public void executeDecision_custom_expression_function_missing_default_function() {

        DmnEngine dmnEngine = activitiRule2.getDmnEngine();
        DmnRuleService ruleService = dmnEngine.getDmnRuleService();

        Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate localDate = dateTimeFormatter.parseLocalDate("2015-09-18");

        processVariablesInput.put("input1", localDate.toDate());
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);

        Assert.assertEquals(0, result.getResultVariables().size());
        Assert.assertNotEquals(true, StringUtils.isEmpty(result.getAuditTrail().getRuleExecutions().get(1).getConditionResults().get(0).getException()));
    }
}
