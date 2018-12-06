package org.jenkinsci.plugins.ewm.steps;

import com.google.inject.Inject;
import hudson.AbortException;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Fingerprint;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.WorkspaceList;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.BodyExecution;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;

/**
 * The execution of {@link ExwsStep}.
 *
 * @author Alexandru Somai
 */
public class ExwsExecution extends AbstractStepExecutionImpl {

    private static final long serialVersionUID = 1L;

    @Inject(optional = true)
    private transient ExwsStep step;

    @StepContextParameter
    private transient Computer computer;

    @StepContextParameter
    private transient TaskListener listener;
    @StepContextParameter
    private transient Run run;
    private BodyExecution body;
   @Override
    public boolean start() throws Exception {


        Node node = computer.getNode();
        if (node == null) {
            throw new Exception("The node is not live due to some unexpected conditions: the node might have been taken offline, or may have been removed");
        }

    String nodeName = computer.getName();
        listener.getLogger().println("Running in " + nodeName +" or ");
       Jenkins.getInstance().doQuietDown();

        while (areComputersIdle(nodeName, Jenkins.getInstance().getComputers()) == false) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //Gracefully cancel shutdown if job is canceled in pre-build phase
                cancelShutdown();
                throw e;
            }
        }

        body = getContext().newBodyInvoker()
                .withCallback(BodyExecutionCallback.wrap(getContext()))
                .start();
        cancelShutdown();
     return false;
    }



    @Override
    public void stop(@Nonnull Throwable cause) throws Exception {
        if (body != null) {
            body.cancel(cause);
        }

        cancelShutdown();
    }



    @Deprecated
    private static final class Callback extends BodyExecutionCallback {

        private final StepContext context;

        Callback(StepContext context) {
            this.context = context;
        }

        @Override
        public void onSuccess(StepContext context, Object result) {

        }

        @Override
        public void onFailure(StepContext context, Throwable t) {
            this.context.onFailure(t);
            cancelShutdown();
        }
    }

    /**
     * @param nodeName the name of the node where this job is running from
     * @param computers all computers who has executors available
     * @return true if this job is the only one being executed on this node and
     *         all other nodes are idle. false otherwise
     */
    private boolean areComputersIdle(String nodeName, Computer[] computers) {
        for (Computer computer : computers) {
            //any other computer than the one the job is executed on should be idle
            if (computer.getName().equals(nodeName) == false && computer.isIdle() == false) {
                return false;
            }
            //check if this job is the only one being running on execution computer
            if (computer.getName().equals(nodeName) == true && computer.countBusy() != 1) {
                return false;
            }
        }
        return true;
    }
    /**
     * Cancels Jenkins shutdown mode
     *
     *
     */
    private static void cancelShutdown() {
        Jenkins.getInstance().doCancelQuietDown();
        running = false;
    }
}
