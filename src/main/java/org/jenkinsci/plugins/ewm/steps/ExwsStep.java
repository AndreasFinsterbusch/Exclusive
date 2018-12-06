package org.jenkinsci.plugins.ewm.steps;

import hudson.Extension;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.ewm.Messages;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * The 'exws' step.
 * Allocates the final external workspace on the current node and uses that as the default directory for nested steps.
 *
 * @author Alexandru Somai
 */
public class ExwsStep extends AbstractStepImpl {

    @DataBoundConstructor
    public ExwsStep() {

    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {


        public DescriptorImpl() {
            super(ExwsExecution.class);
        }


        @Override
        public String getFunctionName() {
            return "exclusive";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.steps_ExwsStep_DisplayName();
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }
    }
}
