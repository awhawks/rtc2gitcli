
package to.rtc.cli.migrate.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;

import com.ibm.team.filesystem.cli.client.internal.subcommands.AcceptCmd;
import com.ibm.team.filesystem.cli.client.internal.subcommands.AcceptCmdOptions;
import com.ibm.team.filesystem.cli.core.AbstractSubcommand;
import com.ibm.team.filesystem.cli.core.Constants;
import com.ibm.team.filesystem.cli.core.subcommands.CommonOptions;
import com.ibm.team.filesystem.cli.core.subcommands.IScmClientConfiguration;
import com.ibm.team.rtc.cli.infrastructure.internal.core.CLIClientException;
import com.ibm.team.rtc.cli.infrastructure.internal.parser.ICommandLine;
import com.ibm.team.rtc.cli.infrastructure.internal.parser.Options;
import com.ibm.team.rtc.cli.infrastructure.internal.parser.exceptions.ConflictingOptionException;

@SuppressWarnings("restriction")
public class AcceptCommandDelegate extends RtcCommandDelegate {

  public AcceptCommandDelegate(IScmClientConfiguration config, String targetWorkspace, String changeSetUuid, boolean baseline,
      boolean acceptMissingChangesets) {
    super(config, "accept " + targetWorkspace + " " + changeSetUuid + " baseline[" + baseline + "]");
    setSubCommandLine(targetWorkspace, changeSetUuid, baseline, acceptMissingChangesets);
  }

  @Override
  public int run() throws CLIClientException {
    try {
      return super.run();
    } catch (CLIClientException e) {
      IStatus status = e.getStatus();
      if (status != null) {
        switch (status.getCode()) {
          case Constants.STATUS_GAP:
            stdout().println("There was a [GAP]. We ignore that, because the following accepts should fix that");
            return Constants.STATUS_GAP;
          case Constants.STATUS_CONFLICT:
            stdout().println("There was a [CONFLICT]. We ignore that, because the following accepts should fix that");
            return Constants.STATUS_CONFLICT;
          case Constants.STATUS_NWAY_CONFLICT:
            stdout().println("There was a [NWAY_CONFLICT]. We ignore that, because the following accepts should fix that");
            return Constants.STATUS_NWAY_CONFLICT;
          case Constants.STATUS_WORKSPACE_UNCHANGED:
            stdout().println("There was a [WORKSPACE_UNCHANGED]. We ignore that, because the following accepts should fix that");
            return Constants.STATUS_WORKSPACE_UNCHANGED;
          default:
            stderr().println("There was an unexpected exception with state [" + status.getCode() + "]");
            break;
        }
      }
      throw e;
    }
  }

  @Override
  AbstractSubcommand getCommand() {
    return new AcceptCmd();
  }

  @Override
  Options getOptions() throws ConflictingOptionException {
    return new AcceptCmdOptions().getOptions();
  }

  void setSubCommandLine(String targetWorkspace, String changeSetUuid, boolean isBaseline, boolean acceptMissingChangesets) {
    String uri = getSubCommandOption(config, CommonOptions.OPT_URI);
    String username = getSubCommandOption(config, CommonOptions.OPT_USERNAME);
    String password = getSubCommandOption(config, CommonOptions.OPT_PASSWORD);
    setSubCommandLine(config,
        generateCommandLine(uri, username, password, targetWorkspace, changeSetUuid, isBaseline, acceptMissingChangesets));
  }

  private ICommandLine generateCommandLine(String uri, String username, String password, String rtcWorkspace, String changeSetUuid,
      boolean isBaseline, boolean acceptMissingChangesets) {
    List<String> args = new ArrayList<String>();
    args.add("-o");
    args.add("--no-merge");
    if (acceptMissingChangesets) {
      args.add("--accept-missing-changesets");
    }
    args.add("-r");
    args.add(uri);
    args.add("-t");
    args.add(rtcWorkspace);
    args.add("-u");
    args.add(username);
    args.add("-P");
    args.add(password);

    if (isBaseline) {
      args.add("--baseline");
    } else {
      args.add("--changes");
    }
    args.add(changeSetUuid);
    return generateCommandLine(args);
  }

}
