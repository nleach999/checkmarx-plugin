package com.checkmarx.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The main entry point for Checkmarx plugin. This class implements the Builder
 * build stage that scans the source code.
 *
 * @author Denis Krivitski
 * @since 3/10/13
 */

public class CxScanBuilder extends Builder {

    //////////////////////////////////////////////////////////////////////////////////////
    // Persistent plugin configuration parameters
    //////////////////////////////////////////////////////////////////////////////////////

    private String serverUrl;
    private String username;
    private String password;
    private String projectName;

    private String preset;
    private boolean presetSpecified;
    private String includeExtensions;
    private String locationPathExclude;

    private String comment;

    /*

    V -comment <text>                             Scan comment. Example: -comment 'important scan1'. Optional.
    -Configuration <configuration>              If configuration is not set, "Default Configuration" will be used for a
                                                new project. Optional.
    V -CxPassword <password>                      Login password
    V -CxServer <server>                          IP address or resolvable name of CxSuite web server
    V -CxUser <username>                          Login username
    -incremental                                Run incremental scan instead of full scan. Optional.
    -LocationBranch <branch>                    Sources GIT branch. Required if -LocationType is GIT. Optional.
    -LocationPassword <password>                Source control or network password. Required if -LocationType is
                                                TFS/SVN/shared.
    -LocationPath <path>                        Local or shared path to sources or source repository branch. Required if
                                                -LocationType is folder/shared.
    V -LocationPathExclude <file list>            List of ignored folders. Relative paths are resolved retalive to
                                                -LocationPath. Example: -LocationPathExclude test* log_*. Optional.
    -LocationPort <url>                         Source control system port. Default 8080/80 (TFS/SVN). Optional.
    -LocationPrivateKey <file>                  GIT private key location. Required  if -LocationType is GIT in SSH mode.
    -LocationPublicKey <file>                   GIT public key location. Required  if -LocationType is GIT in SSH mode.
    -LocationType <folder|shared|TFS|SVN|GIT>   Source location type: folder, shared folder, source repository: SVN,
                                                TFS, GIT
    -LocationURL <url>                          Source control URL. Required if -LocationType is TFS/SVN/GIT.
    -LocationUser <username>                    Source control or network username. Required if -LocationType is
                                                TFS/SVN/shared.
    -log <file>                                 Log file. Optional.
    V -Preset <preset>                            If preset is not specified, will use the predefined preset for an
                                                existing project, and Default preset for a new project. Optional.
    -private                                    Scan will not be visible to other users. Optional.
    V -comment <text>                             Scan comment. Example: -comment 'important scan1'. Optional.
    -Configuration <configuration>              If configuration is not set, "Default Configuration" will be used for a
                                                new project. Optional.
    V -CxPassword <password>                      Login password
    V -CxServer <server>                          IP address or resolvable name of CxSuite web server
    V -CxUser <username>                          Login username
    -incremental                                Run incremental scan instead of full scan. Optional.
    -LocationBranch <branch>                    Sources GIT branch. Required if -LocationType is GIT. Optional.
    -LocationPassword <password>                Source control or network password. Required if -LocationType is
                                                TFS/SVN/shared.
    -LocationPath <path>                        Local or shared path to sources or source repository branch. Required if
                                                -LocationType is folder/shared.
    V -LocationPathExclude <file list>            List of ignored folders. Relative paths are resolved retalive to
                                                -LocationPath. Example: -LocationPathExclude test* log_*. Optional.
    -LocationPort <url>                         Source control system port. Default 8080/80 (TFS/SVN). Optional.
    -LocationPrivateKey <file>                  GIT private key location. Required  if -LocationType is GIT in SSH mode.
    -LocationPublicKey <file>                   GIT public key location. Required  if -LocationType is GIT in SSH mode.
    -LocationType <folder|shared|TFS|SVN|GIT>   Source location type: folder, shared folder, source repository: SVN,
                                                TFS, GIT
    -LocationURL <url>                          Source control URL. Required if -LocationType is TFS/SVN/GIT.
    -LocationUser <username>                    Source control or network username. Required if -LocationType is
                                                TFS/SVN/shared.
    -log <file>                                 Log file. Optional.
    V -Preset <preset>                            If preset is not specified, will use the predefined preset for an
                                                existing project, and Default preset for a new project. Optional.
    -private                                    Scan will not be visible to other users. Optional.
    V -ProjectName <project name>                 A full absolute name of a project. The full Project name includes the
                                                whole path to the project, including Server, service provider, company,
                                                and team. Example:  -ProjectName "CxServer\SP\Company\Users\bs java" If
                                                project with such a name doesn't exist in the system, new project will
                                                be created.
    -ReportCSV <file>                           Name or path to results CSV file. Optional.
    -ReportPDF <file>                           Name or path to results PDF file. Optional.
    -ReportRTF <file>                           Name or path to results RTF file. Optional.
    -ReportXML <file>                           Name or path to results XML file. Optional.
    -v,--verbose                                Turns on verbose mode. All messages and events will be sent to the
                                                console/log file.  Optional.
     */

    //////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    //////////////////////////////////////////////////////////////////////////////////////



    @DataBoundConstructor
    public CxScanBuilder(String serverUrl,
                         String username,
                         String password,
                         String projectName,
                         String preset,
                         boolean presetSpecified,
                         String includeExtensions,
                         String locationPathExclude,
                         String comment)
    {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        this.projectName = projectName;
        this.preset = preset;
        this.presetSpecified = presetSpecified;
        this.includeExtensions = includeExtensions;
        this.locationPathExclude = locationPathExclude;
        this.comment = comment;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // Configuration fields getters
    //////////////////////////////////////////////////////////////////////////////////////

    public String getServerUrl() {
        return serverUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getPreset() {
        return preset;
    }

    public boolean isPresetSpecified() {
        return presetSpecified;
    }


    public String getIncludeExtensions() {
        return includeExtensions;
    }

    public String getLocationPathExclude() {
        return locationPathExclude;
    }

    public String getComment() {
        return comment;
    }



    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return super.perform(build, launcher, listener);
    }



    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }


    /*public String getIconPath() {
        PluginWrapper wrapper = Hudson.getInstance().getPluginManager().getPlugin([YOUR-PLUGIN-MAIN-CLASS].class);
        return Hudson.getInstance().getRootUrl() + "plugin/"+ wrapper.getShortName()+"/";
    }*/

    public String getMyString() {
        return "Hello Jenkins!";
    }
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public static String DEFAULT_INCLUDE_EXTENSION = ".java, .c, .cs";
        public static String DEFAULT_EXCLUDE_FOLDERS = "target, work, src/main/resources";


        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        //////////////////////////////////////////////////////////////////////////////////////
        // Field value validators
        //////////////////////////////////////////////////////////////////////////////////////

        public FormValidation doCheckServerUrl(@QueryParameter String value) {
            try {
                Thread.sleep(20*1000);
            } catch (Exception e)
            {

            }
            try {
                URL u = new URL(value);
                return FormValidation.ok();
            } catch (MalformedURLException e)
            {
                return FormValidation.error(e.getMessage());
            }
        }


        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Execute Checkmarx Scan";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().

            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)

            // save();
            return super.configure(req,formData);
        }


    }
}