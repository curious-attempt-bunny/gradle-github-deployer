package com.curiousattemptbunny.gradle.githubdeployer;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.plugins.resolver.URLResolver;

import java.io.File;
import java.io.IOException
import org.gradle.api.Project;

class GithubDeployer extends URLResolver {
    public static final String NAME = "GitHub Deployer"
    String userName
    String password

    GithubDeployer() {
        setName(NAME);
    }

    @Override
    void publish(Artifact artifact, File src, boolean overwrite) throws IOException {
        if (!userName || !password) {
            throw new IllegalStateException("Define GithubDeployer username and password in order to be able to deploy to GitHub")
        }
        println userName
        println password

        println artifact
        println src
        println overwrite

        if (src.name.endsWith(".xml")) {
            println src.text
        }
    }


}
